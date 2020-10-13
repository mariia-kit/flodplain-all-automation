package com.here.platform.cm.userAccount;


import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.controllers.UserAccountController;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentInfo.StateEnum;
import com.here.platform.cm.steps.api.ConsentFlowSteps;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VIN;
import com.here.platform.common.VinsToFile;
import com.here.platform.common.annotations.CMFeatures.UserAccount;
import com.here.platform.dataProviders.daimler.DataSubjects;
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

    private final UserAccountController userAccountController = new UserAccountController();
    private final List<String> vinsToRemove = new ArrayList<>();
    private String crid;
    private ProviderApplications targetApplication = ProviderApplications.REFERENCE_CONS_1;
    private ConsentInfo targetConsentRequest;

    protected DataSubjects dataSubject = DataSubjects.getNextVinLength(targetApplication.provider.vinLength);
    protected ConsentRequestContainers testContainer = ConsentRequestContainers.generateNew(targetApplication.provider.getName());

    @BeforeEach
    void createConsentRequestAndApproveConsent() {
        var addVins = userAccountController.attachVinToUserAccount(dataSubject.getVin(), dataSubject.getBearerToken());
        new ResponseAssertion(addVins).statusCodeIsEqualTo(StatusCode.OK);
        vinsToRemove.add(dataSubject.getVin());

        targetConsentRequest = ConsentRequestSteps.createValidConsentRequest(targetApplication, dataSubject.getVin(), testContainer);
        crid = targetConsentRequest.getConsentRequestId();
        ConsentFlowSteps.approveConsentForVIN(crid, targetApplication.container, dataSubject.getVin());
        fuSleep();
    }

    @AfterEach
    void cleanUp() {
        userAccountController.deleteVINForUser(dataSubject.getVin(), dataSubject.getBearerToken());
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
        var anotherDataSubject = DataSubjects.getNextBy18VINLength();
        var anotherToken = anotherDataSubject.getBearerToken();
        userAccountController.attachVinToUserAccount(anotherDataSubject.getVin(), anotherToken);

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
        var secondLogin = dataSubject.getBearerToken();
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
        var userConsents = userAccountController.getConsentsForUser(
                dataSubject.getBearerToken(),
                Map.of("state", "")
        );

        new ResponseAssertion(userConsents)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(ConsentInfo[].class);
    }

}
