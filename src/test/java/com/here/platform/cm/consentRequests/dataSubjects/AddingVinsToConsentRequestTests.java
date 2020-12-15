package com.here.platform.cm.consentRequests.dataSubjects;

import static com.here.platform.cm.enums.CMErrorResponse.CONSENT_REQUEST_UPDATE;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.rest.model.ConsentRequestIdResponse;
import com.here.platform.cm.rest.model.ConsentRequestStatus;
import com.here.platform.cm.steps.api.OnboardingSteps;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VinsToFile;
import com.here.platform.common.annotations.CMFeatures.UpdateConsentRequest;
import com.here.platform.common.config.Conf;
import com.here.platform.common.strings.VIN;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.helpers.Steps;
import io.qameta.allure.Issue;
import io.restassured.response.Response;
import java.io.File;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@UpdateConsentRequest
public class AddingVinsToConsentRequestTests extends BaseCMTest {

    private final User mpConsumer = Users.MP_CONSUMER.getUser();
    private final ConsentRequestContainer testContainer = ConsentRequestContainers
            .generateNew(MPProviders.DAIMLER_EXPERIMENTAL);
    private final ConsentRequestData testConsentRequest = new ConsentRequestData()
            .consumerId(mpConsumer.getRealm())
            .providerId(crypto.sha1())
            .title(Conf.cm().getQaTestDataMarker() + faker.gameOfThrones().quote())
            .purpose(faker.commerce().productName())
            .privacyPolicy(faker.internet().url())
            .containerId(testContainer.getId());
    private String crid;
    private File testFileWithVINs = null;

    @BeforeEach
    void onboardApplicationForProviderAndConsumer() {
        OnboardingSteps.onboardApplicationProviderAndConsumer(
                testConsentRequest.getProviderId(),
                testConsentRequest.getConsumerId(),
                testContainer
        );
        Steps.createRegularContainer(new Container(testContainer.getId(),
                testContainer.getId(),
                testContainer.getProvider().getName(),
                testContainer.getContainerDescription(),
                testContainer.getResources().get(0),
                false,
                testContainer.getScopeValue()));
        consentRequestController.withConsumerToken();
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

        consentRequestController.withConsumerToken();
        var addVinsToConsentRequestResponse = consentRequestController
                .addVinsToConsentRequest(crid, new VinsToFile(vinInLowerCase, vinInUpperCaseButLastSymbol).json());

        String cause = new ResponseAssertion(addVinsToConsentRequestResponse)
                .statusCodeIsEqualTo(StatusCode.BAD_REQUEST)
                .expectedErrorResponse(CONSENT_REQUEST_UPDATE).getCause();

        Assertions.assertThat(cause)
                .contains(List.of(vinInLowerCase, vinInUpperCaseButLastSymbol))
                .doesNotContain(validVIN);
    }

    @Test
    @DisplayName("Verify adding twice the same VINs are ignored")
    void addTheSameVINsIgnoredTest() {
        var validVINs = new String[]{VIN.generate(17), VIN.generate(17)};
        testFileWithVINs = new VinsToFile(validVINs).json();

        var expectedConsentRequestStatuses = new ConsentRequestStatus()
                .pending(2)
                .approved(0).revoked(0).expired(0).rejected(0);

        consentRequestController.withConsumerToken();
        var addVinsResponse = consentRequestController.addVinsToConsentRequest(crid, testFileWithVINs);
        new ResponseAssertion(addVinsResponse)
                .statusCodeIsEqualTo(StatusCode.OK);

        var statusForConsentRequestByIdResponse = consentRequestController
                .getStatusForConsentRequestById(crid);
        new ResponseAssertion(statusForConsentRequestByIdResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(expectedConsentRequestStatuses);

        var secondAddVinsResponse = consentRequestController.addVinsToConsentRequest(crid, testFileWithVINs);
        new ResponseAssertion(secondAddVinsResponse)
                .statusCodeIsEqualTo(StatusCode.OK);

        statusForConsentRequestByIdResponse = consentRequestController
                .getStatusForConsentRequestById(crid);
        new ResponseAssertion(statusForConsentRequestByIdResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(expectedConsentRequestStatuses);
    }

    @Test
    @Issue("NS-2802")
    @DisplayName("Verify adding duplicated VINs in a single file will be ignored")
    void duplicatedVINsTest() {
        var duplicatedVin = VIN.generate(17);
        var targetVINsWithDuplication = new String[]{VIN.generate(17), duplicatedVin, duplicatedVin};
        testFileWithVINs = new VinsToFile(targetVINsWithDuplication).json();

        consentRequestController.withConsumerToken();
        var addVinsResponse = consentRequestController
                .addVinsToConsentRequest(crid, testFileWithVINs);

        new ResponseAssertion(addVinsResponse).statusCodeIsEqualTo(StatusCode.OK);

        Response statusForConsentRequestById = consentRequestController.getStatusForConsentRequestById(crid);

        new ResponseAssertion(statusForConsentRequestById)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(
                        new ConsentRequestStatus()
                                .pending(targetVINsWithDuplication.length - 1)
                                .approved(0).revoked(0).expired(0).rejected(0)
                );
    }

}
