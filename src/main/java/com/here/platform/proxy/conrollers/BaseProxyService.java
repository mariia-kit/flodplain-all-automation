package com.here.platform.proxy.conrollers;

import static com.here.platform.common.strings.SBB.sbb;
import static io.restassured.RestAssured.given;
import static io.restassured.config.HeaderConfig.headerConfig;

import com.here.platform.common.config.Conf;
import com.here.platform.ns.dto.Users;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.springframework.util.StringUtils;


public class BaseProxyService<T> {

    private String authorizationToken = "";

    protected RequestSpecification consentServiceClient(final String targetPath) {
        var baseService = given()
                .config(RestAssuredConfig.config()
                        .headerConfig(headerConfig().overwriteHeadersWithName("Authorization", "Content-Type")))
                .baseUri(Conf.proxy().getHost())
                .basePath(targetPath)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .filters(new AllureRestAssured());

        if (!StringUtils.isEmpty(authorizationToken)) {
            baseService.header("Authorization", authorizationToken);
        }

        return baseService;
    }

    private void setAuthorizationToken(String tokenValue) {
        this.authorizationToken = sbb("Bearer").w().append(tokenValue).bld();
    }

    public T withConsumerToken() {
        setAuthorizationToken(Users.MP_CONSUMER.getToken());
        return (T) this;
    }

    public T withAppToken() {
        String token = Users.PROXY_ADMIN.getToken();
        setAuthorizationToken(token);
        return (T) this;
    }

    public T withToken(String authorizationTokenValue) {
        setAuthorizationToken(authorizationTokenValue);
        return (T) this;
    }

}
