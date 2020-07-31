package com.here.platform.hereAccount;

import com.here.platform.dataProviders.DataSubjects;
import com.here.platform.ns.dto.User;
import io.qameta.allure.Step;
import lombok.experimental.UtilityClass;


@UtilityClass
public class LoginSteps {

    private final LoginPage loginPage = new LoginPage();

    @Step
    public void loginDataSubject(DataSubjects dataSubjects) {
        loginPage
                .isLoaded()
                .fillUserEmail(dataSubjects.username)
                .fillUserPassword(dataSubjects.password)
                .clickSignIn();
    }

    @Step
    public void loginMPUser(User mpUser) {
        loginPage
                .isLoaded()
                .fillRealm(mpUser.getRealm())
                .clickNext()
                .fillUserEmail(mpUser.getEmail())
                .fillUserPassword(mpUser.getPass())
                .clickSignIn();
    }

}
