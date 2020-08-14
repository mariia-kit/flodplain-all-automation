package com.here.platform.cm.consentRequests;


import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.rest.model.AdditionalLink;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.rest.model.ConsentRequestIdResponse;
import com.here.platform.cm.rest.model.ConsentRequestPurposeData;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.annotations.ErrorHandler;
import com.here.platform.common.extensions.ConsentRequestRemoveExtension;
import com.here.platform.common.extensions.OnboardApplicationExtension;
import com.here.platform.dataProviders.daimler.DataSubjects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;


public class PurposeTests extends BaseCMTest {

    @RegisterExtension
    final ConsentRequestRemoveExtension requestRemoveExtension = new ConsentRequestRemoveExtension();

    @Test
    @ErrorHandler
    @DisplayName("Get consent request purpose Not found")
    void purposeNotFoundForConsentRequestTest() {
        var privateBearer = DataSubjects.getNext().getBearerToken();
        var purposeResponse = consentRequestController.getConsentRequestPurpose(crypto.sha1(), privateBearer);

        new ResponseAssertion(purposeResponse)
                .statusCodeIsEqualTo(StatusCode.NOT_FOUND)
                .expectedErrorResponse(CMErrorResponse.CONSENT_REQUEST_NOT_FOUND);
    }

    @Test
    @ErrorHandler
    @DisplayName("Forbidden to get purpose without Bearer token")
    void forbiddenToGetPurposeWithoutBearerTokenTest() {
        var purposeResponse = consentRequestController.getConsentRequestPurpose(crypto.sha1(), "");

        new ResponseAssertion(purposeResponse)
                .statusCodeIsEqualTo(StatusCode.UNAUTHORIZED)
                .expectedErrorResponse(CMErrorResponse.TOKEN_VALIDATION);
    }

    public ConsentRequestData getBaseConsentRequestData() {
        return new ConsentRequestData()
                .title(faker.gameOfThrones().character())
                .purpose(faker.gameOfThrones().quote())
                .privacyPolicy(faker.internet().url())
                .addAdditionalLinksItem(
                        new AdditionalLink().title(faker.commerce().department()).url(faker.internet().url())
                );
    }

    @Nested
    @DisplayName("Get consent request purpose")
    public class ConsentRequestPurpose {

        private final String privateBearer = DataSubjects.getNext().generateBearerToken();
        private final ConsentRequestContainers targetContainer = ConsentRequestContainers.DAIMLER_EXPERIMENTAL_CHARGE;


        private final ConsentRequestData targetConsentRequest = getBaseConsentRequestData()
                .providerId(targetContainer.provider.getName())
                .consumerId(faker.crypto().md5())
                .containerId(targetContainer.id);
        @RegisterExtension
        final OnboardApplicationExtension onboardApplicationExtension =
                OnboardApplicationExtension.builder().consentRequestData(targetConsentRequest).build();

        private String crid;

        @BeforeEach
        void beforeEach() {
            consentRequestController.withCMToken();
            var consentRequest = consentRequestController.createConsentRequest(targetConsentRequest);
            crid = new ResponseAssertion(consentRequest)
                    .statusCodeIsEqualTo(StatusCode.CREATED).bindAs(ConsentRequestIdResponse.class)
                    .getConsentRequestId();

            requestRemoveExtension.cridToRemove(crid);
        }

        @Test
        @DisplayName("Verify purpose content of consent request")
        void getPurposeForConsentRequestTest() {
            var purposeResponse = consentRequestController.getConsentRequestPurpose(crid, privateBearer);

            new ResponseAssertion(purposeResponse)
                    .statusCodeIsEqualTo(StatusCode.OK)
                    .responseIsEqualToObject(new ConsentRequestPurposeData()
                            .containerName(targetContainer.name)
                            .containerDescription(targetContainer.containerDescription)
                            .resources(targetContainer.resources)
                            .purpose(targetConsentRequest.getPurpose())
                            .privacyPolicy(targetConsentRequest.getPrivacyPolicy())
                            .consumerName(onboardApplicationExtension.getOnboardedConsumer().getConsumerName())
                            .title(targetConsentRequest.getTitle())
                    );
        }

        @Test
        @DisplayName("Get purpose data by consumerId and containerId")
        void getPurposeByConsumerAndContainerIdsTest() {
            var purposeResponse = consentRequestController.getConsentRequestPurpose(
                    targetConsentRequest.getConsumerId(),
                    targetConsentRequest.getContainerId(),
                    privateBearer
            );

            new ResponseAssertion(purposeResponse)
                    .statusCodeIsEqualTo(StatusCode.OK)
                    .responseIsEqualToObject(new ConsentRequestPurposeData()
                            .containerName(targetContainer.name)
                            .containerDescription(targetContainer.containerDescription)
                            .resources(targetContainer.resources)
                            .purpose(targetConsentRequest.getPurpose())
                            .privacyPolicy(targetConsentRequest.getPrivacyPolicy())
                            .consumerName(onboardApplicationExtension.getOnboardedConsumer().getConsumerName())
                            .title(targetConsentRequest.getTitle())
                    );
        }

    }

}
