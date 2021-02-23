package com.here.platform.proxy.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
public class ProxyResourceRequest {
    private List<ProxyProviderResource> resources;

    public ProxyResourceRequest() {

    }

}
