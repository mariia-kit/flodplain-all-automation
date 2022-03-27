package flodplain.com.web.enums;

import static org.springframework.web.util.UriComponentsBuilder.fromUriString;


import flodplain.com.common.config.Conf;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;


@UtilityClass
public class AccountPageUrl {

    public String getAccountsUrl() {
        return fromUriString(getEnvUrlRoot()).path("/accounts/").toUriString();
    }

    public String getAccountUrl() {
        return "https://keycloak-internal.stage.flodplains.com/auth/realms/g2g-web/";
    }

    public String getLoginUrl() {
        return "https://keycloak-internal.stage.flodplains.com/auth/realms/g2g-web/";
    }

    public String getEnvUrlRoot() {
        var dynamicEnvUrl = System.getProperty("dynamicUrl");
        if (StringUtils.isNotBlank(dynamicEnvUrl)) {
            return Conf.ns().getAuthUrlBase();
        } else {
            return Conf.ns().getAuthUrlBase();
        }
    }


}
