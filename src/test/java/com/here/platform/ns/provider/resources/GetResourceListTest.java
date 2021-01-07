package com.here.platform.ns.provider.resources;

import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.controllers.provider.ProviderController;
import com.here.platform.ns.controllers.provider.ResourceController;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.ProviderResource;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.ns.helpers.DefaultResponses;
import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("NS-Resources")
@DisplayName("Verify get all Resources for DataProvider")
public class GetResourceListTest extends BaseNSTest {

    @Test
    @DisplayName("Verify receive Resources list of DataProvider Successful")
    void verifyGetResourceListCanBeRetrieved() {
        DataProvider provider = Providers.generateNew();
        var addDataProvider = new ProviderController()
                .withToken(PROVIDER)
                .addProvider(provider);
        new NeutralServerResponseAssertion(addDataProvider)
                .expectedCode(HttpStatus.SC_OK);

        ProviderResource res = ProviderResource.generateNew();
        ProviderResource res1 = ProviderResource.generateNew();
        var addResource1 = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider, res);
        new NeutralServerResponseAssertion(addResource1)
                .expectedCode(HttpStatus.SC_OK);
        var addResource2 = new ResourceController()
                .withToken(PROVIDER)
                .addResource(provider, res1);
        new NeutralServerResponseAssertion(addResource2)
                .expectedCode(HttpStatus.SC_OK);

        var getResource = new ResourceController()
                .withToken(PROVIDER)
                .getResourceList(provider);
        new NeutralServerResponseAssertion(getResource)
                .expectedCode(HttpStatus.SC_OK)
                .expected(response -> !DefaultResponses.isResponseListEmpty(response),
                        "Expected list should not be empty!")
                .expected(response -> DefaultResponses
                                .isResourceInList(res, response),
                        "No expected resource in result!")
                .expected(response -> DefaultResponses
                                .isResourceInList(res1, response),
                        "No expected resource in result!");
    }

    @Test
    @DisplayName("Verify receive Resources list of DataProvider with empty Token")
    void verifyGetResourceListWithEmptyToken() {
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
                .expectedCode(HttpStatus.SC_UNAUTHORIZED)
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());
    }

    @Test
    @DisplayName("Verify receive Resources list of DataProvider with invalid Token")
    void verifyGetResourceListWithWrongToken() {
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
                .withToken("Bearer 12345")
                .getResource(provider, res.getName());
        new NeutralServerResponseAssertion(getResource)
                .expectedSentryError(SentryErrorsList.TOKEN_CORRUPTED.getError());
    }

}
