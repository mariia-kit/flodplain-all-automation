package com.here.platform.hereAccount.ui;

import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
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
        return this;
    }

    @Step("Approve HERE consent for new user")
    public HereLoginPage approveConsentIfPresent() {
        Selenide.sleep(5000);
        if ($("#authorizeFlowBtn").isDisplayed()) {
            $("#authorizeFlowBtn").click();
        }
        return this;
    }

}
