package com.here.platform.cm.userAccount;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.controllers.UserAccountController;
import com.here.platform.cm.enums.CMErrorResponse;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.rest.model.UserAccountData;
import com.here.platform.cm.rest.model.VinData;
import com.here.platform.cm.steps.api.UserAccountSteps;
import com.here.platform.common.DataSubject;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.annotations.CMFeatures.UserAccount;
import com.here.platform.common.annotations.ErrorHandler;
import com.here.platform.common.strings.VIN;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.ns.helpers.authentication.AuthController;
import io.qameta.allure.Issue;
import io.qameta.allure.Issues;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UserAccount
@DisplayName("User Account Vin management")
public class UserAccountVINTests extends BaseCMTest {

    private final UserAccountController userAccountController = new UserAccountController();
    //todo implement test to check removing of consents for the VIN removing end-point

    @Test
    @DisplayName("Positive flow of adding VIN to the user account")
    void addVinToUserTest() {
        MPProviders provider = MPProviders.REFERENCE;
        DataSubject dataSubject = UserAccountSteps.generateNewHereAccount(provider.getVinLength());
        var addVinsResponse = userAccountController
                .attachVinToUserAccount(dataSubject.getVin(), AuthController.getDataSubjectToken(dataSubject));

        var actualVinsData = new ResponseAssertion(addVinsResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(UserAccountData.class).getVinsData().stream().map(VinData::getVinLabel).collect(Collectors.toList());

        Assertions.assertThat(actualVinsData).contains(new VIN(dataSubject.getVin()).label());
    }

    @Test
    @DisplayName("Positive flow of adding several VINs to the one User")
    void addSeveralVINsToUserTest() {
        MPProviders provider = MPProviders.REFERENCE;
        DataSubject dataSubject = UserAccountSteps.generateNewHereAccount(provider.getVinLength());
        var firstAddVINsResponse = userAccountController
                .attachVinToUserAccount(dataSubject.getVin(), AuthController.getDataSubjectToken(dataSubject));
        new ResponseAssertion(firstAddVINsResponse)
                .statusCodeIsEqualTo(StatusCode.OK);

        var secondVIN = VIN.generate(17);
        var secondAddVINsResponse = userAccountController
                .attachVinToUserAccount(secondVIN, AuthController.getDataSubjectToken(dataSubject));

        List<String> vinLabels = new ResponseAssertion(secondAddVINsResponse).statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(UserAccountData.class).getVinsData().stream().map(VinData::getVinLabel).collect(Collectors.toList());

        Assertions.assertThat(vinLabels).contains(new VIN(dataSubject.getVin()).label());
    }

    @Test
    @ErrorHandler
    @Issues({@Issue("NS-1888"), @Issue("NS-3024")})
    @DisplayName("Forbidden to add single VIN for several user accounts")
    void forbiddenToAddVINToAnotherUserTest() {
        MPProviders provider = MPProviders.REFERENCE;
        DataSubject dataSubject = UserAccountSteps.generateNewHereAccount(provider.getVinLength());
        var firstAdd = userAccountController
                .attachVinToUserAccount(dataSubject.getVin(), AuthController.getDataSubjectToken(dataSubject));
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
        MPProviders provider = MPProviders.REFERENCE;
        DataSubject dataSubject = UserAccountSteps.generateNewHereAccount(provider.getVinLength());
        var firstAdd = userAccountController
                .attachVinToUserAccount(dataSubject.getVin(), AuthController.getDataSubjectToken(dataSubject));
        new ResponseAssertion(firstAdd).statusCodeIsEqualTo(StatusCode.OK);

        var secondAdd = userAccountController
                .attachVinToUserAccount(dataSubject.getVin(), AuthController.getDataSubjectToken(dataSubject));
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
        MPProviders provider = MPProviders.REFERENCE;
        DataSubject dataSubject = UserAccountSteps.generateNewHereAccount(provider.getVinLength());
        var addVINResponse = userAccountController.attachVinToUserAccount(dataSubject.getVin(), "");
        new ResponseAssertion(addVINResponse)
                .statusCodeIsEqualTo(StatusCode.UNAUTHORIZED)
                .expectedErrorResponse(CMErrorResponse.AUTHORIZATION_FAILED);
    }

    @Test
    @ErrorHandler
    @DisplayName("Negative flow: VIN not found response if twice remove the VIN")
    void VINNotFoundTest() {
        MPProviders provider = MPProviders.REFERENCE;
        DataSubject dataSubject = UserAccountSteps.generateNewHereAccount(provider.getVinLength());
        var removeVinResponse = userAccountController.deleteVINForUser(dataSubject.getVin(),
                AuthController.getDataSubjectToken(dataSubject));
        new ResponseAssertion(removeVinResponse)
                .statusCodeIsEqualTo(StatusCode.NOT_FOUND)
                .expectedErrorResponse(CMErrorResponse.VIN_NOT_FOUND_EXCEPTION);
    }

}