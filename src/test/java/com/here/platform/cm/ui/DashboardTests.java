package com.here.platform.cm.ui;

import static com.codeborne.selenide.Selenide.open;
import static com.here.platform.cm.rest.model.ConsentInfo.StateEnum.APPROVED;
import static com.here.platform.cm.rest.model.ConsentInfo.StateEnum.PENDING;
import static com.here.platform.cm.rest.model.ConsentInfo.StateEnum.REVOKED;
import static io.qameta.allure.Allure.step;

import com.here.platform.cm.enums.ConsentPageUrl;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.pages.BaseCMPage.Header;
import com.here.platform.cm.pages.DashBoardPage;
import com.here.platform.cm.pages.LandingPage;
import com.here.platform.cm.pages.PurposePage;
import com.here.platform.cm.pages.UserProfilePage;
import com.here.platform.cm.pages.VINEnteringPage;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.steps.api.ConsentFlowSteps;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.cm.steps.ui.OfferDetailsPageSteps;
import com.here.platform.cm.steps.ui.SuccessConsentPageSteps;
import com.here.platform.common.DataSubject;
import com.here.platform.common.VinsToFile;
import com.here.platform.common.annotations.CMFeatures.Dashboard;
import com.here.platform.common.annotations.CMFeatures.Purpose;
import com.here.platform.common.strings.VIN;
import com.here.platform.dataProviders.reference.steps.ReferenceApprovePage;
import com.here.platform.hereAccount.controllers.HereUserManagerController.HereUser;
import com.here.platform.hereAccount.ui.HereLoginSteps;
import com.here.platform.ns.helpers.authentication.AuthController;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@Tag("dynamic_ui")
@Dashboard
public class DashboardTests extends BaseUITests {

    private final List<String> cridsToRemove = new ArrayList<>();
    HereUser hereUser = null;
    DataSubject dataSubjectIm;

    @BeforeEach
    void createHereUserAccount() {
        hereUser = new HereUser(faker.internet().emailAddress(), faker.internet().password(), "here");
        dataSubjectIm = new DataSubject(
                hereUser.getEmail(),
                hereUser.getPassword(),
                VIN.generate(providerApplication.provider.vinLength)
        );
        hereUserManagerController.createHereUser(hereUser);
    }

    @AfterEach
    void forceRemoveConsentRequestAndDeleteUser() {
        var privateBearer = AuthController.getDataSubjectToken(dataSubjectIm);
        userAccountController.deleteVINForUser(dataSubjectIm.getVin(), privateBearer);
        for (String crid : cridsToRemove) {
            RemoveEntitiesSteps.forceRemoveConsentRequestWithConsents(
                    crid,
                    new VinsToFile(dataSubjectIm.getVin()).json()
            );
        }
        AuthController.deleteToken(dataSubjectIm);
        if (hereUser != null) {
            hereUserManagerController.deleteHereUser(hereUser);
        }
    }

    @Test
    @DisplayName("Consent requests are displayed in all statuses on Offers Dashboard pages")
    void verifyDashBoardTest() {
        var vin = dataSubjectIm.getVin();
        AtomicReference<ConsentInfo> firstConsentRequest = new AtomicReference<>();
        AtomicReference<ConsentInfo> secondConsentRequest = new AtomicReference<>();
        step("Prepare two consent request for a single user", () -> {
                    ConsentRequestContainer testContainer1 = ConsentRequestContainers.generateNew(providerApplication.provider);
                    firstConsentRequest.set(ConsentRequestSteps
                            .createValidConsentRequestWithNSOnboardings(providerApplication, vin, testContainer1));
                    cridsToRemove.add(firstConsentRequest.get().getConsentRequestId());

                    ConsentRequestContainer testContainer2 = ConsentRequestContainers
                            .generateNew(providerApplication.provider);
                    secondConsentRequest.set(ConsentRequestSteps
                            .createValidConsentRequestWithNSOnboardings(providerApplication, vin, testContainer2));
                    cridsToRemove.add(secondConsentRequest.get().getConsentRequestId());
                }
        );

        var consentRequestId1 = firstConsentRequest.get().getConsentRequestId();
        var consentRequestId2 = secondConsentRequest.get().getConsentRequestId();

        open(ConsentPageUrl.getEnvUrlRoot());
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginNewDataSubjectWithHEREConsentApprove(dataSubjectIm);
        new VINEnteringPage().isLoaded().fillVINAndContinue(vin);
        String token = getUICmToken();
        new DashBoardPage().isLoaded();


        step("Verify offers on dashboard in PENDING status", () -> {
                    DashBoardPage.header.openDashboardNewTab()
                            .isLoaded()
                            .verifyConsentOfferTab(0, secondConsentRequest.get(), vin, PENDING)
                            .verifyConsentOfferTab(1, firstConsentRequest.get(), vin, PENDING);
                }
        );

        step("Approve consents via API", () -> {
                    ConsentFlowSteps.approveConsentForVIN(consentRequestId1, testContainer, vin, token);
                    ConsentFlowSteps.approveConsentForVIN(consentRequestId2, testContainer, vin, token);
                }
        );

        step("Verify offers on dashboard in APPROVED status", () -> {
                    DashBoardPage.header.openDashboardAcceptedTab()
                            .verifyConsentOfferTab(0, secondConsentRequest.get(), vin, APPROVED)
                            .verifyConsentOfferTab(1, firstConsentRequest.get(), vin, APPROVED);
                }
        );

        step("Revoke consents via API", () -> {
                    ConsentFlowSteps.revokeConsentForVIN(consentRequestId1, vin, token);
                    ConsentFlowSteps.revokeConsentForVIN(consentRequestId2, vin, token);
                }
        );

        step("Verify offers on dashboard in REVOKED status", () -> {
                    DashBoardPage.header.openDashboardRevokedTab()
                            .verifyConsentOfferTab(0, secondConsentRequest.get(), vin, REVOKED)
                            .verifyConsentOfferTab(1, firstConsentRequest.get(), vin, REVOKED);
                }
        );
    }

    @Test
    @DisplayName("Open empty Dashboard page for just registered user")
    void openEmptyDashboardPageTest() {
        open(ConsentPageUrl.getEnvUrlRoot());
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginNewDataSubjectWithHEREConsentApprove(dataSubjectIm);

        new VINEnteringPage().isLoaded();
    }

    //todo mark tests with @Feature(or some other) annotation specific user flow that is checked
    // https://confluence.in.here.com/display/OLP/Consent+Management+User+Flows

    @Feature("Sign Out from CM web")
    @Test
    @DisplayName("Sign out and redirect to the 'Landing page'")
    void clickSignOutAndRedirectToLandingPage() {
        open(ConsentPageUrl.getEnvUrlRoot());
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginNewDataSubjectWithHEREConsentApprove(dataSubjectIm);

        new VINEnteringPage().isLoaded().fillVINAndContinue(dataSubjectIm.getVin());

        new DashBoardPage().isLoaded();

        new Header().openDashboardUserAvatarTab();
        new UserProfilePage().isLoaded().clickOnSignOut();

        new LandingPage().isLoaded();
    }

}
