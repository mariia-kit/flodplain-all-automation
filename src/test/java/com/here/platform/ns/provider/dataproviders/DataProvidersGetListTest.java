package com.here.platform.ns.provider.dataproviders;

import static com.here.platform.ns.dto.Users.EXTERNAL_USER;
import static com.here.platform.ns.dto.Users.PROVIDER;

import com.here.platform.ns.BaseNSTest;
import com.here.platform.ns.controllers.provider.ProviderController;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.Providers;
import com.here.platform.ns.dto.SentryErrorsList;
import com.here.platform.ns.helpers.DefaultResponses;
import com.here.platform.ns.helpers.Steps;
import com.here.platform.ns.restEndPoints.NeutralServerResponseAssertion;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@DisplayName("Verify receive list of DataProviders ")
class DataProvidersGetListTest extends BaseNSTest {

    @Test
    @DisplayName("Verify receive list of DataProviders Successful")
    void verifyGetDataProvidersListCanBeRetrieved() {
        var verify = new ProviderController()
                .withToken(PROVIDER)
                .getProviderList();
        new NeutralServerResponseAssertion(verify)
                .expectedCode(HttpStatus.SC_OK)
                .expected(res -> !DefaultResponses.isResponseListEmpty(res),
                        "Expected list should not be empty!");
    }

    @Test
    @DisplayName("Verify receive list of DataProviders new Successful")
    @Tag("smoke_ns")
    void verifyGetDataProvidersListCanBeRetrievedNewOne() {
        DataProvider provider = Providers.generateNew();
        Steps.createRegularProvider(provider);

        var verify = new ProviderController()
                .withToken(PROVIDER)
                .getProviderList();
        new NeutralServerResponseAssertion(verify)
                .expectedCode(HttpStatus.SC_OK)
                .expected(res -> !DefaultResponses.isResponseListEmpty(res),
                        "Expected list should not be empty!")
                .expected(res -> DefaultResponses.isDataProviderPresentInList(provider, res),
                        "No expected container in result!");
    }

    @Test
    @DisplayName("Verify receive list of DataProviders with empty Token")
    void verifyGetDataProvidersListWithEmptyToken() {
        var verify = new ProviderController()
                .withToken(StringUtils.EMPTY)
                .getProviderList();
        new NeutralServerResponseAssertion(verify)
                .expectedCode(HttpStatus.SC_UNAUTHORIZED)
                .expectedSentryError(SentryErrorsList.TOKEN_NOT_FOUND.getError());
    }

    @Test
    @DisplayName("Verify receive list of DataProviders with invalid Token")
    void verifyGetContainersListWithWrongToken() {
        var verify = new ProviderController()
                .withToken("Bearer 12345")
                .getProviderList();
        new NeutralServerResponseAssertion(verify)
                .expectedSentryError(SentryErrorsList.TOKEN_CORRUPTED.getError());
    }

}
