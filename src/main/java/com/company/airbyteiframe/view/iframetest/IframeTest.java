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


}