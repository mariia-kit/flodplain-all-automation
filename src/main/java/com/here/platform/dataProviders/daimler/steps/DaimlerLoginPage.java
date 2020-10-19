package com.here.platform.dataProviders.daimler.steps;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.sleep;

import com.codeborne.selenide.SelenideElement;
import com.here.platform.dataProviders.daimler.DataSubjects;
import io.qameta.allure.Step;
import lombok.experimental.UtilityClass;


@UtilityClass
public class DaimlerLoginPage {

    @Step("Login data subject on Mercedes.me site")
    public void loginDataSubjectOnDaimlerSite(DataSubjects dataSubject) {
        sleep(3000); //hotfix cos of FE developer rotation
        $("#username").setValue(dataSubject.getUserName());
        $("#continue").click();
        $("#login-with-password").click();
        $("#password").setValue(dataSubject.getPass());
        $("#confirm").click();
    }

    @Step("Login data subject on Mercedes.me site")
    public void loginDataSubjectOnDaimlerSiteOld(DataSubjects dataSubject) {
        sleep(3000); //hotfix cos of FE developer rotation
        $("#name").setValue(dataSubject.getUserName());
        $("#password").setValue(dataSubject.getPass());
        $("#ciam-weblogin-auth-login-button").click();
    }

    @Step("Accept consent scopes")
    public void approveDaimlerScopesAndSubmit() {
        for (SelenideElement scope : $$("[name*='scope:mb']")) {
            scope.click();
        }
        $("#consent-btn").click();
    }

}
