package com.here.platform.cm.userAccount;


import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.controllers.UserAccountController;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.rest.model.UserAccountData;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.annotations.CMFeatures.UserAccount;
import com.here.platform.common.annotations.ErrorHandler;
import com.here.platform.common.strings.VIN;
import com.here.platform.dataProviders.daimler.DataSubjects;
import io.qameta.allure.Issue;
import io.qameta.allure.Issues;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;


@UserAccount
@Execution(ExecutionMode.SAME_THREAD)
@DisplayName("User Account Vin management")
public class UserAccountVINTests extends BaseCMTest {

    private final UserAccountController userAccountController = new UserAccountController();

    private final DataSubjects dataSubject = DataSubjects.getNextBy18VINLength();
    private final String privateBearer = dataSubject.getBearerToken();
    private final List<String> userVINsToRemove = new ArrayList<>();


    @BeforeEach
    void beforeEach() {
        userVINsToRemove.add(dataSubject.getVin());
        for (String vinToRemove : userVINsToRemove) {
            userAccountController.deleteVINForUser(vinToRemove, privateBearer);
        }
    }

    @AfterEach
    void afterEach() {
        for (String vinToRemove : userVINsToRemove) {
            userAccountController.deleteVINForUser(vinToRemove, privateBearer);
        }
    }

    //todo implement test to check removing of consents for the VIN removing end-point

    @Test
    @DisplayName("Positive flow of adding VIN to the user account")
    void addVinToUserTest() {
        var addVinsResponse = userAccountController.attachVinToUserAccount(dataSubject.getVin(), privateBearer);

        var actualVinLabels = new ResponseAssertion(addVinsResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(UserAccountData.class).getVinLabels();

        Assertions.assertThat(actualVinLabels).contains(new VIN(dataSubject.getVin()).label());
    }

    @Test
    @DisplayName("Positive flow of adding several VINs to the one User")
    void addSeveralVINsToUserTest() {
        var firstAddVINsResponse = userAccountController.attachVinToUserAccount(dataSubject.getVin(), privateBearer);
        userVINsToRemove.add(dataSubject.getVin());
        new ResponseAssertion(firstAddVINsResponse)
                .statusCodeIsEqualTo(StatusCode.OK);

        var secondVIN = VIN.generate(17);
        var secondAddVINsResponse = userAccountController.attachVinToUserAccount(secondVIN, privateBearer);
        userVINsToRemove.add(secondVIN);

        var vinLabels = new ResponseAssertion(secondAddVINsResponse).statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(UserAccountData.class).getVinLabels();

        Assertions.assertThat(vinLabels).contains(
                new VIN(dataSubject.getVin()).label(), new VIN(secondVIN).label()
        );
    }

    @Test
    @ErrorHandler
    @Issues({@Issue("NS-1888"), @Issue("NS-3024")})
    @DisplayName("Forbidden to add single VIN for several user accounts")
    void forbiddenToAddVINToAnotherUserTest() {
        var firstAdd = userAccountController.attachVinToUserAccount(dataSubject.getVin(), privateBearer);
        new ResponseAssertion(firstAdd).statusCodeIsEqualTo(StatusCode.OK);

        var secondUserLogin = DataSubjects.getNextBy18VINLength().getBearerToken();

        var secondAdd = userAccountController.attachVinToUserAccount(dataSubject.getVin(), secondUserLogin);
        new ResponseAssertion(secondAdd)
                .statusCodeIsEqualTo(StatusCode.CONFLICT)
                .expectedErrorResponse(CMErrorResponse.ALREADY_EXIST_EXCEPTION);
        new ResponseAssertion(secondAdd)
                .expectedErrorCause("Provided VIN already used by other account");
    }

    @Test
    @ErrorHandler
    @Issues({@Issue("NS-1888"), @Issue("NS-3024")})
    @DisplayName("Forbidden to add single VIN to the same user")
    void forbiddenToAddVINToSameUserTest() {
        var firstAdd = userAccountController.attachVinToUserAccount(dataSubject.getVin(), privateBearer);
        new ResponseAssertion(firstAdd).statusCodeIsEqualTo(StatusCode.OK);

        var secondAdd = userAccountController.attachVinToUserAccount(dataSubject.getVin(), privateBearer);
        new ResponseAssertion(secondAdd)
                .statusCodeIsEqualTo(StatusCode.CONFLICT)
                .expectedErrorResponse(CMErrorResponse.ALREADY_EXIST_EXCEPTION);
        new ResponseAssertion(secondAdd)
                .expectedErrorCause("Provided VIN already used by this account");
    }

    @Test
    @ErrorHandler
    @DisplayName("Negative flow of adding VIN with empty Bearer token")
    void forbiddenToAddVINWithoutBearerTokenTest() {
        var addVINResponse = userAccountController.attachVinToUserAccount(dataSubject.getVin(), "");
        new ResponseAssertion(addVINResponse)
                .statusCodeIsEqualTo(StatusCode.UNAUTHORIZED)
                .expectedErrorResponse(CMErrorResponse.TOKEN_VALIDATION);
    }

    @Test
    @ErrorHandler
    @DisplayName("Negative flow: VIN not found response if twice remove the VIN")
    void VINNotFoundTest() {
        var removeVinResponse = userAccountController.deleteVINForUser(dataSubject.getVin(), privateBearer);
        new ResponseAssertion(removeVinResponse)
                .statusCodeIsEqualTo(StatusCode.NOT_FOUND)
                .expectedErrorResponse(CMErrorResponse.VIN_NOT_FOUND_EXCEPTION);
    }

}
