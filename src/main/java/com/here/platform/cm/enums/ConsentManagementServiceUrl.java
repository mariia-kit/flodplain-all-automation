package com.here.platform.cm.enums;

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
            host = String.format("https://%s", dynamicEnvUrl);
        } else {
            host = Conf.cm().getServiceUrl();
        }
        return UriComponentsBuilder.fromUriString(host).path(BASE_PATH).toUriString();
    }

}
