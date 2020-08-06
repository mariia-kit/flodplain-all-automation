package com.here.platform.ns.provider.resources;

import static com.here.platform.ns.dto.Users.EXTERNAL_USER;
import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.controllers.provider.ProviderController;
import com.here.platform.ns.controllers.provider.ResourceController;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.ProviderResource;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.ns.helpers.NSErrors;
import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("Verify receive Resource for Data Provider data")
public class GetResourceTest extends BaseNSTest {

    @Test
    @DisplayName("Verify receive Resource data Successful")
    void verifyGetContainersDataRetrieved() {
        DataProvider provider = Providers.generateNew();
        var addDataProvider = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(addDataProvider)
                .expectedCode(HttpStatus.SC_OK);

        ProviderResource res = ProviderResource.generateNew();
        var addResource = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider, res);
        new NeutralServerResponseAssertion(addResource)
                .expectedCode(HttpStatus.SC_OK);

        var getResource = new ResourceController()
                .withToken(PROVIDER)
                .getResource(provider, res.getName());
        new NeutralServerResponseAssertion(getResource)
                .expectedCode(HttpStatus.SC_OK)
                .expectedEquals("name", res.getName(), "Provider resource not as expected!");
    }

    @Test
    @DisplayName("Verify receive Resource data with no Token")
    void verifyGetContainersNoToken() {
        DataProvider provider = Providers.generateNew();
        var addDataProvider = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(addDataProvider)
                .expectedCode(HttpStatus.SC_OK);

        ProviderResource res = ProviderResource.generateNew();
        var addResource = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider, res);
        new NeutralServerResponseAssertion(addResource)
                .expectedCode(HttpStatus.SC_OK);

        var getResource = new ResourceController()
                .withToken(StringUtils.EMPTY)
                .getResource(provider, res.getName());
        new NeutralServerResponseAssertion(getResource)
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());
    }

    @Test
    @DisplayName("Verify receive Resource data with invalid Token")
    void verifyGetContainersInvalidToken() {
        DataProvider provider = Providers.generateNew();
        var addDataProvider = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(addDataProvider)
                .expectedCode(HttpStatus.SC_OK);

        ProviderResource res = ProviderResource.generateNew();
        var addResource = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider, res);
        new NeutralServerResponseAssertion(addResource)
                .expectedCode(HttpStatus.SC_OK);

        var getResource = new ResourceController()
                .withToken(EXTERNAL_USER)
                .getResource(provider, res.getName());
        new NeutralServerResponseAssertion(getResource)
                .expectedSentryError(SentryErrorsList.TOKEN_INVALID.getError());
    }

    @Test
    @DisplayName("Verify receive Resource data already Deleted")
    void verifyGetContainersDataAlreadyDeleted() {
        DataProvider provider = Providers.generateNew();
        var addDataProvider = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(addDataProvider)
                .expectedCode(HttpStatus.SC_OK);

        ProviderResource res = ProviderResource.generateNew();
        var addResource = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider, res);
        new NeutralServerResponseAssertion(addResource)
                .expectedCode(HttpStatus.SC_OK);

        var delete = new ResourceController()
                .withToken(PROVIDER)
                .deleteResource(provider, res);
        new NeutralServerResponseAssertion(delete)
                .expectedCode(HttpStatus.SC_NO_CONTENT);

        var getResource = new ResourceController()
                .withToken(PROVIDER)
                .getResource(provider, res.getName());
        new NeutralServerResponseAssertion(getResource)
                .expectedError(NSErrors.getProviderResourceNotFoundError(provider.getName(),
                        res.getName()));
    }

    @Test
    @DisplayName("Verify receive Resource data with not valid Provider")
    void verifyGetContainersDataNoProvider() {
        DataProvider provider = Providers.generateNew();
        var addDataProvider = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(addDataProvider)
                .expectedCode(HttpStatus.SC_OK);

        ProviderResource res = ProviderResource.generateNew();
        var addResource = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider, res);
        new NeutralServerResponseAssertion(addResource)
                .expectedCode(HttpStatus.SC_OK);
        provider.setName("no_such_provider");
        var getResource = new ResourceController()
                .withToken(PROVIDER)
                .getResource(provider, res.getName());
        new NeutralServerResponseAssertion(getResource)
                .expectedError(NSErrors.getProviderNotFoundError(provider));
    }

    @Test
    @DisplayName("Verify receive Resource data with not valid resource name")
    void verifyGetContainersDataNoContainer() {
        DataProvider provider = Providers.generateNew();
        var addDataProvider = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(addDataProvider)
                .expectedCode(HttpStatus.SC_OK);

        ProviderResource res = ProviderResource.generateNew();
        var addResource = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider, res);
        new NeutralServerResponseAssertion(addResource)
                .expectedCode(HttpStatus.SC_OK);

        var getResource = new ResourceController()
                .withToken(PROVIDER)
                .getResource(provider, "no_such_res");
        new NeutralServerResponseAssertion(getResource)
                .expectedError(NSErrors.getProviderResourceNotFoundError(provider.getName(),
                        "no_such_res"));
    }

}
