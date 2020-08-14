package com.here.platform.cm.userAccount;


import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.controllers.UserAccountController;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.rest.model.UserAccountData;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VIN;
import com.here.platform.common.annotations.CMFeatures.UserAccount;
import com.here.platform.common.annotations.ErrorHandler;
import com.here.platform.dataProviders.daimler.DataSubjects;
import io.qameta.allure.Issue;
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
@DisplayName("User Account")
public class UserAccountVINTests extends BaseCMTest {

    private final UserAccountController userAccountController = new UserAccountController();

    private final DataSubjects dataSubject = DataSubjects.getNext();
    private final String privateBearer = dataSubject.getBearerToken();
    private final List<String> userVINsToRemove = new ArrayList<>();


    @BeforeEach
    void beforeEach() {
        userVINsToRemove.add(dataSubject.vin);
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

    @Test
    @DisplayName("Add VIN to the user")
    void addVinToUserTest() {
        var addVinsResponse = userAccountController.attachVinToUserAccount(dataSubject.vin, privateBearer);

        var actualVinLabels = new ResponseAssertion(addVinsResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(UserAccountData.class).getVinLabels();

        Assertions.assertThat(actualVinLabels).contains(new VIN(dataSubject.vin).label());
    }

    @Test
    @DisplayName("Add several VINs to the User")
    void addSeveralVINsToUserTest() {
        var firstAddVINsResponse = userAccountController.attachVinToUserAccount(dataSubject.vin, privateBearer);
        userVINsToRemove.add(dataSubject.vin);
        new ResponseAssertion(firstAddVINsResponse)
                .statusCodeIsEqualTo(StatusCode.OK);

        var secondVIN = VIN.generate(17);
        var secondAddVINsResponse = userAccountController.attachVinToUserAccount(secondVIN, privateBearer);
        userVINsToRemove.add(secondVIN);

        var vinLabels = new ResponseAssertion(secondAddVINsResponse).statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(UserAccountData.class).getVinLabels();

        Assertions.assertThat(vinLabels).contains(
                new VIN(dataSubject.vin).label(), new VIN(secondVIN).label()
        );
    }

    @Test
    @ErrorHandler
    @Issue("NS-1888")
    @DisplayName("Forbidden to add one VIN for another user")
    void forbiddenToAddVINToAnotherUserTest() {
        var firstAdd = userAccountController.attachVinToUserAccount(dataSubject.vin, privateBearer);
        new ResponseAssertion(firstAdd).statusCodeIsEqualTo(StatusCode.OK);

        var secondUserLogin = DataSubjects.getNext().getBearerToken();

        var secondAdd = userAccountController.attachVinToUserAccount(dataSubject.vin, secondUserLogin);
        new ResponseAssertion(secondAdd)
                .statusCodeIsEqualTo(StatusCode.CONFLICT)
                .expectedErrorResponse(CMErrorResponse.ALREADY_EXIST_EXCEPTION);
    }

    @Test
    @ErrorHandler
    @Issue("NS-1888")
    @DisplayName("Add existent VIN for the user is forbidden")
    void addExistentVINForUserTest() {
        var firstAdd = userAccountController.attachVinToUserAccount(dataSubject.vin, privateBearer);
        new ResponseAssertion(firstAdd).statusCodeIsEqualTo(StatusCode.OK);

        var addExistentVINResponse = userAccountController.attachVinToUserAccount(dataSubject.vin, privateBearer);
        new ResponseAssertion(addExistentVINResponse)
                .statusCodeIsEqualTo(StatusCode.CONFLICT)
                .expectedErrorResponse(CMErrorResponse.ALREADY_EXIST_EXCEPTION);
    }

    @Test
    @ErrorHandler
    @DisplayName("Forbidden to add VIN without Bearer token")
    void forbiddenToAddVINWithoutBearerTokenTest() {
        var addVINResponse = userAccountController.attachVinToUserAccount(dataSubject.vin, "");
        new ResponseAssertion(addVINResponse)
                .statusCodeIsEqualTo(StatusCode.UNAUTHORIZED)
                .expectedErrorResponse(CMErrorResponse.TOKEN_VALIDATION);
    }

    @Test
    @ErrorHandler
    @DisplayName("VIN not found if twice remove the VIN")
    void VINNotFoundTest() {
        var removeVinResponse = userAccountController.deleteVINForUser(dataSubject.vin, privateBearer);
        new ResponseAssertion(removeVinResponse)
                .statusCodeIsEqualTo(StatusCode.NOT_FOUND)
                .expectedErrorResponse(CMErrorResponse.VIN_NOT_FOUND_EXCEPTION);
    }

}
