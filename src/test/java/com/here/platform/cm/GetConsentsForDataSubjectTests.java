package com.here.platform.cm;

import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPConsumers;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.rest.model.ConsentRequestIdResponse;
import com.here.platform.cm.steps.api.OnboardingSteps;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VIN;
import com.here.platform.common.annotations.CMFeatures.GetConsentRequestStatus;
import java.io.File;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.provider.Arguments;


@DisplayName("Get consents for data subject")
@GetConsentRequestStatus
class GetConsentsForDataSubjectTests extends BaseCMTest {


    private final MPConsumers mpConsumer = MPConsumers.OLP_CONS_1;
    private final String
            testVin = VIN.generate(17);
    ConsentRequestContainers testScope = ConsentRequestContainers.getRandom();
    private final ConsentRequestData testConsentRequestData = new ConsentRequestData()
            .consumerId(mpConsumer.getRealm())
            .providerId(crypto.sha1())
            .containerId(testScope.id)
            .purpose(faker.commerce().productName())
            .privacyPolicy(faker.internet().url())
            .title(faker.gameOfThrones().quote());
    private String crid;
    private File testFileWithVINs;

    static Stream<Arguments> consumerIdAndVins() {
        return Stream.of(
                Arguments.of("", "", "consumerId"),
                Arguments.of("test", "", "vin"),
                Arguments.of("", "test", "consumerId")
        );
    }

    @Nested
    @DisplayName("Get consents for data subject")
    public class WithOnboarding {

        @BeforeEach
        void onboardConsumerAndCreateConsentRequest() {
            OnboardingSteps.onboardApplicationProviderAndConsumer(
                    testConsentRequestData.getProviderId(),
                    mpConsumer.getRealm(),
                    testScope
            );

            consentRequestController.withConsumerToken();
            var consentRequestResponse = consentRequestController.createConsentRequest(testConsentRequestData);
            crid = new ResponseAssertion(consentRequestResponse)
                    .statusCodeIsEqualTo(StatusCode.CREATED)
                    .bindAs(ConsentRequestIdResponse.class)
                    .getConsentRequestId();
        }

        @AfterEach
        void cleanUp() {
            RemoveEntitiesSteps.cascadeForceRemoveConsentRequest(crid, testFileWithVINs, testConsentRequestData);
        }

    }

}
