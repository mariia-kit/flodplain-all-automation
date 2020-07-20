package com.here.platform.ns.restEndPoints;

import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import io.qameta.allure.Step;
import io.restassured.http.Header;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;


@Getter
@Setter
public abstract class BaseRestControllerNS<T extends BaseRestControllerNS<?>> {

    protected String endpointUrl;
    protected String callMessage;

    protected Users defaultUser;

    private String token;
    private List<Header> headers = new ArrayList<>();
    private String queryParams;

    @Step("Auth with token '{token}'")
    public T withToken(String token) {
        this.token = token;
        return (T) this;
    }

    @Step("Auth with token for {user.email}")
    public T withToken(User user) {
        this.token = user.getToken();
        return (T) this;
    }

    @Step("Auth with token '{user}'")
    public T withToken(Users user) {
        this.token = user.getToken();
        return (T) this;
    }

    @Step("Perform call with additional header '{header.name} - {header.value}'")
    public T withHeader(Header header) {
        this.headers.add(header);
        return (T) this;
    }

    @Step("Perform call with additional header '{headerName} - {headerValue}'")
    public T withHeader(String headerName, String headerValue) {
        this.headers.add(new Header(headerName, headerValue));
        return (T) this;
    }

    private void initDefaultToken() {
        if (getToken() == null) {
            this.token = defaultUser.getToken();
        }
        //TODO: remove after refactor to new controller
        if (!token.contains("Bearer") && !token.isEmpty()) {
            token = "Bearer " + token;
        }

    }

    @Step("Use query parameters {query}")
    public T withQueryParam(String query) {
        this.queryParams = query;
        return (T) this;
    }

    public String getQueryParams() {
        return queryParams == null ? StringUtils.EMPTY : "?" + queryParams;
    }


    public NeutralServerResponseAssertion call() {
        initDefaultToken();
        Response response = defineCall().get();

        return new NeutralServerResponseAssertion(response);
    }

    abstract public Supplier<Response> defineCall();

}
