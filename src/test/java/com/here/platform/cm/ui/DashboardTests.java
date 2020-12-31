package com.here.platform.cm.ui;

import static com.codeborne.selenide.Selenide.open;
import static com.here.platform.cm.rest.model.ConsentInfo.StateEnum.APPROVED;
import static com.here.platform.cm.rest.model.ConsentInfo.StateEnum.PENDING;
import static com.here.platform.cm.rest.model.ConsentInfo.StateEnum.REVOKED;
import static io.qameta.allure.Allure.step;

import com.here.platform.cm.enums.ConsentObject;
import com.here.platform.cm.enums.ConsentPageUrl;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.pages.BaseCMPage.Header;
import com.here.platform.cm.pages.DashBoardPage;
import com.here.platform.cm.pages.LandingPage;
import com.here.platform.cm.pages.UserProfilePage;
import com.here.platform.cm.pages.VINEnteringPage;
import com.here.platform.cm.steps.api.ConsentFlowSteps;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.api.UserAccountSteps;
import com.here.platform.common.DataSubject;
import com.here.platform.common.annotations.CMFeatures.Dashboard;
import com.here.platform.hereAccount.ui.HereLoginSteps;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@Tag("dynamic_ui")
@Dashboard
@DisplayName("Dashboard")
public class DashboardTests extends BaseUITests {

    @Test
    @Feature("Dashboard show orders")
    @DisplayName("Consent requests are displayed in all statuses on Offers Dashboard pages")
    void verifyDashBoardTest() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        DataSubject dataSubjectIm = UserAccountSteps.generateNewHereAccount(provider.getVinLength());
        var vin = dataSubjectIm.getVin();
        ConsentRequestContainer targetContainer1 = ConsentRequestContainers.generateNew(provider);
        ConsentRequestContainer targetContainer2 = ConsentRequestContainers.generateNew(provider);

        ConsentObject consentObj1 = new ConsentObject(mpConsumer, provider, targetContainer1);
        ConsentObject consentObj2 = new ConsentObject(mpConsumer, provider, targetContainer2);

        var consentRequestId1 = new ConsentRequestSteps(consentObj1)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(dataSubjectIm.getVin())
                .getId();

        var consentRequestId2 = new ConsentRequestSteps(consentObj2)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(dataSubjectIm.getVin())
                .getId();

        open(consentRequestId1);
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginNewDataSubjectWithHEREConsentApprove(dataSubjectIm);
        new VINEnteringPage().isLoaded().fillVINAndContinue(vin);
        String token = getUICmToken();

        step("Verify offers on dashboard in PENDING status", () -> {
                    DashBoardPage.header.openDashboardNewTab()
                            .isLoaded()
                            .verifyConsentOfferTab(0, consentObj2.getConsent(), vin, PENDING)
                            .verifyConsentOfferTab(1, consentObj1.getConsent(), vin, PENDING);
                }
        );

        step("Approve consents via API", () -> {
                    ConsentFlowSteps.approveConsentForVIN(consentRequestId1, targetContainer1, vin, token);
                    ConsentFlowSteps.approveConsentForVIN(consentRequestId2, targetContainer2, vin, token);
                }
        );

        step("Verify offers on dashboard in APPROVED status", () -> {
                    DashBoardPage.header.openDashboardAcceptedTab()
                            .verifyConsentOfferTab(0, consentObj2.getConsent(), vin, APPROVED)
                            .verifyConsentOfferTab(1, consentObj1.getConsent(), vin, APPROVED);
                }
        );

        step("Revoke consents via API", () -> {
                    ConsentFlowSteps.revokeConsentForVIN(consentRequestId1, vin, token);
                    ConsentFlowSteps.revokeConsentForVIN(consentRequestId2, vin, token);
                }
        );

        step("Verify offers on dashboard in REVOKED status", () -> {
                    DashBoardPage.header.openDashboardRevokedTab()
                            .verifyConsentOfferTab(0, consentObj2.getConsent(), vin, REVOKED)
                            .verifyConsentOfferTab(1, consentObj1.getConsent(), vin, REVOKED);
                }
        );
    }

    @Test
    @DisplayName("Open empty Dashboard page for just registered user")
    void openEmptyDashboardPageTest() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        DataSubject dataSubjectIm = UserAccountSteps.generateNewHereAccount(provider.getVinLength());
        open(ConsentPageUrl.getEnvUrlRoot());
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginNewDataSubjectWithHEREConsentApprove(dataSubjectIm);

        new VINEnteringPage().isLoaded();
    }

    //todo mark tests with @Feature(or some other) annotation specific user flow that is checked
    // https://confluence.in.here.com/display/OLP/Consent+Management+User+Flows

    @Test
    @Issue("NS-2464")
    @Feature("Sign Out from CM web")
    @DisplayName("Sign out and redirect to the 'Landing page'")
    void clickSignOutAndRedirectToLandingPage() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        DataSubject dataSubjectIm = UserAccountSteps.generateNewHereAccount(provider.getVinLength());
        UserAccountSteps.attachDataSubjectVINToUserAccount(dataSubjectIm);
        open(ConsentPageUrl.getEnvUrlRoot());
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginRegisteredDataSubject(dataSubjectIm);

        new DashBoardPage().isLoaded();

        new Header().openDashboardUserAvatarTab();
        new UserProfilePage()
                .isLoaded()
                .clickOnSignOut();

        new LandingPage().isLoaded();
    }

}
