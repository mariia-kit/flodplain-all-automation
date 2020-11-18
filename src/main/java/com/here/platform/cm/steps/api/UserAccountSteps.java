package com.here.platform.cm.steps.api;

import com.here.platform.aaa.HERECMTokenController;
import com.here.platform.cm.controllers.UserAccountController;
import com.here.platform.dataProviders.daimler.DataSubjects;
import lombok.experimental.UtilityClass;


@UtilityClass
public class UserAccountSteps {

    private final UserAccountController accountController = new UserAccountController();
    private final HERECMTokenController hereUserController = new HERECMTokenController();

    public void attachVINToUserAccount(DataSubjects targetDataSubject, String targetVIN) {
        var attachVinToUserAccount = accountController.attachVinToUserAccount(
                targetVIN, targetDataSubject.getBearerToken()
        );
        StatusCodeExpects.expectOKStatusCode(attachVinToUserAccount);
    }

    public void attachDataSubjectVINToUserAccount(DataSubjects targetDataSubject) {
        attachVINToUserAccount(targetDataSubject, targetDataSubject.getVin());
    }

    public void removeVINFromDataSubject(DataSubjects targetDataSubject) {
        accountController.deleteVINForUser(targetDataSubject.getVin(), targetDataSubject.getBearerToken());
    }

}
