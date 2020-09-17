package com.here.platform.cm.enums;

import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

import com.here.platform.common.config.Conf;
import lombok.AllArgsConstructor;


@AllArgsConstructor
public class ConsentPageUrl {

    public static String getConsentRequestsUrl() {
        return fromUriString(getEnvUrlRoot()).path("/requests/").toUriString();
    }

    public static String getEnvUrlRoot() {
        return Conf.cm().getConsentPageUrl();
    }

    public static String getDaimlerCallbackUrl() {
        return fromUriString(getEnvUrlRoot()).path("/oauth2/daimler/auth/callback").toUriString();
    }

    public static String getAcceptedOffersUrl() {
        return getEnvUrlRoot() + "offers#accepted";
    }

}
