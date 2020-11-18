package com.here.platform.cm.enums;

import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

import com.here.platform.common.config.Conf;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;


@UtilityClass
public class ConsentPageUrl {

    public String getConsentRequestsUrl() {
        return fromUriString(getEnvUrlRoot()).path("/requests/").toUriString();
    }

    public String getEnvUrlRoot() {
        var dynamicEnvUrl = System.getProperty("dynamicUrl");
        if (StringUtils.isNotBlank(dynamicEnvUrl)) {
            return Conf.cm().getConsentPageUrlDynamic();
        } else {
            return Conf.cm().getConsentPageUrl();
        }
    }

    public String getDaimlerCallbackUrl() {
        return fromUriString(getEnvUrlRoot()).path("/oauth2/daimler/auth/callback").toUriString();
    }

    public String getStaticPurposePageLinkFor(String consumerId, String containerId) {
        return fromUriString(getEnvUrlRoot()).path("purpose/info")
                .queryParam("consumerId", consumerId)
                .queryParam("containerId", containerId)
                .toUriString();
    }

    public String getAcceptedOffersUrl() {
        return fromUriString(getEnvUrlRoot()).path("/offers").queryParam("type", "accepted").toUriString();
    }

}
