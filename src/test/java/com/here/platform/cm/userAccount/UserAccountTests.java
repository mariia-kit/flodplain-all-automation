package com.here.platform.cm.userAccount;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.controllers.UserAccountController;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.enums.ConsentObject;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.rest.model.UserAccountData;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.api.UserAccountSteps;
import com.here.platform.common.DataSubject;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.annotations.CMFeatures.UserAccount;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import io.qameta.allure.Issue;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@UserAccount
@DisplayName("User Account")
public class UserAccountTests extends BaseCMTest {

    private final UserAccountController userAccountController = new UserAccountController();

    @Test
    @DisplayName("Get user account data for already registered HERE Account")
    void getUserAccountTest() {
        var dataSubject = DataSubjects.getNextBy18VINLength();
        var privateBearer = dataSubject.getBearerToken();

        var userAccountResponse = userAccountController.userAccountGetInfo(privateBearer);

        new ResponseAssertion(userAccountResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(UserAccountData.class);
    }

    @Test
    @DisplayName("Negative flow of sending GET request to get user account data with empty token")
    void getUserAccountWithoutAuthorizationTest() {
        var userAccountResponse = userAccountController.userAccountGetInfo("");

        new ResponseAssertion(userAccountResponse)
                .statusCodeIsEqualTo(StatusCode.UNAUTHORIZED)
                .expectedErrorResponse(CMErrorResponse.TOKEN_VALIDATION);
    }

    @Test
    @Issue("NS-2747")
    @DisplayName("Verify HERE callback on user account delete action")
    void verifyHereCallbackOnAccountDelete() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(provider);

        DataSubject dataSubjectIm = UserAccountSteps.generateNewHereAccount(provider.getVinLength());
        String vin = dataSubjectIm.getVin();

        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, targetContainer);

        var step = new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(vin)
                .approveConsent(vin, dataSubjectIm);
        Response callBack = UserAccountSteps.initHereCallback(dataSubjectIm);
        new ResponseAssertion(callBack)
                .expectedErrorResponse(CMErrorResponse.SIGNATURE_INVALID);
    }

}
