package com.here.platform.cm.bmw;


import com.here.platform.cm.controllers.BMWController;
import com.here.platform.cm.enums.BMWStatus;
import com.here.platform.cm.enums.ConsentManagementServiceUrl;
import com.here.platform.cm.enums.MPConsumers;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.rest.model.AsyncUpdateResponse;
import com.here.platform.cm.rest.model.ConsentInfo.StateEnum;
import com.here.platform.cm.rest.model.ConsentRequestAsyncUpdateInfo;
import com.here.platform.cm.rest.model.ConsentRequestStatus;
import com.here.platform.cm.rest.model.ConsentStatus;
import com.here.platform.cm.rest.model.Health;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VinsToFile;
import com.here.platform.dataProviders.reference.controllers.ReferenceProviderController;
import io.qameta.allure.Issue;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;


@DisplayName("Verify and Set Consent Clearance status by BMW Provider")
@Tag("bmw_cm")
@Execution(ExecutionMode.SAME_THREAD)
public class BmwRequestStatusTests extends BaseBmwConsentTests {

    private String crid;
    private final BMWController bmwController = new BMWController();
    private final ReferenceProviderController referenceProviderController = new ReferenceProviderController();

    @AfterEach
    void cleanUp() {
        RemoveEntitiesSteps.cascadeForceRemoveConsentRequest(crid, testFileWithVINs, testConsentRequestData);
    }

    @Test
    @Issue("NS-2427")
    @DisplayName("Flow of the verifying of Consent Service status for the BMW")
    void pingConsentStatusByBMW() {
        var response = bmwController.pingConsentServiceStatusByBMW();

        new ResponseAssertion(response)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(new Health().status("OK"));
    }

    @ParameterizedTest(name = "[BMW] Positive flow of changing consent status of the current consent request from PENDING to {0}")
    @Issue("NS-2427")
    @EnumSource(BMWStatus.class)
    void setClearanceStatusByBMW(BMWStatus bmwStatus) {
        crid = createValidBmwConsentRequest();

        var responseBefore = consentStatusController
                .withConsumerToken(MPConsumers.OLP_CONS_1)
                .getConsentStatusByIdAndVin(crid, testVin1);
        new ResponseAssertion(responseBefore).statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObjectIgnoringTimeFields(new ConsentStatus()
                        .consentRequestId(crid)
                        .state(StateEnum.PENDING.getValue())
                        .vin(testVin1));

        var clearanceId = referenceProviderController.getClearanceByVinAndContainerId(testVin1, testContainer.getClientId()).jsonPath()
                .get("clearanceId").toString();

        var response = bmwController.setClearanceStatusByBMW(clearanceId, bmwStatus.name());

        new ResponseAssertion(response)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(new Health().status("OK"));

        var responseAfter = consentStatusController.getConsentStatusByIdAndVin(crid, testVin1);
        new ResponseAssertion(responseAfter).statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObjectIgnoringTimeFields(new ConsentStatus()
                        .consentRequestId(crid)
                        .state(bmwStatus.getCmStatus().getValue())
                        .vin(testVin1));
    }

    @Test
    @DisplayName("Positive BMW flow of updating consent statuses for consent request with multiple VINs")
    void setClearanceStatusByBMWMultiple() {
        testFileWithVINs = new VinsToFile(testVin1, testVin2).json(); //for cascade remove
        ProviderApplications targetApp = ProviderApplications.BMW_CONS_1;
        crid = ConsentRequestSteps.createValidConsentRequestWithNSOnboardings(targetApp, testVin1, testContainer)
                .getConsentRequestId();
        ConsentRequestSteps.addVINsToConsentRequest(targetApp, crid, testVin2);

        var expectedConsentRequestStatuses = new ConsentRequestStatus()
                .approved(0)
                .pending(2)
                .revoked(0)
                .expired(0)
                .rejected(0);

        consentRequestController.withConsumerToken();
        var statusForConsentRequestByIdResponse = consentRequestController
                .getStatusForConsentRequestById(crid);
        new ResponseAssertion(statusForConsentRequestByIdResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(expectedConsentRequestStatuses);

        var clearanceId1 = referenceProviderController.getClearanceByVinAndContainerId(testVin1, testContainer.getClientId()).jsonPath()
                .get("clearanceId").toString();
        var clearanceId2 = referenceProviderController.getClearanceByVinAndContainerId(testVin2, testContainer.getClientId()).jsonPath()
                .get("clearanceId").toString();
        bmwController.setClearanceStatusByBMW(clearanceId1, BMWStatus.APPROVED.name());
        bmwController.setClearanceStatusByBMW(clearanceId2, BMWStatus.REVOKED.name());
        expectedConsentRequestStatuses = new ConsentRequestStatus()
                .approved(1)
                .pending(0)
                .revoked(1)
                .expired(0)
                .rejected(0);

        consentRequestController.withConsumerToken();
        statusForConsentRequestByIdResponse = consentRequestController
                .getStatusForConsentRequestById(crid);
        new ResponseAssertion(statusForConsentRequestByIdResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(expectedConsentRequestStatuses);
    }

    @Test
    @DisplayName("Async Verify adding vins to Consent request for BMW")
    void addVinsToConsentRequestTestAsyncBMW() {
        ProviderApplications targetApp = ProviderApplications.BMW_CONS_1;
        String consentRequestAsyncUpdateInfo = "consentRequestAsyncUpdateInfo/";
        crid = ConsentRequestSteps.createValidConsentRequestWithNSOnboardings(targetApp, testVin1, testContainer).getConsentRequestId();
        testFileWithVINs = new VinsToFile(testVin2).json();
        var addVinsToConsentRequest = consentRequestController
                .withConsumerToken(mpConsumer)
                .addVinsToConsentRequestAsync(crid, testFileWithVINs);
        testFileWithVINs = new VinsToFile(testVin1, testVin2).json();
        String updateInfoUrl = new ResponseAssertion(addVinsToConsentRequest)
                .statusCodeIsEqualTo(StatusCode.ACCEPTED)
                .bindAs(AsyncUpdateResponse.class)
                .getConsentRequestAsyncUpdateInfoUrl();

        Assertions.assertThat(updateInfoUrl)
                .isNotEmpty()
                .startsWith(ConsentManagementServiceUrl.getEnvUrl() + consentRequestAsyncUpdateInfo);
        String asyncId = StringUtils.substringAfter(updateInfoUrl, consentRequestAsyncUpdateInfo);

        fuSleep();
        new ResponseAssertion(consentRequestController.getConsentRequestAsyncUpdateInfo(asyncId))
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObjectIgnoringTimeFields(new ConsentRequestAsyncUpdateInfo()
                        .status(ConsentRequestAsyncUpdateInfo.StatusEnum.FINISHED)
                        .action(ConsentRequestAsyncUpdateInfo.ActionEnum.ADD_DATA_SUBJECTS)
                        .consentRequestId(crid)
                        .id(Long.parseLong(asyncId))
                        .vinUpdateErrors(new ArrayList<>())
                );
        consentRequestController.withConsumerToken();
        new ResponseAssertion(consentRequestController.getStatusForConsentRequestById(crid))
                .responseIsEqualToObject(new ConsentRequestStatus()
                        .approved(0)
                        .pending(2)
                        .revoked(0)
                        .expired(0)
                        .rejected(0)
                );
    }

}
