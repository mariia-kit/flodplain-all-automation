package com.here.platform.hereAccount.ui;

import static com.codeborne.selenide.Selectors.withText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.sleep;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;


public class HereLoginPage {

    @Step("HERE login page is loaded")
    public HereLoginPage isLoaded() {
        $("#sign-in-form").waitUntil(Condition.visible, 10000);
        return this;
    }

    @Step("Fill realm id value")
    public HereLoginPage fillRealm(String realm) {
        $("#realm-input").val(realm);
        return this;
    }

    @Step("Fill user's email value")
    public HereLoginPage fillUserEmail(String userEmail) {
        $("#sign-in-email").val(userEmail);
        return this;
    }

    @Step("Fill user's password value")
    public HereLoginPage fillUserPassword(String userPassword) {
        $("#sign-in-password-encrypted").val(userPassword);
        return this;
    }

    @Step("Click on 'Next' button to see email field")
    public HereLoginPage clickNextEmail() {
        $("#nextEmailBtn").click();
        return this;
    }

    @Step("Click on 'Next' button to see realm field")
    public HereLoginPage clickNextRealm() {
        $("#nextRealmBtn").click();
        return this;
    }

    @Step("Click on sign in button")
    public HereLoginPage clickSignIn() {
        $("#signInBtn").click();
        $("#signInBtn").waitUntil(Condition.hidden, 10000);
        $(".spinner-overlay[data-ng-show='changings.loading']").waitUntil(Condition.hidden, 10000);
        return this;
    }

    @Step("Approve HERE consents for new user")
    public HereLoginPage approveHEREAccountConsents() {
        Configuration.clickViaJs = true;
        $(withText("The app Consent Manager needs access to some of your HERE account info"))
                .shouldBe(Condition.visible);
        SelenideElement acceptBtn = $("#authorizeFlowBtn span");
        acceptBtn.click();
        sleep(2000);
        if (acceptBtn.isDisplayed()) {
            acceptBtn.click();
        }
        Configuration.clickViaJs = false;
        return this;
    }

}
