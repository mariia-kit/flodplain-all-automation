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
import com.here.platform.common.controller.ReferenceProviderController;
import io.qameta.allure.Issue;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;


@DisplayName("Verify and Set Consent Clearance status by BMW Provider")
public class BmwRequestStatusTests extends BaseBmwConsentTests {

    private String crid;
    private final BMWController bmwController = new BMWController();
    private final ReferenceProviderController referenceProviderController = new ReferenceProviderController();

    @AfterEach
    void cleanUp() {
        RemoveEntitiesSteps.cascadeForceRemoveConsentRequest(crid, testFileWithVINs, testConsentRequestData);
        referenceProviderController.cleanUpContainersVehiclesResources();
    }

    @Test
    @Issue("NS-2427")
    @DisplayName("BMW read status of consent service acceptability")
    void pingConsentStatusByBMW() {
        var response = bmwController.pingConsentServiceStatusByBMW();

        new ResponseAssertion(response)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(new Health().status("OK"));
    }

    @ParameterizedTest
    @Issue("NS-2427")
    @DisplayName("BMW set status of consent clearance")
    @EnumSource(BMWStatus.class)
    void setClearanceStatusByBMW(BMWStatus bmwStatus) {
        crid = createValidBmwConsentRequest();

        var responseBefore = consentStatusController
                .withConsumerToken(MPConsumers.OLP_CONS_1)
                .getConsentStatusByIdAndVin(crid, testVin);
        new ResponseAssertion(responseBefore).statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObjectIgnoringTimeFields(new ConsentStatus()
                        .consentRequestId(crid)
                        .state(StateEnum.PENDING.getValue())
                        .vin(testVin));

        var clearanceId = referenceProviderController.getClearanceByVin(testVin, bmwContainer).jsonPath()
                .get("clearanceId").toString();

        var response = bmwController.setClearanceStatusByBMW(clearanceId, bmwStatus.name());

        new ResponseAssertion(response)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(new Health().status("OK"));

        var responseAfter = consentStatusController.getConsentStatusByIdAndVin(crid, testVin);
        new ResponseAssertion(responseAfter).statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObjectIgnoringTimeFields(new ConsentStatus()
                        .consentRequestId(crid)
                        .state(bmwStatus.getCmStatus().getValue())
                        .vin(testVin));
    }

    @Test
    @DisplayName("BMW set status of consent clearance multiple")
    void setClearanceStatusByBMWMultiple() {
        testFileWithVINs = new VinsToFile(testVin, testVin1).json();
        ProviderApplications targetApp = ProviderApplications.BMW_CONS_1;
        crid = ConsentRequestSteps.createConsentRequestFor(targetApp).getConsentRequestId();
        ConsentRequestSteps.addVINsToConsentRequest(targetApp, crid, testVin, testVin1);

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

        var clearanceId1 = referenceProviderController.getClearanceByVin(testVin, bmwContainer).jsonPath()
                .get("clearanceId").toString();
        var clearanceId2 = referenceProviderController.getClearanceByVin(testVin1, bmwContainer).jsonPath()
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
    @DisplayName("Verify Adding Vins To ConsentRequest Async BMW")
    void addVinsToConsentRequestTestAsyncBMW() {
        String consentRequestAsyncUpdateInfo = "consentRequestAsyncUpdateInfo/";
        crid = ConsentRequestSteps.createConsentRequestFor(ProviderApplications.BMW_CONS_1).getConsentRequestId();
        testFileWithVINs = new VinsToFile(testVin, testVin1).json();
        var addVinsToConsentRequest = consentRequestController
                .withConsumerToken(mpConsumer)
                .addVinsToConsentRequestAsync(crid, testFileWithVINs);

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
