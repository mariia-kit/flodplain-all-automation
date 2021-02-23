package com.here.platform.proxy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ProxyProviderResource {
    Long id;
    String title;
    String path;
    String hrn;

    public ProxyProviderResource() {

    };

    public ProxyProviderResource(String title, String path) {
        this.title = title;
        this.path = path;
    };

    public ProxyProviderResource withId(Long id) {
        this.id = id;
        return this;
    }

}
