package com.here.platform.cm.bmw;


import com.here.platform.cm.controllers.BMWController;
import com.here.platform.cm.enums.BMWStatus;
import com.here.platform.cm.enums.ConsentManagementServiceUrl;
import com.here.platform.cm.enums.ConsentObject;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.rest.model.AsyncUpdateResponse;
import com.here.platform.cm.rest.model.ConsentInfo.StateEnum;
import com.here.platform.cm.rest.model.ConsentRequestAsyncUpdateInfo;
import com.here.platform.cm.rest.model.ConsentRequestStatus;
import com.here.platform.cm.rest.model.Health;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VinsToFile.FILE_TYPE;
import com.here.platform.common.annotations.CMFeatures.ASYNC;
import com.here.platform.common.config.Conf;
import com.here.platform.dataProviders.reference.controllers.ReferenceProviderController;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import io.qameta.allure.Issue;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.FixMethodOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.runners.MethodSorters;


@DisplayName("Verify and Set Consent Clearance status by BMW Provider")
@Tag("bmw_cm")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Execution(ExecutionMode.SAME_THREAD)
public class BmwRequestStatusTests extends BaseBmwConsentTests {


    private final BMWController bmwController = new BMWController();
    private final ReferenceProviderController referenceProviderController = new ReferenceProviderController();

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
        MPProviders provider = MPProviders.BMW_TEST;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer testContainer = ConsentRequestContainers.generateNew(provider)
                .withResources(List.of("fuel"))
                .withClientIdSecret(Conf.cmUsers().getBmwApp());
        testContainer.setClientId(testContainer.getId());

        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, testContainer);
        var step= new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(testVin1)
                .verifyConsentStatusByVin(testVin1, StateEnum.PENDING.getValue());

        var clearanceId = referenceProviderController
                .getClearanceByVinAndContainerId(testVin1, testContainer.getClientId()).jsonPath()
                .get("clearanceId")
                .toString();

        var response = bmwController.setClearanceStatusByBMW(clearanceId, bmwStatus.name());

        new ResponseAssertion(response)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(new Health().status("OK"));

        step.verifyConsentStatusByVin(testVin1, bmwStatus.getCmStatus().getValue());
    }

    @Test
    @DisplayName("Positive BMW flow of updating consent statuses for consent request with multiple VINs")
    void setClearanceStatusByBMWMultiple() {
        MPProviders provider = MPProviders.BMW_TEST;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer testContainer = ConsentRequestContainers.generateNew(provider)
                .withResources(List.of("fuel"))
                .withClientIdSecret(Conf.cmUsers().getBmwApp());
        testContainer.setClientId(testContainer.getId());

        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, testContainer);
        var step= new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(testVin1)
                .addVINsToConsentRequest(testVin2)
                .verifyConsentStatus(
                        new ConsentRequestStatus()
                                .approved(0)
                                .pending(2)
                                .revoked(0)
                                .expired(0)
                                .rejected(0));


        var clearanceId1 = referenceProviderController
                .getClearanceByVinAndContainerId(testVin1, testContainer.getClientId()).jsonPath()
                .get("clearanceId").toString();
        var clearanceId2 = referenceProviderController
                .getClearanceByVinAndContainerId(testVin2, testContainer.getClientId()).jsonPath()
                .get("clearanceId").toString();

        bmwController.setClearanceStatusByBMW(clearanceId1, BMWStatus.APPROVED.name());
        bmwController.setClearanceStatusByBMW(clearanceId2, BMWStatus.REVOKED.name());

        step.verifyConsentStatus(
                new ConsentRequestStatus()
                        .approved(1)
                        .pending(0)
                        .revoked(1)
                        .expired(0)
                        .rejected(0));
    }

    @Test
    @ASYNC
    @DisplayName("Async Verify adding vins to Consent request for BMW")
    void addVinsToConsentRequestTestAsyncBMW() {
        MPProviders provider = MPProviders.BMW_TEST;
        String consentRequestAsyncUpdateInfo = "consentRequestAsyncUpdateInfo/";
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer testContainer = ConsentRequestContainers.generateNew(provider)
                .withResources(List.of("fuel"))
                .withClientIdSecret(Conf.cmUsers().getBmwApp());
        testContainer.setClientId(testContainer.getId());

        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, testContainer);
        var step= new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(testVin1);
        var crid = step.getId();

        var addVinsToConsentRequest = consentRequestController
                .withConsumerToken(mpConsumer)
                .addVinsToConsentRequestAsync(crid, FILE_TYPE.JSON, testVin2);
        String updateInfoUrl = new ResponseAssertion(addVinsToConsentRequest)
                .statusCodeIsEqualTo(StatusCode.ACCEPTED)
                .bindAs(AsyncUpdateResponse.class)
                .getConsentRequestAsyncUpdateInfoUrl();

        Assertions.assertThat(updateInfoUrl)
                .isNotEmpty()
                .startsWith(ConsentManagementServiceUrl.getEnvUrl() + consentRequestAsyncUpdateInfo);
        String asyncId = StringUtils.substringAfter(updateInfoUrl, consentRequestAsyncUpdateInfo);

        waitForAsyncBMWRequest(asyncId);
        new ResponseAssertion(consentRequestController.getConsentRequestAsyncUpdateInfo(asyncId))
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObjectIgnoringTimeFields(new ConsentRequestAsyncUpdateInfo()
                        .status(ConsentRequestAsyncUpdateInfo.StatusEnum.FINISHED)
                        .action(ConsentRequestAsyncUpdateInfo.ActionEnum.ADD_DATA_SUBJECTS)
                        .consentRequestId(crid)
                        .id(Long.parseLong(asyncId))
                        .vinUpdateErrors(new ArrayList<>())
                );

        step.verifyConsentStatus(
                new ConsentRequestStatus()
                        .approved(0)
                        .pending(2)
                        .revoked(0)
                        .expired(0)
                        .rejected(0));
    }


}
