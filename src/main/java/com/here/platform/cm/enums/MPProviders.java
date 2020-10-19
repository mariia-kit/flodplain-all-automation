package com.here.platform.cm.enums;

import com.here.platform.common.config.Conf;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;


@AllArgsConstructor
public enum MPProviders {

    DAIMLER_EXPERIMENTAL("daimler_experimental", 18, "daimler_experimental", "https://id.mercedes-benz.com/as/authorization.oauth2", "https://id.mercedes-benz.com/as/token.oauth2"),
    DAIMLER("daimler", 17, "daimler", "https://id.mercedes-benz.com/as/authorization.oauth2", ""),
    DAIMLER_EXPERIMENTAL_REFERENCE("daimleR_experimental", 18, "daimler_experimental", Conf.ns().getRefProviderUrl() + "/auth/oauth/v2/authorize", Conf.ns().getRefProviderUrl() + "/auth/oauth/v2/token"),
    DAIMLER_REFERENCE("daimleR", 17, "daimler", Conf.ns().getRefProviderUrl() + "/auth/oauth/v2/authorize", Conf.ns().getRefProviderUrl() + "/auth/oauth/v2/token"),
    BMW_TEST("test-bmw", 17, "test-bmw", "", ""),
    BMW("bmw", 17, "bmw", "", "");

    private final String name;
    public int vinLength;
    private final String type;
    private final String authUrl;
    private final String tokenUrl;

    public String getName() {
        return name;
    }
    public String getType() {
        return type;
    }
    public String getAuthUrl() {
        return authUrl;
    }
    public String getTokenUrl() {
        return tokenUrl;
    }

    public static MPProviders findByProviderId(String providerId) {
        return Stream.of(MPProviders.values())
                .filter(prov -> prov.getName().equals(providerId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No such provider defined:" + providerId));
    }

}
