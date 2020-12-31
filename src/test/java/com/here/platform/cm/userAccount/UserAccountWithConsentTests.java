package com.here.platform.cm.userAccount;


import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.controllers.UserAccountController;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.enums.ConsentObject;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentInfo.StateEnum;
import com.here.platform.cm.steps.api.ConsentFlowSteps;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.api.UserAccountSteps;
import com.here.platform.common.DataSubject;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.annotations.CMFeatures.UserAccount;
import com.here.platform.common.strings.VIN;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import com.here.platform.ns.helpers.authentication.AuthController;
import io.qameta.allure.Issue;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@UserAccount
@DisplayName("User Account with Consents")
public class UserAccountWithConsentTests extends BaseCMTest {

    private final UserAccountController userAccountController = new UserAccountController();


    public ConsentObject prepareConsentRequestAndApproveConsent() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(provider);

        DataSubject dataSubjectIm = UserAccountSteps.generateNewHereAccount(provider.getVinLength());
        String vin = dataSubjectIm.getVin();

        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, targetContainer);
        consentObj.setDataSubject(dataSubjectIm);
        var crid = new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(vin)
                .getId();
        ConsentFlowSteps.approveConsentForVIN(crid, targetContainer, vin, AuthController.getDataSubjectToken(dataSubjectIm));
        userAccountController.attachVinToUserAccount(dataSubjectIm.getVin(), AuthController.getDataSubjectToken(dataSubjectIm));
        return consentObj;
    }

    @Test
    @Issue("NS-1709")
    @DisplayName("Negative flow of sending GET request to get consent with invalid token")
    void isNotPossibleToGetConsentByInvalidTokenTest() {
        ConsentObject consentObject = prepareConsentRequestAndApproveConsent();
        var userConsentsInState = userAccountController.getConsentsForUser(
                "null", Map.of("consentRequestId", consentObject.getCrid(), "state", StateEnum.APPROVED)
        );
        new ResponseAssertion(userConsentsInState)
                .statusCodeIsEqualTo(StatusCode.UNAUTHORIZED)
                .expectedErrorResponse(CMErrorResponse.TOKEN_VALIDATION);
    }

    @Test
    @Issue("NS-1709")
    @DisplayName("Forbidden to get consent by not owner")
    void isNotPossibleToGetByNotOwnerUserConsentTest() {
        ConsentObject consentObject = prepareConsentRequestAndApproveConsent();
        var anotherDataSubject = DataSubjects.getNextVinLength(consentObject.getProvider().getVinLength());
        userAccountController.attachVinToUserAccount(anotherDataSubject.getVin(), anotherDataSubject.getBearerToken());

        var userConsentsInState = userAccountController.getConsentsForUser(
                anotherDataSubject.getBearerToken(), Map.of("consentRequestId", consentObject.getCrid(), "state", ""));

        new ResponseAssertion(userConsentsInState)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEmptyArray();
    }

    @Test
    @Issue("NS-2949")
    @DisplayName("Forbidden to get consent by empty consentRequestId")
    void isNotPossibleToGetByEmptyConsentId() {
        ConsentObject consentObject = prepareConsentRequestAndApproveConsent();
        var userConsentsInState = userAccountController.getConsentsForUser(
                AuthController.getDataSubjectToken(consentObject.getDataSubject()),
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
        ConsentObject consentObject = prepareConsentRequestAndApproveConsent();
        var userConsentsInState = userAccountController.getConsentsForUser(
                AuthController.getDataSubjectToken(consentObject.getDataSubject()),
                Map.of("consentRequestId", consentObject.getCrid(), "state", "")
        );

        new ResponseAssertion(userConsentsInState)
                .statusCodeIsEqualTo(StatusCode.OK)
                .responseIsEqualToObjectIgnoringTimeFields(List.of(consentObject.getConsent()
                        .state(StateEnum.APPROVED)).toArray(ConsentInfo[]::new)
                );
    }

    @Test
    @DisplayName("Get consent request with 2 consents for a user")
    void getConsentRequestWith2ConsentsForSingleUserTest() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(provider);

        DataSubject dataSubjectIm = UserAccountSteps.generateNewHereAccount(provider.getVinLength());
        String vin = dataSubjectIm.getVin();
        var secondVin = VIN.generate(provider.getVinLength());

        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, targetContainer);
        var crid = new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(vin, secondVin)
                .getId();
        ConsentFlowSteps.approveConsentForVIN(crid, targetContainer, vin, AuthController.getDataSubjectToken(dataSubjectIm));
        userAccountController.attachVinToUserAccount(vin, AuthController.getDataSubjectToken(dataSubjectIm));
        userAccountController.attachVinToUserAccount(secondVin, AuthController.getDataSubjectToken(dataSubjectIm));

        var consentInfos = userAccountController
                .getConsentsForUser(
                        AuthController.getDataSubjectToken(dataSubjectIm),
                        Map.of("consentRequestId", crid, "state", ""))
                .as(ConsentInfo[].class);

        Assertions.assertThat(consentInfos)
                .usingElementComparatorIgnoringFields(ResponseAssertion.timeFieldsToIgnore)
                .contains(consentObj.getConsent().vinLabel(new VIN(dataSubjectIm.getVin()).label())
                        .state(StateEnum.APPROVED))
                .contains(consentObj.getConsent().vinLabel(new VIN(secondVin).label())
                        .state(StateEnum.PENDING));
    }

    @Test
    @DisplayName("Success flow of sending GET request to return all consent requests per specific User")
    void getListOfConsentRequestForUserTest() {
        ConsentObject consentObject = prepareConsentRequestAndApproveConsent();
        var userConsents = userAccountController.getConsentsForUser(
                AuthController.getDataSubjectToken(consentObject.getDataSubject()),
                Map.of("state", "")
        );

        new ResponseAssertion(userConsents)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(ConsentInfo[].class);
    }

}
