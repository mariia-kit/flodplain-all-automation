package com.here.platform.cm.consentRequests.dataSubjects;

import static com.here.platform.cm.rest.model.ConsentInfo.StateEnum.APPROVED;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.controllers.ConsentStatusController;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.enums.ConsentManagementServiceUrl;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPConsumers;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.rest.model.AsyncUpdateResponse;
import com.here.platform.cm.rest.model.ConsentRequestAsyncUpdateInfo;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.rest.model.ConsentRequestIdResponse;
import com.here.platform.cm.rest.model.ConsentRequestStatus;
import com.here.platform.cm.rest.model.ConsentStatus;
import com.here.platform.cm.rest.model.VinUpdateError;
import com.here.platform.cm.steps.api.ConsentFlowSteps;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.api.OnboardingSteps;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VIN;
import com.here.platform.common.VinsToFile;
import com.here.platform.common.annotations.CMFeatures.UpdateConsentRequest;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.Containers;
import com.here.platform.ns.helpers.Steps;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("Update consent request Async")
@UpdateConsentRequest
public class UpdateConsentRequestAsyncTests extends BaseCMTest {

    private final ProviderApplications targetApp = ProviderApplications.REFERENCE_CONS_1;
    private final String consentRequestAsyncUpdateInfo = "consentRequestAsyncUpdateInfo/";
    private final MPConsumers mpConsumer = targetApp.consumer;

    private final int vinLength = targetApp.provider.vinLength;
    private final String
            vin1 = VIN.generate(vinLength),
            vin2 = VIN.generate(vinLength),
            vin3 = VIN.generate(vinLength);

    Container container = Containers.generateNew(targetApp.provider.getName());
    protected ConsentRequestContainers testContainer = ConsentRequestContainers.getById(container.getId());
    private final ConsentRequestData testConsentRequest = new ConsentRequestData()
            .consumerId(mpConsumer.getRealm())
            .providerId(targetApp.provider.getName())
            .title(faker.gameOfThrones().quote())
            .purpose(faker.commerce().productName())
            .privacyPolicy(faker.internet().url())
            .containerId(testContainer.id);
    private String crid;
    private File testFileWithVINs = null;

    @BeforeEach
    void onboardApplicationForProviderAndConsumer() {
        Steps.createRegularContainer(container);
        OnboardingSteps onboard = new OnboardingSteps(targetApp.provider, targetApp.consumer.getRealm());
        onboard.onboardTestProvider();
        onboard.onboardTestProviderApplication(
                container.getName(),
                targetApp.container.clientId,
                targetApp.container.clientSecret);
        crid = ConsentRequestSteps.createConsentRequestFor(
                targetApp.provider.getName(),
                targetApp.consumer.getConsumerName(),
                targetApp.consumer.getRealm(),
                testContainer.id)
                .getConsentRequestId();
    }

    @AfterEach
    void cleanUp() {
        fuSleep();
        RemoveEntitiesSteps.cascadeForceRemoveConsentRequest(crid, testFileWithVINs, testConsentRequest);
    }

    @Test
    @Disabled("cos of removing VINs is not working fine for this test")
    @DisplayName("Verify Adding Vins To ConsentRequest Async")
    void addVinsToConsentRequestTestAsync() {
        testFileWithVINs = new VinsToFile(vin1).json();
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

        new ResponseAssertion(consentRequestController.getConsentRequestAsyncUpdateInfo(asyncId))
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObjectIgnoringTimeFields(new ConsentRequestAsyncUpdateInfo()
                        .status(ConsentRequestAsyncUpdateInfo.StatusEnum.FINISHED)
                        .action(ConsentRequestAsyncUpdateInfo.ActionEnum.ADD_DATA_SUBJECTS)
                        .consentRequestId(crid)
                        .id(Long.parseLong(asyncId))
                        .vinUpdateErrors(new ArrayList<>())
                );

        new ResponseAssertion(consentRequestController.getStatusForConsentRequestById(crid))
                .responseIsEqualToObject(new ConsentRequestStatus()
                        .approved(0)
                        .pending(1)
                        .revoked(0)
                        .expired(0)
                        .rejected(0)
                );
    }

    @Test
    @DisplayName("Force remove approved consents from consent request Async")
    void forceRemoveApprovedDataSubjectsTestAsync() {
        var vinToApprove = DataSubjects.getNextVinLength(targetApp.provider.vinLength).getVin();
        testFileWithVINs = new VinsToFile(vinToApprove, vin2, vin3).json();
        consentRequestController
                .withConsumerToken(mpConsumer)
                .addVinsToConsentRequest(crid, testFileWithVINs);
        fuSleep();

        ConsentFlowSteps.approveConsentForVIN(crid, testContainer, vinToApprove);
        fuSleep();

        var removeVinsAsync = consentRequestController.
                forceRemoveVinsFromConsentRequestAsync(crid, new VinsToFile(vinToApprove, vin2).csv());

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
                        .consentRequestId(crid)
                        .id(Long.parseLong(asyncId))
                        .vinUpdateErrors(new ArrayList<>())
                );

        new ResponseAssertion(consentRequestController.getStatusForConsentRequestById(crid))
                .responseIsEqualToObject(new ConsentRequestStatus()
                        .approved(0)
                        .pending(1)
                        .revoked(0)
                        .expired(0)
                        .rejected(0)
                );
    }

    @Test
    @DisplayName("Verify Remove Vins From ConsentRequest Async")
    void removeVinsFromConsentRequestTestAsync() {
        var vinToApprove = DataSubjects.getNextVinLength(targetApp.provider.vinLength).getVin();
        testFileWithVINs = new VinsToFile(vinToApprove, vin2, vin3).json();
        consentRequestController
                .withConsumerToken(mpConsumer)
                .addVinsToConsentRequest(crid, testFileWithVINs);
        fuSleep();

        ConsentFlowSteps.approveConsentForVIN(crid, testContainer, vinToApprove);
        fuSleep();

        var removeVinsAsync = new ResponseAssertion(consentRequestController
                .removeVinsFromConsentRequestAsync(crid, new VinsToFile(vinToApprove, vin1, vin2).json()))
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
                        .consentRequestId(crid)
                        .id(Long.parseLong(asyncId))
                        .vinUpdateErrors(List.of(
                                new VinUpdateError()
                                        .reason("does not exist")
                                        .vinLabel(new VIN(vin1).label()),
                                new VinUpdateError()
                                        .reason("has APPROVED status")
                                        .vinLabel(new VIN(vinToApprove).label())
                        ).stream().sorted(Comparator.comparing(VinUpdateError::getVinLabel))
                                .collect(Collectors.toList()))
                );
        consentRequestController.withConsumerToken();
        new ResponseAssertion(consentRequestController.getStatusForConsentRequestById(crid))
                .responseIsEqualToObject(new ConsentRequestStatus()
                        .approved(1)
                        .pending(1)
                        .revoked(0)
                        .expired(0)
                        .rejected(0)
                );

        var consentStatusByIdAndVinResponse = new ConsentStatusController()
                .withConsumerToken(mpConsumer)
                .getConsentStatusByIdAndVin(crid, vinToApprove);
        new ResponseAssertion(consentStatusByIdAndVinResponse).statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObject(new ConsentStatus()
                        .consentRequestId(crid)
                        .vin(vinToApprove)
                        .state(APPROVED.getValue())
                );
    }

    @Test
    @DisplayName("Verify error of getConsentRequestAsyncUpdateInfo not exist")
    void getConsentRequestAsyncUpdateInfoNotFound() {
        crid = null;
        var getConsentAsync = consentRequestController
                .withConsumerToken(mpConsumer)
                .getConsentRequestAsyncUpdateInfo(String.valueOf(Long.MAX_VALUE - 1));
        new ResponseAssertion(getConsentAsync)
                .expectedErrorResponse(CMErrorResponse.CONSENT_UPDATE_INFO_NOT_FOUND);
    }

}
