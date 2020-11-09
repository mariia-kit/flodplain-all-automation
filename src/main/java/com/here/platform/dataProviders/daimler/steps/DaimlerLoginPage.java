package com.here.platform.dataProviders.daimler.steps;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.sleep;

import com.codeborne.selenide.SelenideElement;
import com.here.platform.common.DataSubject;
import io.qameta.allure.Step;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;


@UtilityClass
public class DaimlerLoginPage {

    @Step("Login data subject on Mercedes.me site")
    public void loginDataSubjectOnDaimlerSite(DataSubject dataSubject) {
        sleep(3000); //hotfix cos of FE developer rotation
        $("#username").setValue(dataSubject.getEmail());
        $("#continue").click();
        $("#login-with-password").click();
        $("#password").setValue(dataSubject.getPass());
        $("#confirm").click();
    }

    @Step("Login data subject on Mercedes.me site")
    public void loginDataSubjectOnDaimlerSiteOld(DataSubject dataSubject) {
        sleep(3000); //hotfix cos of FE developer rotation
        $("#name").setValue(dataSubject.getEmail());
        $("#password").setValue(dataSubject.getPass());
        $("#ciam-weblogin-auth-login-button").click();
    }

    @Step("Accept consent scopes")
    @SneakyThrows
    public void approveDaimlerLegalAndSubmit() {
        if (!$("#allow").isDisplayed()) {
            for (SelenideElement scope : $$("#legaltext0")) {
                scope.click();
            }
            Thread.sleep(1000);
            $("#continue").click();
        }
    }

    @Step("Accept consent scopes")
    public void approveDaimlerScopesAndSubmit() {
        //no need to enable scopes, all of tem enabled by default
        $("#allow").click();
    }

    @Step("Accept consent scopes")
    public void approveDaimlerScopesAndSubmitOld() {
        for (SelenideElement scope : $$("[name*='scope:mb']")) {
            scope.click();
        }
        $("#consent-btn").click();
    }

}
