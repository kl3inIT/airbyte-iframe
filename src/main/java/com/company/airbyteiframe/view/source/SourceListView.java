package com.company.airbyteiframe.view.source;

import com.company.airbyteiframe.entity.Source;
import com.company.airbyteiframe.view.main.MainView;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;
import io.jmix.flowui.kit.component.button.JmixButton;


@Route(value = "sources", layout = MainView.class)
@ViewController(id = "Source.list")
@ViewDescriptor(path = "source-list-view.xml")
@LookupComponent("sourcesDataGrid")
@DialogMode(width = "64em")
public class SourceListView extends StandardListView<Source> {
    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        
    }

}