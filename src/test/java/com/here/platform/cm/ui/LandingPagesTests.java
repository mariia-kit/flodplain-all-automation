package com.here.platform.cm.ui;

import static com.codeborne.selenide.Selenide.open;

import com.here.platform.cm.enums.ConsentPageUrl;
import com.here.platform.cm.pages.LandingPage;
import com.here.platform.cm.pages.WelcomePage;
import io.qameta.allure.Issue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@DisplayName("Verify Landing Pages")
@Tag("dynamic_ui")
public class LandingPagesTests extends BaseUITests {

    LandingPage landingPage = new LandingPage();

    @Test
    @Issue("NS-2761")
    @Disabled("Disabled until bug NS-3054")
    @DisplayName("Verify Landing page direct access")
    public void verifyLandingPageDirect() {
        open(ConsentPageUrl.getEnvUrlRoot());
        landingPage.isLoaded().isSignUpNotPresent();
    }

    @Test
    @Issue("NS-2761")
    @Disabled("Disabled until bug NS-3054")
    @DisplayName("Verify Landing page through link access")
    public void verifyLandingPageThroughLink() {
        String crid = "1234567890";
        open(crid);
        landingPage.isLoaded().isSignUpPresent();
    }

    @Test
    @Issue("NS-2760")
    @Disabled("Disabled until bug NS-3054")
    @DisplayName("Verify Welcome page access though link")
    public void verifyWelcomePage() {
        String crid = "1234567890";
        open(crid);
        landingPage.isLoaded().isSignUpPresent().signUp();
        new WelcomePage()
                .isLoaded()
                .verifyPage("Ensure", "Daimler")
                .pressNext();
    }
}
