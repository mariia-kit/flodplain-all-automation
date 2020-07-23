package com.here.platform.ns.dto;

import com.here.platform.ns.helpers.authentication.AuthController;
import com.here.platform.ns.utils.NS_Config;
import com.here.platform.ns.utils.PropertiesLoader;

import java.util.Base64;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;


@AllArgsConstructor
public enum Users {

    PROVIDER(PropertiesLoader.getInstance().loadUser(UserType_NS.NS, "provider")),
    CONSUMER(PropertiesLoader.getInstance().loadUser(UserType_NS.NS, "consumer")),
    EXTERNAL_USER(PropertiesLoader.getInstance().loadUser(UserType_NS.NS, "non-consumer-manager")
            .withToken(NS_Config.EXTERNAL_USER_TOKEN.toString())),
    APPLICATION(new User("Application", StringUtils.EMPTY).withUserType(UserType_NS.APP)),
    AAA(new User("AAA Connector", StringUtils.EMPTY).withUserType(UserType_NS.AA)),
    DAIMLER(new User(new String(Base64.getDecoder().decode(NS_Config.DAIMLER_API_LOGIN.toString())),
            new String(Base64.getDecoder().decode(NS_Config.DAIMLER_API_PASS.toString()))).withUserType(UserType_NS.DAIMLER)),
    MP_CONSUMER(PropertiesLoader.getInstance().loadUser(UserType_NS.MP, "consumer")),
    MP_PROVIDER(PropertiesLoader.getInstance().loadUser(UserType_NS.MP, "provider")),
    HERE_USER(new User(NS_Config.CM_HERE_LOGIN.toString(), NS_Config.CM_HERE_PASS.toString(), "HERE", StringUtils.EMPTY).withUserType(UserType_NS.CM)),
    CM_CONSUMER(PropertiesLoader.getInstance().loadUser(UserType_NS.MP, "consumer"));

    private User user;

    public User getUser() {
        if (StringUtils.isEmpty(user.getToken())) {
            AuthController.setUserToken(user);
        }
        return user;
    }

    public String getToken() {
        if (StringUtils.isEmpty(user.getToken())) {
            AuthController.setUserToken(user);
        }
        return user.getToken();
    }

}
