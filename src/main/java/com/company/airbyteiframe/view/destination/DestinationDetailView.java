package com.company.airbyteiframe.view.destination;

import com.company.airbyteiframe.entity.Destination;
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


@Route(value = "destinations/:id", layout = MainView.class)
@ViewController("Destination.detail")
@ViewDescriptor("destination-detail-view.xml")
@EditedEntityContainer("destinationDc")
public class DestinationDetailView extends StandardDetailView<Destination> {

    @Autowired
    private Notifications notifications;

    @Autowired
    private ObjectMapper objectMapper;

    @ViewComponent
    private IFrame airbyteFrame;

    private static final String WORKSPACE_ID = "eb9e7f02-519f-403f-bf95-13c5c7d18bed";
    private static final String AIRBYTE_ORIGIN = "http://localhost:3000";
    private static final String AIRBYTE_BASE = AIRBYTE_ORIGIN + "/workspaces/" + WORKSPACE_ID;
    @Autowired
    private ViewNavigators viewNavigators;

    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        Destination dest = getEditedEntity();

        String url = (dest.getDestinationId() == null)
                ? AIRBYTE_BASE + "/destination/new-destination"
                : AIRBYTE_BASE + "/destination/" + dest.getDestinationId();
        airbyteFrame.setSrc(url);
    }

    @Subscribe
    public void onInit(InitEvent event) {

    }

    @Subscribe
    public void onReady(final ReadyEvent event) {
        UI.getCurrent().getPage().executeJs(
                """
                        (function(frame, allowedOriginParam){
                          if (!frame) return;
                        
                          1) Dedupe theo ELEMENT, không theo id chuỗi
                          window._airbyteBridge = window._airbyteBridge || new WeakMap();
                          if (window._airbyteBridge.has(frame)) return;
                        
                          2) Origin: nếu không truyền, tự lấy từ frame.src cho đúng port
                          const allowedOrigin = (allowedOriginParam && allowedOriginParam !== '*')
                            ? allowedOriginParam
                            : (function(){ try { return new URL(frame.src).origin; } catch(e) { return ''; } })();
                        
                          const handler = function(ev){
                            if (!frame.contentWindow || ev.source !== frame.contentWindow) return;
                            if (allowedOrigin && ev.origin !== allowedOrigin) return;
                        
                            const data = ev.data || {};
                            const detail = {
                              origin: ev.origin || '',
                              type: data.type || '',
                              payload: (typeof data.payload === 'string') ? data.payload : JSON.stringify(data.payload || {})
                            };
                            frame.dispatchEvent(new CustomEvent('airbyte-message', { detail }));
                          };
                        
                          window.addEventListener('message', handler);
                          window._airbyteBridge.set(frame, handler);
                        
                          3) Cleanup tự động khi iframe bị gỡ khỏi DOM (tránh rò rỉ + stale ref)
                          const obs = new MutationObserver(() => {
                            if (!document.body.contains(frame)) {
                              const h = window._airbyteBridge.get(frame);
                              if (h) window.removeEventListener('message', h);
                              window._airbyteBridge.delete(frame);
                              obs.disconnect();
                            }
                          });
                          obs.observe(document.body, { childList: true, subtree: true });
                        })($0, $1);
                        """,
                airbyteFrame.getElement(),
                AIRBYTE_ORIGIN
        );

        airbyteFrame.getElement()
                .addEventListener("airbyte-message", (DomEventListener) e -> {
                    String type = e.getEventData().getString("event.detail.type");
                    String payloadJson = e.getEventData().getString("event.detail.payload");
                    String origin = e.getEventData().getString("event.detail.origin");

                    if ("DESTINATION_CREATE_SUCCESS".equals(type)) {
                        handleCreateSuccess(payloadJson, origin);
                    } else if ("DESTINATION_CREATE_ERROR".equals(type)) {
                        handleCreateError(payloadJson);
                    } else {
//           Other events
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
            String destinationIdStr = n.path("destinationId").asText(null);
            String destinationType = n.path("destinationType").asText(null);

            Destination destination = getEditedEntity();

            destination.setDestinationId(destinationIdStr);
            destination.setDestinationType(destinationType);
            destination.setName(name);

            getViewData().getDataContext().save();
            getEditedEntity().setId(null);
            String body = sbJoin(" • ",
                    nonBlank("Tên: ", name),
                    nonBlank("from ", origin)
            );

            notifications.create("Tạo Destination thành công", body)
                    .withType(Notifications.Type.SUCCESS)
                    .withPosition(Notification.Position.TOP_END)
                    .show();

            viewNavigators.view(this, "Destination.list").navigate();

        } catch (Exception ex) {
            notifications.create("Tạo Destination thành công", payloadJson)
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

            notifications.create("Tạo Destination thất bại", body)
                    .withType(Notifications.Type.ERROR)
                    .withPosition(Notification.Position.TOP_END)
                    .show();

        } catch (Exception ex) {
            notifications.create("Tạo Destination thất bại",
                            (payloadJson != null && !payloadJson.isBlank()) ? payloadJson : "Unknown error")
                    .withType(Notifications.Type.ERROR)
                    .withPosition(Notification.Position.TOP_END)
                    .show();
        }
    }

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
