package com.here.platform.cm.providersAndConsumers;

import static com.here.platform.common.strings.SBB.sbb;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.enums.ConsentManagementServiceUrl;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.Consents;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.rest.model.ConsentRequestIdResponse;
import com.here.platform.cm.rest.model.Provider;
import com.here.platform.cm.steps.api.ConsentRequestSteps2;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.annotations.CMFeatures.OnBoardProvider;
import com.here.platform.common.config.Conf;
import com.here.platform.common.extensions.ConsentRequestRemoveExtension;
import com.here.platform.common.extensions.OnboardAndRemoveApplicationExtension;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;


@OnBoardProvider
@DisplayName("Onboard Provider")
class ProvidersTests extends BaseCMTest {

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

    @Test
    @DisplayName("Verify redirect to the Data Provider OAUTH")
    void dataProviderRedirectTest() {
        ProviderApplications targetApp = ProviderApplications.DAIMLER_CONS_1;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(targetApp.getProvider());

        ConsentInfo consentInfo = Consents.generateNewConsentInfo(mpConsumer, targetContainer);
        var crid = new ConsentRequestSteps2(targetContainer, consentInfo)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .getId();

        var redirectToDataProviderResponse = providerController
                .redirectToDataProviderByRequestId(crid, ConsentManagementServiceUrl.getEnvUrl());

        new ResponseAssertion(redirectToDataProviderResponse)
                .statusCodeIsEqualTo(StatusCode.REDIRECT)
                .responseIsEmpty();
    }

    @Test
    @DisplayName("Verify redirect to the Data Provider OAUTH Reference")
    void dataProviderRedirectTestReference() {
        ProviderApplications targetApp = ProviderApplications.REFERENCE_CONS_1;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(targetApp.getProvider());

        ConsentInfo consentInfo = Consents.generateNewConsentInfo(mpConsumer, targetContainer);
        var crid = new ConsentRequestSteps2(targetContainer, consentInfo)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .getId();

        var redirectToDataProviderResponse = providerController
                .redirectToDataProviderByRequestId(crid, ConsentManagementServiceUrl.getEnvUrl());

        new ResponseAssertion(redirectToDataProviderResponse)
                .statusCodeIsEqualTo(StatusCode.REDIRECT)
                .responseIsEmpty();
    }

}
