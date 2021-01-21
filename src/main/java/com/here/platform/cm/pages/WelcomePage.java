package com.here.platform.cm.pages;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.sleep;
import static com.here.platform.common.strings.SBB.sbb;

import com.codeborne.selenide.Condition;
import io.qameta.allure.Step;


public class WelcomePage extends BaseCMPage {

    public WelcomePage isLoaded() {
        $(".title").waitUntil(Condition.visible, 5000)
                .shouldHave(Condition.text("Welcome to HERE")
                .because("Welcome page title not detected!"));
        return this;
    }

    @Step("Press Next button")
    public WelcomePage pressNext() {
        sleep(8000);
        $(byText("Next")).click();
        return this;
    }

    @Step("Verify Page data is correct")
    public WelcomePage verifyPage() {
        String expected = sbb()
                .append("HERE Technologies makes sure your data is transferred and handled securely.").w()
                .append("You can decide how your data and the offer are managed any time you wish.")
                .build();
        $(".subtitle p")
                .waitUntil(Condition.visible.because("Sub title expected on page!"), 5000)
                .shouldHave(Condition.text(expected).because("Welcome page sub-title not as expected!"));
        return this;
    }

    @Step("Verify HERE Sign In Page is opened")
    public WelcomePage verifyHERESignInOpened() {
        $("#form-sign-up")
                .shouldBe(Condition.visible.because("HERE sign-up form expected after Welcome page!"));
        return this;
    }
}
