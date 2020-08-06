package com.here.platform.cm.consentRequests;

import static com.here.platform.cm.steps.api.OnboardingSteps.onboardApplicationProviderAndConsumer;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.enums.DaimlerContainers;
import com.here.platform.cm.enums.MPConsumers;
import com.here.platform.cm.rest.model.AdditionalLink;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.rest.model.ConsentRequestIdResponse;
import com.here.platform.cm.rest.model.ConsentRequestStatus;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VIN;
import com.here.platform.common.VinsToFile;
import com.here.platform.common.annotations.CMFeatures.CreateConsentRequest;
import io.qameta.allure.TmsLink;
import java.io.File;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@DisplayName("Create consent request")
@CreateConsentRequest
@Tag("smoke_cm")
public class CreateConsentRequestsTests extends BaseCMTest {

    private final MPConsumers mpConsumer = MPConsumers.OLP_CONS_1;
    private final DaimlerContainers testScope = DaimlerContainers.getRandom();
    private final ConsentRequestData testConsentRequest = new ConsentRequestData()
            .consumerId(mpConsumer.getRealm())
            .providerId(crypto.sha1())
            .title(faker.gameOfThrones().quote())
            .purpose(faker.commerce().productName())
            .privacyPolicy(faker.internet().url())
            .addAdditionalLinksItem(
                    new AdditionalLink().title(faker.commerce().department()).url(faker.internet().url()))
            .containerId(testScope.id);
    private String crid;
    private File testFileWithVINs = null;

    @BeforeEach
    void beforeEach() {
        onboardApplicationProviderAndConsumer(
                testConsentRequest.getProviderId(),
                testConsentRequest.getConsumerId(),
                testScope
        );

        consentRequestController.withCMToken();
        final var actualResponse = consentRequestController.createConsentRequest(testConsentRequest);
        crid = new ResponseAssertion(actualResponse)
                .statusCodeIsEqualTo(StatusCode.CREATED)
                .bindAs(ConsentRequestIdResponse.class)
                .getConsentRequestId();
    }

    @AfterEach
    void cleanUp() {
        RemoveEntitiesSteps.cascadeForceRemoveConsentRequest(crid, testFileWithVINs, testConsentRequest);
    }

    @Test
    @DisplayName("Verify Creation Of ConsentRequest With Empty Vin")
    @TmsLink("NS-1351")
    void createConsentRequestWithEmptyVinTestPositiveTst() {
        var expectedConsentRequestStatuses = new ConsentRequestStatus()
                .approved(0)
                .pending(0)
                .revoked(0);

        consentRequestController.withCMToken();
        var statusForConsentRequestByIdResponse = consentRequestController
                .getStatusForConsentRequestById(crid);
        new ResponseAssertion(statusForConsentRequestByIdResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(expectedConsentRequestStatuses);
    }

    @Test
    @DisplayName("Verify Adding Vins To Empty ConsentRequest Via File")
    @TmsLink("NS-1382")
    void addVinsToEmptyConsentRequestViaFileTest() {
        var testVin = VIN.generate(17);
        testFileWithVINs = new VinsToFile(
                testVin,
                VIN.generate(17)
        ).json();

        consentRequestController.withConsumerToken(mpConsumer);
        consentRequestController
                .addVinsToConsentRequest(crid, testFileWithVINs);
        fuSleep();
        var expectedConsentRequestStatuses = new ConsentRequestStatus()
                .approved(0)
                .pending(2)
                .revoked(0);

        consentRequestController.withCMToken();
        var statusForConsentRequestByIdResponse = consentRequestController
                .getStatusForConsentRequestById(crid);
        new ResponseAssertion(statusForConsentRequestByIdResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(expectedConsentRequestStatuses);
    }

}
