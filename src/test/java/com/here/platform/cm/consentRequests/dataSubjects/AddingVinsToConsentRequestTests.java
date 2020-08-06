package com.here.platform.cm.consentRequests.dataSubjects;

import static com.here.platform.cm.enums.CMErrorResponse.CONSENT_REQUEST_UPDATE;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.enums.DaimlerContainers;
import com.here.platform.cm.enums.MPConsumers;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.rest.model.ConsentRequestIdResponse;
import com.here.platform.cm.rest.model.ConsentRequestStatus;
import com.here.platform.cm.rest.model.ErrorResponse;
import com.here.platform.cm.steps.api.OnboardingSteps;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VIN;
import com.here.platform.common.VinsToFile;
import com.here.platform.common.annotations.CMFeatures.UpdateConsentRequest;
import java.io.File;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("Update consent request")
@UpdateConsentRequest
public class AddingVinsToConsentRequestTests extends BaseCMTest {

    private final MPConsumers mpConsumer = MPConsumers.OLP_CONS_1;
    private final DaimlerContainers testScope = DaimlerContainers.getRandom();
    private final ConsentRequestData testConsentRequest = new ConsentRequestData()
            .consumerId(mpConsumer.getRealm())
            .providerId(crypto.sha1())
            .title(faker.gameOfThrones().quote())
            .purpose(faker.commerce().productName())
            .privacyPolicy(faker.internet().url())
            .containerId(testScope.id);
    private String crid;
    private File testFileWithVINs = null;

    @BeforeEach
    void onboardApplicationForProviderAndConsumer() {
        OnboardingSteps.onboardApplicationProviderAndConsumer(
                testConsentRequest.getProviderId(),
                testConsentRequest.getConsumerId(),
                testScope
        );

        consentRequestController.withCMToken();
        crid = new ResponseAssertion(consentRequestController.createConsentRequest(testConsentRequest))
                .statusCodeIsEqualTo(StatusCode.CREATED)
                .bindAs(ConsentRequestIdResponse.class)
                .getConsentRequestId();
    }

    @AfterEach
    void cleanUp() {
        RemoveEntitiesSteps.cascadeForceRemoveConsentRequest(crid, testFileWithVINs, testConsentRequest);
    }

    @Test
    @DisplayName("Verify Adding Invalid VINs Error")
    void addInvalidVINsTest() {
        var vinInLowerCase = VIN.generate(17).toLowerCase();
        var vinInUpperCaseButLastSymbol = VIN.generate(16) + "a";
        var validVIN = VIN.generate(17);

        consentRequestController.withConsumerToken(mpConsumer);
        var addVinsToConsentRequestResponse = consentRequestController
                .addVinsToConsentRequest(crid, new VinsToFile(vinInLowerCase, vinInUpperCaseButLastSymbol).json());

        Assertions.assertThat(addVinsToConsentRequestResponse.statusCode()).isEqualTo(StatusCode.BAD_REQUEST.code);
        var actualErrorResponse = addVinsToConsentRequestResponse.as(ErrorResponse.class);

        Assertions.assertThat(actualErrorResponse.getCode()).isEqualTo(CONSENT_REQUEST_UPDATE.getCode());
        Assertions.assertThat(actualErrorResponse.getAction()).isEqualTo(CONSENT_REQUEST_UPDATE.getAction());
        Assertions.assertThat(actualErrorResponse.getTitle()).isEqualTo(CONSENT_REQUEST_UPDATE.getTitle());
        Assertions.assertThat(actualErrorResponse.getCause())
                .contains(List.of(vinInLowerCase, vinInUpperCaseButLastSymbol))
                .doesNotContain(validVIN);
    }

    @Test
    @DisplayName("Verify Same VINs Ignored During Adding")
    void addTheSameVINsIgnoredTest() {
        var validVINs = new String[]{VIN.generate(17), VIN.generate(17)};

        testFileWithVINs = new VinsToFile(validVINs).json();
        consentRequestController.withConsumerToken(mpConsumer);
        var addVinsResponse = consentRequestController
                .addVinsToConsentRequest(crid, testFileWithVINs);

        Assertions.assertThat(addVinsResponse.statusCode()).isEqualTo(StatusCode.OK.code);

        var secondAddVinsResponse = consentRequestController
                .addVinsToConsentRequest(crid, testFileWithVINs);

        Assertions.assertThat(secondAddVinsResponse.statusCode()).isEqualTo(StatusCode.OK.code);

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
