package com.here.platform.cm.enums;

import com.here.platform.common.config.Conf;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.ToString;


@ToString
@Getter
public enum MPProviders {

    DAIMLER_EXPERIMENTAL(
            "daimler_experimental", 18, "daimler_experimental",
            "https://id.mercedes-benz.com/as/authorization.oauth2",
            "https://id.mercedes-benz.com/as/token.oauth2"
    ),
    DAIMLER(
            "daimler", 17, "daimler",
            "https://id.mercedes-benz.com/as/authorization.oauth2", ""
    ),
    DAIMLER_REFERENCE(
            Conf.cm().getReferenceProviderName(), 17, "test-daimler",
            Conf.ns().getReferenceProviderAuthUrl(),
            Conf.ns().getReferenceProviderTokenUrl()
    ),
    BMW_TEST("test-bmw", 17, "test-bmw", "", ""),
    BMW("bmw", 17, "bmw", "", ""),
    REFERENCE(
            "exelsior", 17, "test-daimler",
            Conf.ns().getReferenceProviderAuthUrl(),
            Conf.ns().getReferenceProviderTokenUrl()
    ),
    REFERENCE_PROD(
            "reference_provider", 17, "test-daimler",
            Conf.ns().getReferenceProviderAuthUrl(),
            Conf.ns().getReferenceProviderTokenUrl()
    );;

    public final int vinLength;
    private final String
            name,
            type,
            authUrl,
            tokenUrl;

    MPProviders(String name, int vinLength, String type, String authUrl, String tokenUrl) {
        this.name = name;
        this.vinLength = vinLength;
        this.type = type;
        this.authUrl = authUrl;
        this.tokenUrl = tokenUrl;
    }

    public static MPProviders findByProviderId(String providerId) {
        return Stream.of(MPProviders.values())
                .filter(prov -> prov.getName().equals(providerId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No such provider defined:" + providerId));
    }

}
