package com.here.platform.ns.dto;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;


@Getter
@Setter
public class User {

    private String email;
    private String token;
    private String refreshToken;
    private String pass;
    private String realm;
    private String userId;
    private UserType_NS type;
    private String clientId;
    private String clientSecret;

    public User(String email, String pass, String realm, String userId) {
        this.email = email;
        this.pass = pass;
        this.realm = realm;
        this.userId = userId;
    }

    public User(String email, String pass) {
        this.email = email;
        this.pass = pass;
        this.realm = StringUtils.EMPTY;
        this.userId = StringUtils.EMPTY;
    }

    public User withUserType(UserType_NS userType) {
        this.setType(userType);
        return this;
    }

    public User withToken(String tokenValue) {
        this.setToken(tokenValue);
        return this;
    }
}
