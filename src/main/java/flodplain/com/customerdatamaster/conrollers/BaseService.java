package flodplain.com.customerdatamaster.conrollers;

import static flodplain.com.common.strings.SBB.sbb;
import static io.restassured.RestAssured.given;
import static io.restassured.config.HeaderConfig.headerConfig;

import flodplain.com.common.config.Conf;
import flodplain.com.customerdatamaster.dto.UserEnum;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.springframework.util.StringUtils;


public class BaseService<T> {

    private String authorizationToken = "";

    protected RequestSpecification serviceClient(final String targetPath) {
        var baseService = given()
                .config(RestAssuredConfig.config()
                        .headerConfig(headerConfig().overwriteHeadersWithName("Authorization", "Content-Type")))
                .baseUri(Conf.ns().getHost())
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

    public T withToken(String authorizationTokenValue) {
        setAuthorizationToken(authorizationTokenValue);
        return (T) this;
    }

    public T withMerchantToken() {
        setAuthorizationToken(UserEnum.WEB.getToken());
        return (T) this;
    }

    public T withAdminToken(String token) {
        setAuthorizationToken(token);
        return (T) this;
    }

    public T withJwtToken(String authorizationTokenValue) {
        setAuthorizationToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJHTFVVIiwiaWF0IjoxNjI3OTc3NjAwLCJleHAiOjQ3ODM2NTEyMDAsImF1ZCI6Ind3dy5mbG9kcGxhaW4uY29tIiwic3ViIjoiIiwidXNlcl9pZCI6IjEiLCJpbnN0aXR1dGlvbl9pZCI6IjIiLCJzbWVfaWQiOiIyIiwicm9sZXMiOlsiTWVyY2hhbnQiLCJMZWFkIl19.nK2maiz0HVHqw1h0tx0Msrrv8th5b-TDvDBwofaNCU4");
        return (T) this;
    }

}
