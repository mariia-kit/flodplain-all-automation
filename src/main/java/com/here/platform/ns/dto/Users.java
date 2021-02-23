package com.here.platform.ns.dto;

import com.here.platform.common.config.Conf;
import com.here.platform.ns.helpers.authentication.AuthController;
import java.util.Base64;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;


@AllArgsConstructor
public enum Users {

    PROVIDER(Conf.nsUsers().getProvider()),
    CONSUMER(Conf.nsUsers().getConsumer()),
    EXTERNAL_USER(Conf.nsUsers().getNonConsumerManager()),
    APPLICATION(new User("Application", StringUtils.EMPTY).withUserType(UserType_NS.APP)),
    AAA(new User("AAA Connector " + System.getProperty("env"), StringUtils.EMPTY).withUserType(UserType_NS.AA)),
    DAIMLER(new User(
            new String(Base64.getDecoder().decode(Conf.nsUsers().getDaimlerUser().getEmail())),
            new String(Base64.getDecoder().decode(Conf.nsUsers().getDaimlerUser().getPass())))
            .withUserType(UserType_NS.DAIMLER)),
    MP_CONSUMER(Conf.mpUsers().getMpConsumer()),
    MP_PROVIDER(Conf.mpUsers().getMpProvider()),
    HERE_USER(new User(
            Conf.nsUsers().getHereUser().getEmail(),
            Conf.nsUsers().getHereUser().getPass(),
            "HERE",
            StringUtils.EMPTY)
            .withUserType(UserType_NS.CM)),
    CM_CONSUMER(new User("CmToken_" + System.getProperty("env"), StringUtils.EMPTY).withUserType(UserType_NS.CMCONS)),
    PROXY_ADMIN(new User("proxy_admin_" + System.getProperty("env"), StringUtils.EMPTY).withUserType(UserType_NS.PROXY_ADM)),
    PROXY_APP(new User("proxy_app_" + System.getProperty("env"), StringUtils.EMPTY).withUserType(UserType_NS.PROXY_APP));

    private final User user;

    public User getUser() {
        return user;
    }

    public String getToken() {
        return user.getToken();
    }

}
