package com.here.platform.ns.restEndPoints.provider.container_info;

import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.dto.CallsUrl;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.helpers.resthelper.RestHelper;
import com.here.platform.ns.restEndPoints.BaseRestControllerNS;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import java.util.function.Supplier;


public class AddContainerCall extends BaseRestControllerNS<AddContainerCall> {

    private String body;

    public AddContainerCall(Container container) {
        this.body = container.generateBody();
        callMessage = String
                .format("Perform call to create new Container for provider '%s' with name '%s'",
                        container.getDataProviderName(), container.getName());
        setDefaultUser(PROVIDER);
        endpointUrl = CallsUrl.ADD_CONTAINER.builder()
                .withContainerName(container.getId())
                .withProviderName(container.getDataProviderName())
                .getUrl();
    }

    @Step("Use Body {body}")
    public AddContainerCall withBody(String body) {
        this.body = body;
        return this;
    }

    @Override
    public Supplier<Response> defineCall() {
        return () -> RestHelper.put(this, body);
    }

}
