package com.here.platform.hereAccount.ui;

import static com.codeborne.selenide.Selenide.open;

import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.ns.dto.User;
import io.qameta.allure.Step;
import lombok.experimental.UtilityClass;


@UtilityClass
public class HereLoginSteps {

    private final HereLoginPage loginPage = new HereLoginPage();

    @Step("Login data subject")
    public void loginDataSubject(DataSubjects dataSubjects) {
        loginPage
                .isLoaded()
                .fillUserEmail(dataSubjects.username)
                .fillUserPassword(dataSubjects.password)
                .clickSignIn();
    }

    @Step("Login marketplace user")
    public void loginMPUser(User mpUser) {
        loginPage
                .isLoaded()
                .fillRealm(mpUser.getRealm())
                .clickNext()
                .fillUserEmail(mpUser.getEmail())
                .fillUserPassword(mpUser.getPass())
                .clickSignIn();
    }

    @Step
    public void logout() {
        open("logout");
        loginPage.isLoaded();
    }

}
