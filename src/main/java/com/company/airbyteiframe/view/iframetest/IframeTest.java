package com.company.airbyteiframe.view.iframetest;

import com.company.airbyteiframe.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.router.Route;
import elemental.json.JsonObject;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.*;

import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "iframe-test", layout = MainView.class)
@ViewController("IframeTest")
@ViewDescriptor("iframe-test.xml")
public class IframeTest extends StandardView {

    @ViewComponent
    private IFrame airbyteFrame;

    // Cho DEV: origin của Airbyte UI
    private static final String ALLOWED_ORIGIN = "http://localhost:3000"; // Prod: thay bằng domain thật@io.jmix.flowui.view.Subscribe
    @Autowired
    private Notifications notifications;

    @Subscribe
    public void onInit(final InitEvent event) {
        UI.getCurrent().getPage().executeJs(
                """
                        (function(frameId, allowedOrigin){
                          const frame = document.getElementById(frameId);
                          if (!frame) return;

                          // chống đăng ký trùng (dev/HMR)
                          window._airbyteBridge = window._airbyteBridge || {};
                          if (window._airbyteBridge[frameId]) return;
                          window._airbyteBridge[frameId] = true;

                          window.addEventListener('message', function(ev){
                            // Chỉ nhận message đến từ đúng iframe này
                            if (!frame.contentWindow || ev.source !== frame.contentWindow) return;

                            // Siết origin nếu cấu hình
                            if (allowedOrigin && allowedOrigin !== '*' && ev.origin !== allowedOrigin) return;

                            const data = ev.data || {};
                            const detail = {
                              origin: ev.origin || '',
                              type: data.type || '',
                              // Đảm bảo payload là chuỗi để server đọc dễ
                              payload: (typeof data.payload === 'string') ? data.payload
                                       : JSON.stringify(data.payload || {})
                            };
                            frame.dispatchEvent(new CustomEvent('airbyte-message', { detail }));
                          });
                        })($0, $1);
                        """,
                airbyteFrame.getId().orElse("airbyteFrame"),
                ALLOWED_ORIGIN
        );

        airbyteFrame.getElement()
                .addEventListener("airbyte-message", (DomEventListener) e -> {
                    String type = e.getEventData().getString("event.detail.type");
                    String payloadJson = e.getEventData().getString("event.detail.payload");
                    String origin = e.getEventData().getString("event.detail.origin");

                    String name = extractJson(payloadJson, "name");
                    String sourceId = extractJson(payloadJson, "sourceId");
                    String message = extractJson(payloadJson, "message");

                    switch (type) {
                        case "SOURCE_CREATE_SUCCESS" -> {
                            String title = "Tạo Source thành công";
                            String body =
                                    (name != null ? "Tên: " + name : "") +
                                            (sourceId != null ? ((name != null ? " • " : "") + "ID: " + sourceId) : "") +
                                            (origin != null && !origin.isBlank() ? " • from " + origin : "");

                            notifications.create(title, body)
                                    .withType(Notifications.Type.SUCCESS)
                                    .withPosition(Notification.Position.TOP_END)
                                    // .withCloseable(true) // nếu muốn có nút đóng
                                    .show();
                        }
                        case "SOURCE_CREATE_ERROR" -> {
                            String title = "Tạo Source thất bại";
                            String body =
                                    (name != null ? "Tên: " + name + " • " : "") +
                                            (message != null ? message : "Unknown error");

                            notifications.create(title, body)
                                    .withType(Notifications.Type.ERROR) // ERROR tự set assertive=true
                                    .withPosition(Notification.Position.TOP_END)
                                    .show();
                        }
                        default -> {

                        }
                    }
                })
                .addEventData("event.detail.type")
                .addEventData("event.detail.payload")
                .addEventData("event.detail.origin");

    }

    // ====== Helper rất nhỏ để lấy value trong JSON (tránh kéo ObjectMapper nếu bạn muốn gọn):
    private String extractJson(String json, String key) {
        if (json == null || key == null) return null;
        // Cách đơn giản: tìm theo pattern "key":"value" (chỉ hợp với payload phẳng, không có quote đặc biệt)
        String pat = "\"" + key + "\"";
        int i = json.indexOf(pat);
        if (i < 0) return null;
        int colon = json.indexOf(':', i + pat.length());
        if (colon < 0) return null;
        // bỏ qua khoảng trắng
        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        if (start >= json.length()) return null;

        // nếu là chuỗi
        if (json.charAt(start) == '"') {
            int end = json.indexOf('"', start + 1);
            if (end > start) return json.substring(start + 1, end);
        }
        // nếu là số / boolean / null
        int endComma = json.indexOf(',', start);
        int endBrace = json.indexOf('}', start);
        int end = (endComma < 0) ? endBrace : ((endBrace < 0) ? endComma : Math.min(endComma, endBrace));
        if (end > start) return json.substring(start, end).trim();
        return null;
    }

}