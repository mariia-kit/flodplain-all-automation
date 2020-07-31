package com.here.platform.cm.enums;

import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

import com.here.platform.common.EnumByEnv;
import lombok.AllArgsConstructor;


@AllArgsConstructor
public enum ConsentPageUrl {

    LOCAL("http://localhost:8080/consent/"),
    DEV("https://portal.platform.in.here.com/consent/"),
    SIT("https://sit-web.consent.api.platform.in.here.com/"),
    STG(SIT.envUrl),
    PROD("https://web.consent.api.platform.here.com/");

    private final String envUrl;

    public static String getConsentRequestsUrl() {
        return fromUriString(getEnvUrlRoot()).path("/requests/").toUriString();
    }

    public static String getEnvUrlRoot() {
        return EnumByEnv.get(ConsentPageUrl.class).envUrl;
    }

    public static String getDaimlerCallbackUrl() {
        return fromUriString(getEnvUrlRoot()).path("/oauth2/daimler/auth/callback").toUriString();
    }

    public static String getAcceptedOffersUrl() {
        return fromUriString(getEnvUrlRoot()).path("offers#accepted").toUriString();
    }

}
