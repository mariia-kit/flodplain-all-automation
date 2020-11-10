package com.here.platform.hereAccount.ui;

import static com.codeborne.selenide.Selenide.open;

import com.codeborne.selenide.Selenide;
import com.here.platform.common.DataSubject;
import com.here.platform.ns.dto.User;
import io.qameta.allure.Step;
import lombok.experimental.UtilityClass;


@UtilityClass
public class HereLoginSteps {

    private final HereLoginPage loginPage = new HereLoginPage();

    @Step("Login as new Data subject and approve HERE account consents")
    public void loginNewDataSubjectWithHEREConsentApprove(DataSubject dataSubject) {
        signInHEREAccountUser(dataSubject)
                .approveHEREAccountConsents();
    }

    @Step("Login as registered Data subject and approve HERE account consents")
    public void loginRegisteredDataSubject(DataSubject dataSubject) {
        signInHEREAccountUser(dataSubject);
    }

    private HereLoginPage signInHEREAccountUser(DataSubject dataSubject) {
        return loginPage
                .isLoaded()
                .fillUserEmail(dataSubject.getEmail())
                .fillUserPassword(dataSubject.getPass())
                .clickSignIn();
    }

    @Step("Login as Marketplace user")
    public void loginMPUser(User mpUser) {
        loginPage
                .isLoaded()
                .fillUserEmail(mpUser.getEmail())
                .clickNextEmail()
                .fillRealm(mpUser.getRealm())
                .clickNextRealm()
                .fillUserPassword(mpUser.getPass())
                .clickSignIn();
    }

    @Step("Logout from HERE portal")
    public void logout() {
        open("logout");
        loginPage.isLoaded();
        Selenide.clearBrowserCookies();
        Selenide.clearBrowserLocalStorage();
    }

}
