package com.here.platform.ns.dto;

import static com.here.platform.ns.dto.Users.MP_PROVIDER;

import com.here.platform.common.config.Conf;
import io.qameta.allure.Step;
import java.beans.ConstructorProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@EqualsAndHashCode
public class Container {

    private String id;
    private String dataProviderId;
    private String name;
    private String description;
    private String resourceNames;
    private Boolean consentRequired;
    private String scope;


    @ConstructorProperties({"id", "name", "dataProviderId", "description", "resourceNames",
            "consentRequired", "scope"})
    public Container(String id, String name, String dataProviderId, String description,
            String resourceNames, Boolean consentRequired, String scope) {
        this.dataProviderId = dataProviderId;
        this.name = name;
        this.id = id;
        this.description = description;
        this.resourceNames = resourceNames;
        this.consentRequired = consentRequired;
        this.scope = scope;
    }

    public String generateHrn(String envHrn, String realm) {
        return String.format("hrn:%s:neutral::%s:%s/containers/%s",
                envHrn,
                realm,
                getDataProviderId(),
                getId());
    }

    public String generateHrn() {
        return generateHrn(Conf.ns().getRealm(), MP_PROVIDER.getUser().getRealm());
    }

    public String generateScope() {
        if (this.getDataProviderId().toLowerCase().equals("daimler") || this.getDataProviderId()
                .equals(Providers.DAIMLER_EXPERIMENTAL.getName())) {
            return "mb:user:pool:reader mb:vehicle:status:general offline_access";
        } else {
            return getId() + ":" + getResourceNames();
        }
    }

    public Map<String, Object> generateContainerBody() {
        Map<String, Object> containerBody = new HashMap<>();
        if (this.getName() != null) {
            containerBody.put("name", this.getName());
        }
        if (this.getDescription() != null) {
            containerBody.put("description", this.getDescription());
        }
        if (this.getResourceNames() != null) {
            containerBody.put("resourceNames", this.getResourceNames());
        }
        if (this.getConsentRequired() != null) {
            containerBody.put("consentRequired", this.getConsentRequired());
        }
        if (this.getScope() != null) {
            containerBody.put("scope", this.getScope());
        }
        return containerBody;
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

    @Step("Set Data Provider id value for container to '{0}'")
    public Container withDataProviderId(String dataProviderId) {
        this.dataProviderId = dataProviderId;
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
        return new Container(id, name, dataProviderId, description, resourceNames, consentRequired, scope);
    }

    @Override
    public String toString() {
        return String.format(
                "ContainerInfo(id=%s, name=%s, dataProviderName=%s, description=%s, resourceNames=%s, consentRequired=%s, scope=%s)",
                id, name, dataProviderId, description, resourceNames, consentRequired, scope);
    }

    public DataProvider getDataProviderByName() {
        return Arrays.stream(Providers.values()).filter(p -> p.getProvider().getId().equals(this.dataProviderId))
                .findFirst().get()
                .getProvider();
    }

}
