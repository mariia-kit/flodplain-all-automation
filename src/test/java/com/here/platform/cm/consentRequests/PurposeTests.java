package com.here.platform.cm.consentRequests;


import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.Consents;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.rest.model.AdditionalLink;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.rest.model.ConsentRequestIdResponse;
import com.here.platform.cm.rest.model.ConsentRequestPurposeData;
import com.here.platform.cm.steps.api.ConsentRequestSteps2;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.annotations.CMFeatures.Purpose;
import com.here.platform.common.annotations.ErrorHandler;
import com.here.platform.common.config.Conf;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@Purpose
@DisplayName("Purpose for consent request")
public class PurposeTests extends BaseCMTest {

    @Test
    @ErrorHandler
    @DisplayName("Get consent request purpose Not found")
    void purposeNotFoundForConsentRequestTest() {
        var privateBearer = DataSubjects.getNextBy18VINLength().getBearerToken();
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
                .title(Conf.cm().getQaTestDataMarker() + faker.gameOfThrones().character())
                .purpose(faker.gameOfThrones().quote())
                .privacyPolicy(faker.internet().url())
                .addAdditionalLinksItem(
                        new AdditionalLink().title(faker.commerce().department()).url(faker.internet().url())
                );
    }

    @Test
    @DisplayName("Verify purpose content of consent request")
    void getPurposeForConsentRequestTest() {
        ProviderApplications targetApp = ProviderApplications.REFERENCE_CONS_1;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(targetApp.getProvider());
        DataSubjects dataSubject = DataSubjects.getNextVinLength(targetApp.getProvider().getVinLength());
        ConsentInfo consentInfo = Consents.generateNewConsentInfo(mpConsumer, targetContainer);
        var crid = new ConsentRequestSteps2(targetContainer, consentInfo)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .getId();

        var purposeResponse = consentRequestController
                .withConsumerToken()
                .getConsentRequestPurpose(crid, dataSubject.getBearerToken());

        new ResponseAssertion(purposeResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(new ConsentRequestPurposeData()
                        .containerName(targetContainer.getName())
                        .containerDescription(targetContainer.getContainerDescription())
                        .resources(targetContainer.getResources())
                        .purpose(consentInfo.getPurpose())
                        .privacyPolicy(consentInfo.getPrivacyPolicy())
                        .consumerName(consentInfo.getConsumerName())
                        .title(consentInfo.getTitle())
                );
    }

    @Test
    @DisplayName("Get purpose data by consumerId and containerId")
    void getPurposeByConsumerAndContainerIdsTest() {
        ProviderApplications targetApp = ProviderApplications.REFERENCE_CONS_1;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(targetApp.getProvider());
        DataSubjects dataSubject = DataSubjects.getNextVinLength(targetApp.getProvider().getVinLength());
        ConsentInfo consentInfo = Consents.generateNewConsentInfo(mpConsumer, targetContainer);
        var crid = new ConsentRequestSteps2(targetContainer, consentInfo)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .getId();

        var purposeResponse = consentRequestController
                .withConsumerToken()
                .getConsentRequestPurpose(
                        consentInfo.getConsumerId(),
                        consentInfo.getContainerId(),
                        dataSubject.getBearerToken()
                );

        new ResponseAssertion(purposeResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(new ConsentRequestPurposeData()
                        .containerName(targetContainer.getName())
                        .containerDescription(consentInfo.getContainerDescription())
                        .resources(targetContainer.getResources())
                        .purpose(consentInfo.getPurpose())
                        .privacyPolicy(consentInfo.getPrivacyPolicy())
                        .consumerName(consentInfo.getConsumerName())
                        .title(consentInfo.getTitle())
                );
    }

}
