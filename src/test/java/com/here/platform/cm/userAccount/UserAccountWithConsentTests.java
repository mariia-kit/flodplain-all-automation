package com.here.platform.cm.userAccount;


import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.controllers.UserAccountController;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.Consents;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentInfo.StateEnum;
import com.here.platform.cm.steps.api.ConsentFlowSteps;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.api.ConsentRequestSteps2;
import com.here.platform.common.DataSubject;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.annotations.CMFeatures.UserAccount;
import com.here.platform.common.strings.VIN;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.hereAccount.controllers.HereUserManagerController.HereUser;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.helpers.authentication.AuthController;
import io.qameta.allure.Issue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@UserAccount
@DisplayName("User Account with Consents")
public class UserAccountWithConsentTests extends BaseCMTest {

    private final UserAccountController userAccountController = new UserAccountController();
    private final List<String> vinsToRemove = new ArrayList<>();
    private String crid;
    private final ProviderApplications targetApplication = ProviderApplications.REFERENCE_CONS_1;
    private ConsentInfo consentInfo;

    HereUser hereUser = null;
    DataSubject dataSubjectIm;

    protected ConsentRequestContainer testContainer = ConsentRequestContainers
            .generateNew(targetApplication.provider);

    @BeforeEach
    void createConsentRequestAndApproveConsent() {
        ProviderApplications targetApp = ProviderApplications.REFERENCE_CONS_1;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(targetApp.getProvider());

        hereUser = new HereUser(faker.internet().emailAddress(), faker.internet().password(), "here");
        dataSubjectIm = new DataSubject(
                hereUser.getEmail(),
                hereUser.getPassword(),
                VIN.generate(targetApp.getProvider().getVinLength())
        );
        hereUserManagerController.createHereUser(hereUser);
        String vin = dataSubjectIm.getVin();
        vinsToRemove.add(vin);
        consentInfo = Consents.generateNewConsentInfo(mpConsumer, targetContainer);
        crid = new ConsentRequestSteps2(targetContainer, consentInfo)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(vin)
                .getId();
        ConsentFlowSteps.approveConsentForVIN(crid, testContainer, vin, AuthController.getDataSubjectToken(dataSubjectIm));
        userAccountController.attachVinToUserAccount(dataSubjectIm.getVin(), AuthController.getDataSubjectToken(dataSubjectIm));
    }

    @AfterEach
    void cleanUp() {
        vinsToRemove.forEach(vin -> userAccountController.deleteVINForUser(vin, AuthController.getDataSubjectToken(dataSubjectIm)));
        if (hereUser != null) {
            hereUserManagerController.deleteHereUser(hereUser);
        }
        AuthController.deleteToken(dataSubjectIm);
    }

    @Test
    @Issue("NS-1709")
    @DisplayName("Negative flow of sending GET request to get consent with invalid token")
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
        var anotherDataSubject = DataSubjects.getNextVinLength(targetApplication.provider.vinLength);
        userAccountController.attachVinToUserAccount(anotherDataSubject.getVin(), anotherDataSubject.getBearerToken());

        var userConsentsInState = userAccountController.getConsentsForUser(
                anotherDataSubject.getBearerToken(), Map.of("consentRequestId", crid, "state", ""));

        new ResponseAssertion(userConsentsInState)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEmptyArray();
    }

    @Test
    @Issue("NS-2949")
    @DisplayName("Forbidden to get consent by empty consentRequestId")
    void isNotPossibleToGetByEmptyConsentId() {
        var userConsentsInState = userAccountController.getConsentsForUser(
                AuthController.getDataSubjectToken(dataSubjectIm),
                Map.of("consentRequestId", "", "state", "")
        );

        new ResponseAssertion(userConsentsInState)
                .statusCodeIsEqualTo(StatusCode.BAD_REQUEST)
                .expectedErrorResponse(CMErrorResponse.PARAMETER_VALIDATION);
    }

    @Test
    @Issue("NS-1709")
    @DisplayName("Get consents for user by consentRequestId and state")
    void getConsentRequestForUserByCridAndStateTest() {
        var userConsentsInState = userAccountController.getConsentsForUser(
                AuthController.getDataSubjectToken(dataSubjectIm),
                Map.of("consentRequestId", crid, "state", "")
        );

        new ResponseAssertion(userConsentsInState)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObjectIgnoringTimeFields(List.of(consentInfo
                        .consumerName(targetApplication.consumer.getName())
                        .containerDescription(targetApplication.container.containerDescription)
                        .resources(targetApplication.container.resources)
                        .state(StateEnum.APPROVED)).toArray(ConsentInfo[]::new)
                );
    }

    @Test
    @DisplayName("Get consent request with 2 consents for a user")
    void getConsentRequestWith2ConsentsForSingleUserTest() {
        var secondVin = VIN.generate(targetApplication.provider.vinLength);
        userAccountController
                .attachVinToUserAccount(secondVin, AuthController.getDataSubjectToken(dataSubjectIm));
        vinsToRemove.add(secondVin);
        ConsentRequestSteps.addVINsToConsentRequest(targetApplication, crid, secondVin);

        var consentInfos = userAccountController
                .getConsentsForUser(
                        AuthController.getDataSubjectToken(dataSubjectIm),
                        Map.of("consentRequestId", crid, "state", ""))
                .as(ConsentInfo[].class);

        Assertions.assertThat(consentInfos)
                .usingElementComparatorIgnoringFields(ResponseAssertion.timeFieldsToIgnore)
                .contains(consentInfo.vinLabel(new VIN(dataSubjectIm.getVin()).label())
                        .state(StateEnum.APPROVED))
                .contains(consentInfo.vinLabel(new VIN(secondVin).label())
                        .state(StateEnum.PENDING));
    }

    @Test
    @DisplayName("Success flow of sending GET request to return all consent requests per specific User")
    void getListOfConsentRequestForUserTest() {
        var userConsents = userAccountController.getConsentsForUser(
                AuthController.getDataSubjectToken(dataSubjectIm),
                Map.of("state", "")
        );

        new ResponseAssertion(userConsents)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(ConsentInfo[].class);
    }

}
