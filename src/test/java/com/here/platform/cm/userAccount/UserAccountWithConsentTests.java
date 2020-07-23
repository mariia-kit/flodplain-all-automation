package com.here.platform.cm.userAccount;

import com.here.platform.aaa.HereCMBearerAuthorization;
import com.here.platform.cm.BaseCMTest;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.annotations.CMFeatures.UserAccount;
import com.here.platform.cm.controllers.UserAccountController;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentInfo.StateEnum;
import com.here.platform.cm.steps.ConsentFlowSteps;
import com.here.platform.cm.steps.ConsentRequestSteps;
import com.here.platform.cm.steps.RemoveEntitiesSteps;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VIN;
import com.here.platform.common.VinsToFile;
import com.here.platform.dataProviders.DataSubjects;
import io.qameta.allure.Issue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;


@UserAccount
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("User Account")
public class UserAccountWithConsentTests extends BaseCMTest {

    private final DataSubjects dataSubject = DataSubjects.getNext();
    private final UserAccountController userAccountController = new UserAccountController();
    private final List<String> vinsToRemove = new ArrayList<>();
    private String crid;
    private ProviderApplications targetApplication;
    private ConsentInfo targetConsentRequest;

    @BeforeEach
    void createConsentRequestAndApproveConsent() {
        targetApplication = ProviderApplications.DAIMLER_CONS_1;

        userAccountController.attachVinToUserAccount(dataSubject.vin, dataSubject.getBearerToken());
        vinsToRemove.add(dataSubject.vin);

        targetConsentRequest = ConsentRequestSteps.createConsentRequestWithVINFor(targetApplication, dataSubject.vin);
        crid = targetConsentRequest.getConsentRequestId();
        userAccountController.attachConsumerToUserAccount(crid, dataSubject.getBearerToken());

        ConsentFlowSteps.approveConsentForVIN(crid, targetApplication.container, dataSubject.vin);
        fuSleep();
    }

    @AfterEach
    void cleanUp() {
        userAccountController.deleteVINForUser(dataSubject.vin, dataSubject.getBearerToken());
        userAccountController
                .deleteConsumerForUser(targetApplication.consumer.getRealm(), dataSubject.getBearerToken());
        RemoveEntitiesSteps.forceRemoveConsentRequestWithConsents(
                crid,
                new VinsToFile(vinsToRemove.toArray(String[]::new)).json()
        );
    }

    @Test
    @Issue("NS-1709")
    @DisplayName("Forbidden to get consent with invalid token")
    void isNotPossibleToGetConsentByInvalidTokenTest() {
        var userConsentsInState = userAccountController.getConsentsForUser(
                "null", Map.of("consentRequestId", crid, "state", StateEnum.APPROVED)
        );
        new ResponseAssertion(userConsentsInState)
                .statusCodeIsEqualTo(StatusCode.UNAUTHORIZED)
                .expectedErrorResponse(CMErrorResponse.TOKEN_VALIDATION);
    }

    @Test
    @Issue("NS-1709")
    @DisplayName("Forbidden to get consent by not owner")
    void isNotPossibleToGetByNotOwnerUserConsentTest() {
        var anotherDataSubject = DataSubjects.getNext();
        var anotherToken = anotherDataSubject.getBearerToken();
        userAccountController.attachVinToUserAccount(anotherDataSubject.vin, anotherToken);

        var userConsentsInState = userAccountController.getConsentsForUser(
                anotherToken, Map.of("consentRequestId", crid, "state", ""));

        new ResponseAssertion(userConsentsInState)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEmptyArray();
    }

    @Test
    @Issue("NS-1709")
    @DisplayName("Get consents for user by consentRequestId and state")
    void getConsentRequestForUserByCridAndStateTest() {
        userAccountController.attachConsumerToUserAccount(crid, dataSubject.getBearerToken());
        var userConsentsInState = userAccountController.getConsentsForUser(
                dataSubject.getBearerToken(),
                Map.of("consentRequestId", crid, "state", "")
        );

        new ResponseAssertion(userConsentsInState)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObjectIgnoringTimeFields(List.of(targetConsentRequest
                        .consumerName(targetApplication.consumer.getConsumerName())
                        .containerDescription(targetApplication.container.containerDescription)
                        .resources(targetApplication.container.resources)
                        .state(StateEnum.APPROVED)).toArray(ConsentInfo[]::new)
                );
    }

    @Test
    @DisplayName("Get consent request with 2 consents for single user")
    void getConsentRequestWith2ConsentsForSingleUserTest() {
        var secondVin = VIN.generate(targetApplication.provider.vinLength);
        var secondLogin = HereCMBearerAuthorization.getCmToken(dataSubject);
        userAccountController.attachVinToUserAccount(secondVin, secondLogin);
        vinsToRemove.add(secondVin);
        ConsentRequestSteps.addVINsToConsentRequest(targetApplication, crid, secondVin);
        fuSleep();

        var consentInfos = userAccountController.getConsentsForUser(
                secondLogin, Map.of("consentRequestId", crid, "state", "")).as(ConsentInfo[].class);

        Assertions.assertThat(consentInfos)
                .usingElementComparatorIgnoringFields(ResponseAssertion.timeFieldsToIgnore)
                .contains(targetConsentRequest
                        .consumerName(targetApplication.consumer.getConsumerName())
                        .containerName(targetApplication.container.name)
                        .containerDescription(targetApplication.container.containerDescription)
                        .resources(targetApplication.container.resources)
                        .state(StateEnum.APPROVED)
                )
                .contains(targetConsentRequest.vinLabel(new VIN(secondVin).label())
                        .state(StateEnum.PENDING));
    }

    @Test
    @DisplayName("Get all consent requests for the user")
    void getListOfConsentRequestForUserTest() {
        userAccountController.attachConsumerToUserAccount(crid, dataSubject.getBearerToken());
        var userConsents = userAccountController.getConsentsForUser(
                dataSubject.getBearerToken(),
                Map.of("state", "")
        );

        new ResponseAssertion(userConsents)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(ConsentInfo[].class);
    }

}