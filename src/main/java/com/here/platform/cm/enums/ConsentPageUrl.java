package com.here.platform.cm.enums;

import com.here.platform.common.EnumByEnv;
import lombok.AllArgsConstructor;


@AllArgsConstructor
public enum ConsentPageUrl {

    LOCAL("http://localhost:8080/"),
    DEV("https://dev-web.consent.api.platform.in.here.com/"),
    SIT("https://sit-web.consent.api.platform.in.here.com/"),
    PROD("https://web.consent.api.platform.here.com/");

    private final String envUrl;

    public static String getEnvUrl() {
        return getEnvUrlRoot() + "requests/";
    }

    public static String getEnvUrlRoot() {
        return EnumByEnv.get(ConsentPageUrl.class).envUrl;
    }

}
