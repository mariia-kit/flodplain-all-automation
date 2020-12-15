package com.here.platform.cm.providersAndConsumers;

import static com.here.platform.common.strings.SBB.sbb;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.enums.ConsentManagementServiceUrl;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.rest.model.ConsentRequestIdResponse;
import com.here.platform.cm.rest.model.Provider;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.annotations.CMFeatures.OnBoardProvider;
import com.here.platform.common.config.Conf;
import com.here.platform.common.extensions.ConsentRequestRemoveExtension;
import com.here.platform.common.extensions.OnboardAndRemoveApplicationExtension;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;


@OnBoardProvider
class ProvidersTests extends BaseCMTest {

    private final ConsentRequestContainers testContainer = ConsentRequestContainers.getNextDaimlerExperimental();
    private final ConsentRequestData testConsentRequest = new ConsentRequestData()
            .consumerId(crypto.sha1())
            .providerId(testContainer.provider.getName())
            .title(sbb(Conf.cm().getQaTestDataMarker()).append(faker.gameOfThrones().quote()).bld())
            .purpose(faker.commerce().productName())
            .privacyPolicy(faker.internet().url())
            .containerId(testContainer.id);

    @Test
    @Tag("smoke_cm")
    @DisplayName("Verify success on-boarding of a Data Provider")
    void onboardDataProvider() {
        var testDataProvider = new Provider()
                .name(faker.commerce().department())
                .id(crypto.md5())
                .properties(Map.of());

        var onboardDataProvider = providerController
                .withCMToken()
                .onboardDataProvider(testDataProvider);

        new ResponseAssertion(onboardDataProvider)
                .statusCodeIsEqualTo(StatusCode.CREATED)
                .responseIsEmpty();

        var getProviderResponse = providerController
                .withConsumerToken()
                .getProviderById(testDataProvider.getId());
        new ResponseAssertion(getProviderResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(testDataProvider);

        RemoveEntitiesSteps.removeProvider(testDataProvider.getId());

        getProviderResponse = providerController
                .withConsumerToken()
                .getProviderById(testDataProvider.getId());
        new ResponseAssertion(getProviderResponse)
                .statusCodeIsEqualTo(StatusCode.NOT_FOUND)
                .expectedErrorResponse(CMErrorResponse.PROVIDER_NOT_FOUND);
    }

    @Nested
    @DisplayName("Redirect to data provider OAUTH")
    public class RedirectDataProvider {

        @RegisterExtension
        OnboardAndRemoveApplicationExtension onboardApplicationExtension = OnboardAndRemoveApplicationExtension
                .builder().consentRequestData(testConsentRequest).build();

        @RegisterExtension
        ConsentRequestRemoveExtension consentRequestRemoveExtension = new ConsentRequestRemoveExtension();

        @Test
        @DisplayName("Verify redirect to the Data Provider OAUTH")
        void dataProviderRedirectTest() {
            var consentRequestResponse = consentRequestController
                    .withConsumerToken()
                    .createConsentRequest(testConsentRequest);
            var crid = new ResponseAssertion(consentRequestResponse)
                    .statusCodeIsEqualTo(StatusCode.CREATED)
                    .bindAs(ConsentRequestIdResponse.class)
                    .getConsentRequestId();
            consentRequestRemoveExtension.cridToRemove(crid);

            var redirectToDataProviderResponse = providerController
                    .redirectToDataProviderByRequestId(crid, ConsentManagementServiceUrl.getEnvUrl());

            new ResponseAssertion(redirectToDataProviderResponse)
                    .statusCodeIsEqualTo(StatusCode.REDIRECT)
                    .responseIsEmpty();
        }

    }

    //todo add tests for DAIMLER redirect and excelsior

}
