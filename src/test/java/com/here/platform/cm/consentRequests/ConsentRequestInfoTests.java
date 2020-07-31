package com.here.platform.cm.consentRequests;

import com.here.platform.aaa.HereCMBearerAuthorization;
import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.controllers.ConsentStatusController;
import com.here.platform.cm.controllers.ConsentStatusController.PageableConsent;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPConsumers;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentInfo.StateEnum;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.rest.model.ConsentRequestIdResponse;
import com.here.platform.cm.rest.model.ConsentRequestPurposeData;
import com.here.platform.cm.steps.api.OnboardingSteps;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VIN;
import com.here.platform.common.VinsToFile;
import com.here.platform.common.annotations.CMFeatures.CreateConsentRequest;
import com.here.platform.common.annotations.ErrorHandler;
import com.here.platform.dataProviders.DataSubjects;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@CreateConsentRequest
@DisplayName("Consent request Info")
public class ConsentRequestInfoTests extends BaseCMTest {

    private final MPConsumers mpConsumer = MPConsumers.OLP_CONS_1;
    private final ConsentRequestContainers testContainer = ConsentRequestContainers.CONNECTED_VEHICLE;
    private final ConsentRequestData testConsentRequest = new ConsentRequestData()
            .consumerId(mpConsumer.getRealm())
            .providerId(MPProviders.DAIMLER_EXPERIMENTAL.getName())
            .purpose(faker.commerce().productName())
            .privacyPolicy(faker.internet().url())
            .containerId(testContainer.id);

    private final ConsentStatusController consentStatusController = new ConsentStatusController();
    private final List<String> cridsToRemove = new ArrayList<>();
    private File testFileWithVINs = null;


    @BeforeEach
    void beforeEach() {
        OnboardingSteps.onboardApplicationProviderAndConsumer(
                testConsentRequest.getProviderId(),
                testConsentRequest.getConsumerId(),
                testContainer
        );
    }

    @AfterEach
    void cleanUp() {
        for (String crid : cridsToRemove) {
            RemoveEntitiesSteps.forceRemoveConsentRequestWithConsents(crid, testFileWithVINs);
        }
        RemoveEntitiesSteps.forceRemoveApplicationProviderConsumerEntities(testConsentRequest);
    }

    @Test
    @DisplayName("Fetch consent request info for VIN")
    void fetchConsentRequestInfoTest() {
        var testVin = VIN.generate(18);
        var theLast8SymbolsOfVin = new VIN(testVin).label();

        var firstTitle = faker.gameOfThrones().quote();
        testConsentRequest.title(firstTitle);
        var firstCrid = createConsentRequestWithCar(testVin);
        cridsToRemove.add(firstCrid);

        var firstExpectedInfos = new ConsentInfo()
                .consentRequestId(firstCrid)
                .purpose(testConsentRequest.getPurpose())
                .title(firstTitle)
                .consumerName(mpConsumer.getConsumerName())
                .state(StateEnum.PENDING)
                .approveTime(null)
                .revokeTime(null)
                .containerName(testContainer.name)
                .containerDescription(testContainer.containerDescription)
                .resources(testContainer.resources)
                .vinLabel(theLast8SymbolsOfVin);

        var consentRequestInfoResponse = consentStatusController
                .getConsentsInfo(
                        testVin,
                        PageableConsent.builder().stateEnum(StateEnum.PENDING).build()
                );
        var consentRequestInfos = new ResponseAssertion(consentRequestInfoResponse).statusCodeIsEqualTo(StatusCode.OK)
                .bindAsListOf(ConsentInfo[].class);
        Assertions.assertThat(consentRequestInfos)
                .usingElementComparatorIgnoringFields("createTime", "vinHash")
                .hasSize(1)
                .contains(firstExpectedInfos);

        var secondTitle = faker.gameOfThrones().quote();
        testConsentRequest.title(secondTitle);
        var secondCrid = createConsentRequestWithCar(testVin);
        cridsToRemove.add(secondCrid);

        var secondExpectedInfo = new ConsentInfo()
                .title(secondTitle).consentRequestId(secondCrid)
                .consumerName(mpConsumer.getConsumerName())
                .containerName(testContainer.name)
                .containerDescription(testContainer.containerDescription)
                .resources(firstExpectedInfos.getResources())
                .purpose(testConsentRequest.getPurpose()).state(StateEnum.PENDING)
                .vinLabel(theLast8SymbolsOfVin);

        consentRequestInfoResponse = consentStatusController
                .getConsentsInfo(
                        testVin,
                        PageableConsent.builder().stateEnum(StateEnum.PENDING).build()
                );
        consentRequestInfos = new ResponseAssertion(consentRequestInfoResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAsListOf(ConsentInfo[].class);
        Assertions.assertThat(consentRequestInfos)
                .usingElementComparatorIgnoringFields("createTime", "vinHash")
                .hasSize(2)
                .containsExactlyInAnyOrder(firstExpectedInfos, secondExpectedInfo);
    }

    @Test
    @ErrorHandler
    @DisplayName("Get consent request purpose Not found")
    void purposeNotFoundForConsentRequestTest() {
        testConsentRequest.title(faker.commerce().productName());

        var privateBearer = HereCMBearerAuthorization.getCmToken(DataSubjects.getNext());
        var purposeResponse = consentRequestController.getConsentRequestPurpose(crypto.sha1(), privateBearer);

        new ResponseAssertion(purposeResponse)
                .statusCodeIsEqualTo(StatusCode.NOT_FOUND)
                .expectedErrorResponse(CMErrorResponse.CONSENT_REQUEST_NOT_FOUND);
    }

    @Test
    @ErrorHandler
    @DisplayName("Forbidden to get purpose without Bearer token")
    void forbiddenToGetPurposeWithoutBearerTokenTest() {
        testConsentRequest.title(faker.commerce().productName());

        var purposeResponse = consentRequestController.getConsentRequestPurpose(crypto.sha1(), "");

        new ResponseAssertion(purposeResponse)
                .statusCodeIsEqualTo(StatusCode.UNAUTHORIZED)
                .expectedErrorResponse(CMErrorResponse.TOKEN_VALIDATION);
    }

    @Test
    @DisplayName("Verify purpose content of consent request")
    void getPurposeContentForConsentRequestTest() {
        testConsentRequest.title(faker.commerce().productName());
        consentRequestController.withCMToken();
        var crid = new ResponseAssertion(consentRequestController.createConsentRequest(testConsentRequest))
                .statusCodeIsEqualTo(StatusCode.CREATED)
                .bindAs(ConsentRequestIdResponse.class)
                .getConsentRequestId();

        cridsToRemove.add(crid);

        var privateBearer = HereCMBearerAuthorization.getCmToken(DataSubjects.getNext());

        var purposeResponse = consentRequestController.getConsentRequestPurpose(crid, privateBearer);

        new ResponseAssertion(purposeResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(new ConsentRequestPurposeData()
                        .containerName(testContainer.id)
                        .containerDescription(testContainer.containerDescription)
                        .resources(testContainer.resources)
                        .purpose(testConsentRequest.getPurpose())
                        .privacyPolicy(testConsentRequest.getPrivacyPolicy())
                        .consumerName(mpConsumer.getConsumerName())
                        .title(testConsentRequest.getTitle())
                );
    }

    private String createConsentRequestWithCar(String targetVin) {
        consentRequestController.withCMToken();
        var crid = new ResponseAssertion(consentRequestController.createConsentRequest(testConsentRequest))
                .statusCodeIsEqualTo(StatusCode.CREATED)
                .bindAs(ConsentRequestIdResponse.class)
                .getConsentRequestId();

        fuSleep();
        testFileWithVINs = new VinsToFile(targetVin).json(); //save for remove

        var addVinsResponse = consentRequestController
                .withConsumerToken(mpConsumer)
                .addVinsToConsentRequest(crid, testFileWithVINs);
        new ResponseAssertion(addVinsResponse).statusCodeIsEqualTo(StatusCode.OK);
        return crid;
    }

}
