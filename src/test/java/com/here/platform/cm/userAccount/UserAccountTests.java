package com.here.platform.cm.userAccount;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.controllers.UserAccountController;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.rest.model.UserAccountData;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.annotations.CMFeatures.UserAccount;
import com.here.platform.dataProviders.daimler.DataSubjects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@UserAccount
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

}
