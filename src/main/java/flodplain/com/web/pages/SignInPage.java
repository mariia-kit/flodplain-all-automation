package flodplain.com.web.pages;


import static com.codeborne.selenide.Selenide.$;

import io.qameta.allure.Step;


public class SignInPage {

    public SignInPage isLoaded() {
        return this;
    }

    @Step("Click Sign In button on Login page")
    public SignInPage clickSignIn() {
        $(".").click();
        return this;
    }

}
