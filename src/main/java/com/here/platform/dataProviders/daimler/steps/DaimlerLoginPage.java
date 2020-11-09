package com.here.platform.dataProviders.daimler.steps;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.here.platform.common.DataSubject;
import io.qameta.allure.Step;
import lombok.experimental.UtilityClass;
import org.openqa.selenium.By;


@UtilityClass
public class DaimlerLoginPage {

    private final By loginPageSelector = By.id("loginPage");

    @Step("Login data subject on Mercedes.me site")
    public void loginDataSubjectOnDaimlerSite(DataSubject dataSubject) {
        $(loginPageSelector).waitUntil(Condition.visible, 20000);
        $("#username").setValue(dataSubject.getEmail());
        $("#continue").click();
        waitForPreLoaderDisapears();
        $("#login-with-password").click();
        $("#password").setValue(dataSubject.getPass());
        $("#confirm").click();
        waitForPreLoaderDisapears();
    }

    private void waitForPreLoaderDisapears() {
        $("#loader").waitUntil(Condition.disappear, 10000);
    }

    @Step("Accept consent scopes")
    public void approveDaimlerLegalAndSubmit() {
        if (!$("#allow").isDisplayed()) {
            for (SelenideElement scope : $$("#legaltext0")) {
                scope.click();
            }
            Selenide.sleep(1000);
            $("#continue").click();
            waitForPreLoaderDisapears();
        }
    }

    @Step("Accept consent scopes")
    public void approveDaimlerScopesAndSubmit() {
        //no need to enable scopes, all of tem enabled by default
        $("#allow").click();
        waitForPreLoaderDisapears();
    }

}
