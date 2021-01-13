package com.here.platform.cm.steps.api;

import com.github.javafaker.Faker;
import com.here.platform.cm.controllers.UserAccountController;
import com.here.platform.cm.steps.remove.DataForRemoveCollector;
import com.here.platform.common.DataSubject;
import com.here.platform.common.strings.VIN;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.hereAccount.controllers.HereUserManagerController;
import com.here.platform.hereAccount.controllers.HereUserManagerController.HereUser;
import com.here.platform.ns.helpers.authentication.AuthController;
import io.restassured.response.Response;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;


@UtilityClass
public class UserAccountSteps {

    private final UserAccountController accountController = new UserAccountController();
    protected static Faker faker = new Faker();

    public void attachVINToUserAccount(DataSubject targetDataSubject, String targetVIN) {
        var attachVinToUserAccount = accountController.attachVinToUserAccount(
                targetVIN, AuthController.getDataSubjectToken(targetDataSubject)
        );
        StatusCodeExpects.expectOKStatusCode(attachVinToUserAccount);
    }

    public void attachDataSubjectVINToUserAccount(DataSubject targetDataSubject) {
        attachVINToUserAccount(targetDataSubject, targetDataSubject.getVin());
    }

    public void removeVINFromDataSubject(DataSubjects targetDataSubject) {
        accountController.deleteVINForUser(targetDataSubject.getVin(), targetDataSubject.getBearerToken());
    }

    public void removeVINFromDataSubject(DataSubject targetDataSubject) {
        accountController.deleteVINForUser(targetDataSubject.getVin(), AuthController.getDataSubjectToken(targetDataSubject));
    }

    public DataSubject generateNewHereAccount(int vinLength) {
        HereUser hereUser = new HereUser(faker.internet().emailAddress(), faker.internet().password(), "here");
        DataSubject dataSubjectIm = new DataSubject(
                hereUser.getEmail(),
                hereUser.getPassword(),
                VIN.generate(vinLength)
        );
        new HereUserManagerController().createHereUser(hereUser);
        DataForRemoveCollector.addHereUser(hereUser);
        return dataSubjectIm;
    }

    public void removeHereAccount(HereUser hereUser) {
        DataSubject dataSubjectIm = new DataSubject(
                hereUser.getEmail(),
                hereUser.getPassword(),
                StringUtils.EMPTY
        );
        new HereUserManagerController().deleteHereUser(hereUser);
        AuthController.deleteToken(dataSubjectIm);
    }

    public Response initHereCallback(DataSubject dataSubjectIm) {
        HereUser hereUser = new HereUser(
                dataSubjectIm.getEmail(),
                dataSubjectIm.getPass(),
                "here"
        );
        String token = new HereUserManagerController().getHereCurrentToken(hereUser);
        String userId = new HereUserManagerController()
                .getHereAccountData("Bearer " + token).getBody().jsonPath().getString("userId");

        String actualAction = "{\"hereAccountId\":\"" + userId + "\",\"action\":\"DELETE\"}";
        String encryptedAction = "eyJoZXJlQWNjb3VudElkIjoiSEVSRS1lNDVkYTA0Mi03MDY2LTQzMzEtODYyNS1iYjAzMzlhYTgwNGUiLCJhY3Rpb24iOiJERUxFVEUifQ"
                + ".BgzuLH4VIOXP9BMKQYTcxfuBf6VHBOCEBdGaWM1hJ4voFRc50qec8sPaQ3KXGPt5zBcDXVHh2TQX9SKDp24GAx8Xm1MktKYucwBnlYFUyEgIBfUOXv7fKnY2RpbD59o1FlAbB5-_IXm7aoBj0C32oMTMvw34lT-tTJDNy4DaPXjOMCXIQw1QDPTufH5jMOwWjcaXbHYGApSok2Jpw6D5__yt-PSTeEWfRSVyLzMYym94EFPPNm-SFHsYy7yjyLx2kQuOT6cQ9QNOlRC_iPh5oPL9Qaot9Ev-yHIS3pmc5s7FUctq4DSznK5cU28TkhfX-DcvxrpTa7nywSqu4-dGuQ";
        return accountController.userAccountDeleteCallback(encryptedAction);
    }

}
