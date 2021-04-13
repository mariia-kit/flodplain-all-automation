package com.here.platform.cm.consentRequests;

import static com.here.platform.cm.rest.model.ConsentInfo.StateEnum.APPROVED;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.enums.ConsentManagementServiceUrl;
import com.here.platform.cm.enums.ConsentObject;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.rest.model.AsyncUpdateResponse;
import com.here.platform.cm.rest.model.ConsentRequestAsyncUpdateInfo;
import com.here.platform.cm.rest.model.ConsentRequestStatus;
import com.here.platform.cm.rest.model.VinUpdateError;
import com.here.platform.cm.steps.api.ConsentFlowSteps;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VinsToFile;
import com.here.platform.common.VinsToFile.FILE_TYPE;
import com.here.platform.common.annotations.CMFeatures.ASYNC;
import com.here.platform.common.annotations.CMFeatures.UpdateConsentRequest;
import com.here.platform.common.strings.VIN;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("Update consent request Async")
@UpdateConsentRequest
public class UpdateConsentRequestAsyncTests extends BaseCMTest {

    private final String consentRequestAsyncUpdateInfo = "consentRequestAsyncUpdateInfo/";

    //todo add test for 2mb file

    @Test
    @ASYNC
    @DisplayName("Verify Adding Vins To ConsentRequest Async")
    void addVinsToConsentRequestTestAsync() {
        User mpConsumer = Users.MP_CONSUMER.getUser();
        MPProviders targetProvider = MPProviders.DAIMLER_REFERENCE;
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(targetProvider);
        ConsentObject consentObj = new ConsentObject(mpConsumer, targetProvider, targetContainer);
        String vin1 = VIN.generate(targetProvider.getVinLength());

        ConsentRequestSteps steps = new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest();
;
        var addVinsToConsentRequest = consentRequestController
                .withConsumerToken(mpConsumer)
                .addVinsToConsentRequestAsync(steps.getId(), FILE_TYPE.JSON, vin1);

        String updateInfoUrl = new ResponseAssertion(addVinsToConsentRequest)
                .statusCodeIsEqualTo(StatusCode.ACCEPTED)
                .bindAs(AsyncUpdateResponse.class)
                .getConsentRequestAsyncUpdateInfoUrl();
        fuSleep();
        fuSleep();

        Assertions.assertThat(updateInfoUrl)
                .isNotEmpty()
                .startsWith(ConsentManagementServiceUrl.getEnvUrl() + consentRequestAsyncUpdateInfo);
        String asyncId = StringUtils.substringAfter(updateInfoUrl, consentRequestAsyncUpdateInfo);

        consentRequestController.withConsumerToken();
        new ResponseAssertion(consentRequestController.getConsentRequestAsyncUpdateInfo(asyncId))
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObjectIgnoringTimeFields(new ConsentRequestAsyncUpdateInfo()
                        .status(ConsentRequestAsyncUpdateInfo.StatusEnum.FINISHED)
                        .action(ConsentRequestAsyncUpdateInfo.ActionEnum.ADD_DATA_SUBJECTS)
                        .consentRequestId(steps.getId())
                        .id(Long.parseLong(asyncId))
                        .vinUpdateErrors(new ArrayList<>())
                );
        steps.verifyConsentStatus(new ConsentRequestStatus()
                .approved(0)
                .pending(1)
                .revoked(0)
                .expired(0)
                .rejected(0));
    }

    @Test
    @ASYNC
    @DisplayName("Force remove approved consents from consent request Async")
    void forceRemoveApprovedDataSubjectsTestAsync() {
        User mpConsumer = Users.MP_CONSUMER.getUser();
        MPProviders targetProvider = MPProviders.DAIMLER_REFERENCE;
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(targetProvider);
        ConsentObject consentObj = new ConsentObject(mpConsumer, targetProvider, targetContainer);
        var vinToApprove = DataSubjects.getNextVinLength(targetProvider.getVinLength()).getVin();
        String vin1 = VIN.generate(targetProvider.getVinLength());

        ConsentRequestSteps steps = new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(vinToApprove, vin1);
        ConsentFlowSteps.approveConsentForVIN(steps.getId(), targetContainer, vinToApprove);

        var removeVinsAsync = consentRequestController
                .withConsumerToken()
                .forceRemoveVinsFromConsentRequestAsync(steps.getId(), FILE_TYPE.CSV, vinToApprove);

        String updateInfoUrl = new ResponseAssertion(removeVinsAsync)
                .statusCodeIsEqualTo(StatusCode.ACCEPTED)
                .bindAs(AsyncUpdateResponse.class)
                .getConsentRequestAsyncUpdateInfoUrl();

        Assertions.assertThat(updateInfoUrl)
                .isNotEmpty()
                .startsWith(ConsentManagementServiceUrl.getEnvUrl() + consentRequestAsyncUpdateInfo);
        String asyncId = StringUtils.substringAfter(updateInfoUrl, consentRequestAsyncUpdateInfo);
        fuSleep();

        consentRequestController.withConsumerToken();
        new ResponseAssertion(consentRequestController.getConsentRequestAsyncUpdateInfo(asyncId))
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObjectIgnoringTimeFields(new ConsentRequestAsyncUpdateInfo()
                        .status(ConsentRequestAsyncUpdateInfo.StatusEnum.FINISHED)
                        .action(ConsentRequestAsyncUpdateInfo.ActionEnum.REMOVE_ALL_VINS)
                        .consentRequestId(steps.getId())
                        .id(Long.parseLong(asyncId))
                        .vinUpdateErrors(new ArrayList<>())
                );
        steps.verifyConsentStatus(new ConsentRequestStatus()
                .approved(0)
                .pending(1)
                .revoked(0)
                .expired(0)
                .rejected(0));
    }

    @Test
    @ASYNC
    @DisplayName("Verify removing VINs from the consent request Asynchronously")
    void removeVinsFromConsentRequestTestAsync() {
        User mpConsumer = Users.MP_CONSUMER.getUser();
        MPProviders targetProvider = MPProviders.DAIMLER_REFERENCE;
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(targetProvider);
        ConsentObject consentObj = new ConsentObject(mpConsumer, targetProvider, targetContainer);
        var vinToApprove = DataSubjects.getNextVinLength(targetProvider.getVinLength()).getVin();
        String vin1 = VIN.generate(targetProvider.getVinLength());
        String vin2 = VIN.generate(targetProvider.getVinLength());

        ConsentRequestSteps steps = new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(vinToApprove, vin1);

        ConsentFlowSteps.approveConsentForVIN(steps.getId(), targetContainer, vinToApprove);

        var removeVinsAsync = new ResponseAssertion(consentRequestController
                .withConsumerToken()
                .removeVinsFromConsentRequestAsync(steps.getId(), new VinsToFile(vinToApprove, vin1, vin2).json()))
                .statusCodeIsEqualTo(StatusCode.ACCEPTED)
                .bindAs(AsyncUpdateResponse.class);
        Assertions.assertThat(removeVinsAsync.getApprovedVINs()).isEqualTo(List.of(vinToApprove));
        String updateInfoUrl = removeVinsAsync.getConsentRequestAsyncUpdateInfoUrl();

        Assertions.assertThat(updateInfoUrl)
                .isNotEmpty()
                .startsWith(ConsentManagementServiceUrl.getEnvUrl() + consentRequestAsyncUpdateInfo);
        String asyncId = StringUtils.substringAfter(updateInfoUrl, consentRequestAsyncUpdateInfo);
        fuSleep();

        new ResponseAssertion(consentRequestController.getConsentRequestAsyncUpdateInfo(asyncId))
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObjectIgnoringTimeFields(new ConsentRequestAsyncUpdateInfo()
                        .status(ConsentRequestAsyncUpdateInfo.StatusEnum.FINISHED)
                        .action(ConsentRequestAsyncUpdateInfo.ActionEnum.REMOVE_NON_APPROVED_VINS)
                        .consentRequestId(steps.getId())
                        .id(Long.parseLong(asyncId))
                        .vinUpdateErrors(List.of(
                                new VinUpdateError()
                                        .reason("does not exist")
                                        .vinLabel(new VIN(vin2).label()),
                                new VinUpdateError()
                                        .reason("has APPROVED status")
                                        .vinLabel(new VIN(vinToApprove).label())
                        ).stream().sorted(Comparator.comparing(VinUpdateError::getVinLabel))
                                .collect(Collectors.toList()))
                );

        steps.verifyConsentStatus(new ConsentRequestStatus()
                .approved(1)
                .pending(0)
                .revoked(0)
                .expired(0)
                .rejected(0))
        .verifyConsentStatusByVin(vinToApprove, APPROVED.getValue());
    }

    @Test
    @ASYNC
    @DisplayName("Verify error of getConsentRequestAsyncUpdateInfo not exist")
    void getConsentRequestAsyncUpdateInfoNotFound() {
        var getConsentAsync = consentRequestController
                .withConsumerToken()
                .getConsentRequestAsyncUpdateInfo(String.valueOf(Long.MAX_VALUE - 1));
        new ResponseAssertion(getConsentAsync)
                .expectedErrorResponse(CMErrorResponse.CONSENT_UPDATE_INFO_NOT_FOUND);
    }

}
