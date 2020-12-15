package com.here.platform.cm.ui;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

import com.codeborne.selenide.Condition;
import com.here.platform.cm.enums.ConsentPageUrl;
import com.here.platform.cm.pages.LandingPage;
import com.here.platform.cm.pages.WelcomePage;
import io.qameta.allure.Issue;
import io.qameta.allure.Step;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@DisplayName("Verify Landing Pages")
@Tag("dynamic_ui")
public class LandingPagesTests extends BaseUITests {

    LandingPage landingPage = new LandingPage();

    @Test
    @Issue("NS-2761")
    @DisplayName("Verify Landing page direct access")
    public void verifyLandingPageDirect() {
        open(ConsentPageUrl.getEnvUrlRoot());
        landingPage.isLoaded();

        signUpButtonIsNotPresent();
    }

    @Test
    @Issue("NS-2760")
    @DisplayName("Verify Welcome page access though link")
    public void verifyWelcomePage() {
        String crid = "1234567890";
        open(crid);
        landingPage.isLoaded().clickOnSignUp();
        new WelcomePage()
                .isLoaded()
                .verifyPage("Ensure", "Daimler")
                .pressNext()
                .verifyHERESignInOpened();
    }

    @Step("Verify is Sign-Up on landing page is not present")
    private void signUpButtonIsNotPresent() {
        $(byText("Sign Up")).shouldBe(Condition.hidden.because("Sign Up button should be not present!"));
    }

}
