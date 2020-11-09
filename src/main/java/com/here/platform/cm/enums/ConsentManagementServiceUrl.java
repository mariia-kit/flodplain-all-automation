package com.here.platform.cm.enums;

import static com.here.platform.common.strings.SBB.sbb;

import com.here.platform.common.config.Conf;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;


@AllArgsConstructor
public class ConsentManagementServiceUrl {

    private static final String BASE_PATH = "consent-service/v1/";

    public static String getEnvUrl() {
        String host;
        var dynamicEnvUrl = System.getProperty("dynamicUrl");
        if (StringUtils.isNotBlank(dynamicEnvUrl)) {
            host = sbb("https://").append(dynamicEnvUrl).bld();
        } else {
            host = Conf.cm().getServiceUrl();
        }
        return UriComponentsBuilder.fromUriString(host).path(BASE_PATH).toUriString();
    }

}
