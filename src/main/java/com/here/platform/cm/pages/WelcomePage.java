package com.here.platform.cm.pages;

import static com.codeborne.selenide.Selenide.$;
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
        $(".buttons").click();
        return this;
    }

    @Step("Verify Page data is correct")
    public WelcomePage verifyPage(String consumerName, String providerName) {
        String expected = sbb()
                .append("As a trusted partner of ").append(consumerName).append(" and ").append(providerName)
                .append(" HERE Technologies makes sure ")
                .append("your data is transferred and handled securely. You can decide how your data")
                .append(" and the offer are managed any time you wish.")
                .build();
        $(".subtitle p")
                .waitUntil(Condition.visible.because("Sub title expected on page!"), 5000)
                .shouldHave(Condition.text(expected).because("Welcome page sub-title not as expected!"));
        return this;
    }
}
