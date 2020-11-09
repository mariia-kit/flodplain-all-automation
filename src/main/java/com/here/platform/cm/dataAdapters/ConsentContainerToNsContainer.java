package com.here.platform.cm.dataAdapters;

import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.ns.dto.Container;


public class ConsentContainerToNsContainer {

    private final Container nsContainer;

    public ConsentContainerToNsContainer(ConsentRequestContainer container) {
        this.nsContainer = new Container(
                container.getId(),
                container.getName(),
                container.getProvider().getName(),
                container.getContainerDescription(),
                String.join(",", container.getResources()),
                true,
                container.getScopeValue());

    }

    public Container nsContainer() {
        return nsContainer;
    }

}
