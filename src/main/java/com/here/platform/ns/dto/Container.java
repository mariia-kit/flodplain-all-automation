package com.here.platform.ns.dto;

import static com.here.platform.ns.dto.Users.MP_PROVIDER;

import com.here.platform.common.config.Conf;
import io.qameta.allure.Step;
import java.beans.ConstructorProperties;
import java.util.Arrays;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;


@Getter
@Setter
@EqualsAndHashCode
public class Container {

    private String id;
    private String name;
    private String dataProviderName;
    private String description;
    private String resourceNames;
    private Boolean consentRequired;
    private String scope;


    @ConstructorProperties({"id", "name", "dataProviderName", "description", "resourceNames",
            "consentRequired", "scope"})
    public Container(String id, String name, String dataProviderName, String description,
            String resourceNames, Boolean consentRequired, String scope) {
        this.name = name;
        this.id = id;
        this.dataProviderName = dataProviderName;
        this.description = description;
        this.resourceNames = resourceNames;
        this.consentRequired = consentRequired;
        this.scope = scope;
    }

    public String generateHrn(String envHrn, String realm) {
        return String.format("hrn:%s:neutral::%s:%s/containers/%s",
                envHrn,
                realm,
                getDataProviderName(),
                getId());
    }

    public String generateHrn() {
        return generateHrn(Conf.ns().getRealm(), MP_PROVIDER.getUser().getRealm());
    }

    public String generateScope() {
        if (this.getDataProviderName().toLowerCase().equals("daimler") || this.getDataProviderName()
                .equals(Providers.DAIMLER_EXPERIMENTAL.getName())) {
            return "mb:user:pool:reader mb:vehicle:status:general offline_access";
        } else {
            return getId() + ":" + getResourceNames();
        }
    }

    public String generateBody() {
        JSONObject object = new JSONObject();
        try {
            if (this.getId() != null) {
                object.put("name", this.getName());
            }
            if (this.getDescription() != null) {
                object.put("description", this.getDescription());
            }
            if (this.getResourceNames() != null) {
                object.put("resourceNames", this.getResourceNames());
            }
            if (this.getConsentRequired() != null) {
                object.put("consentRequired", this.getConsentRequired());
            }
            if (this.getScope() != null) {
                object.put("scope", this.getScope());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    public String generateFullBody() {
        JSONObject object = new JSONObject();
        try {
            if (this.getId() != null) {
                object.put("id", this.getId());
            }
            if (this.getDescription() != null) {
                object.put("description", this.getDescription());
            }
            if (this.getResourceNames() != null) {
                object.put("resourceNames", this.getResourceNames());
            }
            if (this.getConsentRequired() != null) {
                object.put("consentRequired", this.getConsentRequired());
            }
            if (this.getDataProviderName() != null) {
                object.put("dataProviderName", this.getDataProviderName());
            }
            if (this.getName() != null) {
                object.put("name", this.getName());
            }
            if (this.getScope() != null) {
                object.put("scope", this.getScope());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    @Step("Set id value for container to '{0}'")
    public Container withId(String id) {
        this.id = id;
        setScope(generateScope());
        return this;
    }

    @Step("Set name value for container to '{0}'")
    public Container withName(String name) {
        this.name = name;
        return this;
    }

    @Step("Set Data Provider name value for container to '{0}'")
    public Container withDataProviderName(String dataProviderName) {
        this.dataProviderName = dataProviderName;
        return this;
    }

    @Step("Set description value for container to '{0}'")
    public Container withDescription(String description) {
        this.description = description;
        return this;
    }

    @Step("Set resource names values for container to '{0}'")
    public Container withResourceNames(String resourceNames) {
        this.resourceNames = resourceNames;
        setScope(generateScope());
        return this;
    }

    @Step("Set is Consent Required value for container to '{0}'")
    public Container withConsentRequired(Boolean isRequired) {
        this.consentRequired = isRequired;
        return this;
    }

    @Step("Set Scope value for container to '{0}'")
    public Container withScope(String scope) {
        this.scope = scope;
        return this;
    }

    public Container clone() {
        return new Container(id, name, dataProviderName, description, resourceNames, consentRequired, scope);
    }

    @Override
    public String toString() {
        return String.format(
                "ContainerInfo(id=%s, name=%s, dataProviderName=%s, description=%s, resourceNames=%s, consentRequired=%s, scope=%s)",
                id, name, dataProviderName, description, resourceNames, consentRequired, scope);
    }

    public DataProvider getDataProviderByName() {
        return Arrays.stream(Providers.values()).filter(p -> p.getProvider().getName().equals(this.dataProviderName))
                .findFirst().get()
                .getProvider();
    }

}
