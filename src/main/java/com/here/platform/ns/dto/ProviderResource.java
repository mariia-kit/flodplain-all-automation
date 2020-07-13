package com.here.platform.ns.dto;

import static com.here.platform.ns.dto.Users.MP_PROVIDER;

import com.here.platform.ns.helpers.LoggerHelper;
import com.here.platform.ns.helpers.UniqueId;
import com.here.platform.ns.utils.NS_Config;
import java.beans.ConstructorProperties;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ProviderResource implements Cloneable {

    private String name;


    @ConstructorProperties({"name"})
    public ProviderResource(String name) {
        this.name = name;
    }

    public static String getResNamePrefix() {
        return "AutomatedTestResource";
    }

    public static ProviderResource generateNew() {
        ProviderResource res = new ProviderResource(
                getResNamePrefix() + UniqueId.getUniqueKey());
        LoggerHelper.logStep("Generate new Resource:" + res.toString());
        return res;
    }

    public String generateBody() {
        return "{\n"
                + "  \"name\": \"" + name + "\"\n"
                + "}";
    }

    @Override
    public String toString() {
        return String.format(
                "ProviderResource(name=%s", name);
    }

    public String generateHrn(String providerName) {
        return String
                .format("hrn:%s:neutral::%s:%s/resources/%s", NS_Config.REALM.toString(),
                        MP_PROVIDER.getUser().getRealm(),
                        providerName,
                        getName());
    }

    public String generateGeneralResourceHrn(String providerName) {
        return String
                .format("hrn:%s:neutral::%s:%s/resources", NS_Config.REALM.toString(),
                        MP_PROVIDER.getUser().getRealm(),
                        providerName);
    }

    public ProviderResource clone() {
        return new ProviderResource(this.getName());
    }

}
