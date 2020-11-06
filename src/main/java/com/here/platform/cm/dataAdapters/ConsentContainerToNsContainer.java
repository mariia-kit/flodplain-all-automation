package com.here.platform.cm.dataAdapters;

import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.ns.dto.Container;
import java.util.stream.Collectors;


public class ConsentContainerToNsContainer {

    private final Container nsContainer;

    public ConsentContainerToNsContainer(ConsentRequestContainer container) {
        this.nsContainer = new Container(
                container.getId(),
                container.getName(),
                container.getProvider().getName(),
                container.getContainerDescription(),
                container.getResources().stream().collect(Collectors.joining(",")),
                true,
                container.getScopeValue());

    }

    public Container nsContainer() {
        return nsContainer;
    }
}
