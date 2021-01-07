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

}
