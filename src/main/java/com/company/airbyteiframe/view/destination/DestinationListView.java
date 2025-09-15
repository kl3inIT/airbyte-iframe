package com.company.airbyteiframe.view.destination;

import com.company.airbyteiframe.entity.Destination;
import com.company.airbyteiframe.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "destinations", layout = MainView.class)
@ViewController(id = "Destination.list")
@ViewDescriptor(path = "destination-list-view.xml")
@LookupComponent("destinationsDataGrid")
@DialogMode(width = "64em")
public class DestinationListView extends StandardListView<Destination> {
    @Subscribe
    public void onBeforeShow(final BeforeShowEvent event) {
        
    }

}
