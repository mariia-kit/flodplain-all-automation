package com.here.platform.dataProviders.daimler.steps;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.refresh;
import static com.codeborne.selenide.Selenide.sleep;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.here.platform.dataProviders.daimler.DataSubjects;
import io.qameta.allure.Step;
import lombok.experimental.UtilityClass;


@UtilityClass
public class DaimlerLoginPage {

    @Step("Login data subject on Mercedes.me site")
    public void loginDataSubjectOnDaimlerSite(DataSubjects dataSubject) {
        sleep(3000); //hotfix cos of FE developer rotation
        Selenide.clearBrowserCookies();
        Selenide.clearBrowserLocalStorage();
        refresh();
        $("[name=username]").setValue(dataSubject.getUserName());
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
