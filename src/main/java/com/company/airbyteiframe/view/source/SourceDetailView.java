package com.company.airbyteiframe.view.source;

import com.company.airbyteiframe.entity.Source;
import com.company.airbyteiframe.view.main.MainView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Route(value = "sources/:id", layout = MainView.class)
@ViewController("Source.detail")
@ViewDescriptor("source-detail-view.xml")
@EditedEntityContainer("sourceDc")
public class SourceDetailView extends StandardDetailView<Source> {

    @Autowired
    private Notifications notifications;

    @Autowired
    private ObjectMapper objectMapper;

    @ViewComponent
    private IFrame airbyteFrame;

    // === Cấu hình Airbyte (DEV) ===
    private static final String WORKSPACE_ID = "fba7cc07-72e6-487d-b82a-1368de8d8f29";
    private static final String AIRBYTE_ORIGIN = "http://localhost:3000";
    private static final String AIRBYTE_BASE = AIRBYTE_ORIGIN + "/workspaces/" + WORKSPACE_ID;
    @Autowired
    private ViewNavigators viewNavigators;

    // 1) Chọn URL cho iFrame theo sourceId (new vs edit)
    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        Source src = getEditedEntity();

        String url = (src.getSourceId() == null)
                ? AIRBYTE_BASE + "/source/new-source"          // tạo mới
                : AIRBYTE_BASE + "/source/" + src.getSourceId();// chỉnh sửa
        airbyteFrame.setSrc(url);
    }

    // 2) Bridge JS + bắt sự kiện từ iFrame và xử lý bằng ObjectMapper
    @Subscribe
    public void onInit(InitEvent event) {

    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
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
                AIRBYTE_ORIGIN
        );

        // Server-side listener: nhận CustomEvent('airbyte-message')
        airbyteFrame.getElement()
                .addEventListener("airbyte-message", (DomEventListener) e -> {
                    String type = e.getEventData().getString("event.detail.type");
                    String payloadJson = e.getEventData().getString("event.detail.payload");
                    String origin = e.getEventData().getString("event.detail.origin");

                    if ("SOURCE_CREATE_SUCCESS".equals(type)) {
                        handleCreateSuccess(payloadJson, origin);
                    } else if ("SOURCE_CREATE_ERROR".equals(type)) {
                        handleCreateError(payloadJson);
                    } else {
                        // các event khác (READY/ACK/...) — nếu cần thì log/handle thêm
                    }
                })
                .addEventData("event.detail.type")
                .addEventData("event.detail.payload")
                .addEventData("event.detail.origin");
    }

    private void handleCreateSuccess(String payloadJson, String origin) {
        try {
            JsonNode n = objectMapper.readTree(payloadJson);
            String name = n.path("name").asText(null);
            String sourceIdStr = n.path("sourceId").asText(null);
            String sourceType = n.path("sourceType").asText(null);   // bạn đã gửi từ React

            // Cập nhật entity
            Source source = getEditedEntity();

            source.setSourceId(sourceIdStr);
            source.setSourceType(sourceType);
            source.setName(name);

            getViewData().getDataContext().save();

            String body = sbJoin(" • ",
                    nonBlank("Tên: ", name),
                    nonBlank("from ", origin)
            );

            notifications.create("Tạo Source thành công", body)
                    .withType(Notifications.Type.SUCCESS)
                    .withPosition(Notification.Position.TOP_END)
                    .show();

            viewNavigators.view(this, "Source.list").navigate();

        } catch (Exception ex) {
            notifications.create("Tạo Source thành công", payloadJson)
                    .withType(Notifications.Type.SUCCESS)
                    .withPosition(Notification.Position.TOP_END)
                    .show();
        }
    }

    private void handleCreateError(String payloadJson) {
        try {
            JsonNode n = objectMapper.readTree(payloadJson);
            String name = n.path("name").asText(null);
            String msg = n.path("message").asText("Unknown error");

            String body = sbJoin(" • ",
                    nonBlank("Tên: ", name),
                    nonBlank("", msg)
            );

            notifications.create("Tạo Source thất bại", body)
                    .withType(Notifications.Type.ERROR)
                    .withPosition(Notification.Position.TOP_END)
                    .show();

        } catch (Exception ex) {
            notifications.create("Tạo Source thất bại",
                            (payloadJson != null && !payloadJson.isBlank()) ? payloadJson : "Unknown error")
                    .withType(Notifications.Type.ERROR)
                    .withPosition(Notification.Position.TOP_END)
                    .show();
        }
    }

    // ===== helpers =====
    private static String nonBlank(String prefix, String v) {
        return (v == null || v.isBlank()) ? null : prefix + v;
    }

    private static String sbJoin(String sep, String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p == null) continue;
            if (!sb.isEmpty()) sb.append(sep);
            sb.append(p);
        }
        return sb.toString();
    }
}
