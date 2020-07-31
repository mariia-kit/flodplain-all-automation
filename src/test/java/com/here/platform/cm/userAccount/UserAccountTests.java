package com.here.platform.cm.userAccount;

import com.here.platform.aaa.HereCMBearerAuthorization;
import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.controllers.UserAccountController;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.rest.model.UserAccountData;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.annotations.CMFeatures.UserAccount;
import com.here.platform.dataProviders.DataSubjects;
import io.qameta.allure.Issue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@UserAccount
@Issue("OLPPORT-2678")
@DisplayName("User Account")
public class UserAccountTests extends BaseCMTest {

    private final UserAccountController userAccountController = new UserAccountController();

    @Test
    @DisplayName("Get registered user account data")
    void getUserAccountTest() {
        var dataSubject = DataSubjects.getNext();
        var privateBearer = HereCMBearerAuthorization.getCmToken(dataSubject);

        var userAccountResponse = userAccountController.userAccountGetInfo(privateBearer);

        new ResponseAssertion(userAccountResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(UserAccountData.class);
    }

    @Test
    @DisplayName("Get user account data forbidden with empty token")
    void getUserAccountWithoutAuthorizationTest() {
        var userAccountResponse = userAccountController.userAccountGetInfo("");

        new ResponseAssertion(userAccountResponse)
                .statusCodeIsEqualTo(StatusCode.UNAUTHORIZED)
                .expectedErrorResponse(CMErrorResponse.TOKEN_VALIDATION);
    }

}
