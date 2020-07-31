package com.here.platform.cm.enums;

import com.here.platform.common.EnumByEnv;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;


@AllArgsConstructor
public enum ConsentManagementServiceUrl {

    LOCAL("http://localhost:8080"),
    DEV("https://dev.consent.api.platform.in.here.com"),
    SIT("https://sit.consent.api.platform.in.here.com"),
    STG(SIT.envUrl),
    PROD("https://consent.api.platform.here.com");

    private static final String BASE_PATH = "consent-service/v1/";
    private final String envUrl;

    public static String getEnvUrl() {
        String host;
        var dynamicEnvUrl = System.getProperty("dynamicUrl");
        if (StringUtils.isNotBlank(dynamicEnvUrl)) {
            host = String.format("https://%s", dynamicEnvUrl);
        } else {
            host = EnumByEnv.get(ConsentManagementServiceUrl.class).envUrl;
        }
        return UriComponentsBuilder.fromUriString(host).path(BASE_PATH).toUriString();
    }

}
