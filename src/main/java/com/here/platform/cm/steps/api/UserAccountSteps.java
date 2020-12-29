package com.here.platform.cm.steps.api;

import com.here.platform.cm.controllers.UserAccountController;
import com.here.platform.common.DataSubject;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.ns.helpers.authentication.AuthController;
import lombok.experimental.UtilityClass;


@UtilityClass
public class UserAccountSteps {

    private final UserAccountController accountController = new UserAccountController();

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

}
