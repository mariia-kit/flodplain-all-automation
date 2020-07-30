package com.here.platform.ns.dto;

import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.utils.NS_Config;
import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.json.JSONException;
import org.json.JSONObject;


@Getter
@Setter
@EqualsAndHashCode
@ToString
public class DataProvider {

    private String name;
    private String url;
    @EqualsAndHashCode.Exclude
    private List<ProviderResource> resources;

    @ConstructorProperties({"name", "url"})
    public DataProvider(String name, String url) {
        this.name = name;
        this.url = url;
        this.resources = new ArrayList<>();
    }

    public String generateBody() {
        JSONObject object = new JSONObject();
        if (this.getUrl() != null) {
            try {
                object.put("url", this.getUrl());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return object.toString();
    }

    public void addResource(ProviderResource resource) {
        resources.add(resource);
    }

    public DataProvider withName(String name) {
        this.name = name;
        return this;
    }

    public DataProvider withUrl(String url) {
        this.url = url;
        return this;
    }

    public DataProvider withResources(ProviderResource resource) {
        this.addResource(resource);
        return this;
    }

    public String generateHrn() {
        return "hrn:" + NS_Config.REALM.toString() + ":neutral::" + PROVIDER.getUser().getRealm() + ":" + name;
    }

}
