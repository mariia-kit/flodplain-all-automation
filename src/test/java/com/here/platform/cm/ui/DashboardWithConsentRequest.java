package com.here.platform.cm.ui;

import static com.codeborne.selenide.Selenide.open;
import static com.here.platform.cm.rest.model.ConsentInfo.StateEnum.APPROVED;
import static com.here.platform.cm.rest.model.ConsentInfo.StateEnum.REVOKED;

import com.here.platform.cm.enums.ConsentObject;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.pages.BaseCMPage.Header;
import com.here.platform.cm.pages.DashBoardPage;
import com.here.platform.cm.pages.LandingPage;
import com.here.platform.cm.pages.PurposePage;
import com.here.platform.cm.pages.VINEnteringPage;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.api.UserAccountSteps;
import com.here.platform.cm.steps.ui.OfferDetailsPageSteps;
import com.here.platform.cm.steps.ui.SuccessConsentPageSteps;
import com.here.platform.common.DataSubject;
import com.here.platform.common.annotations.CMFeatures.Dashboard;
import com.here.platform.common.annotations.CMFeatures.Purpose;
import com.here.platform.dataProviders.reference.steps.ReferenceApprovePage;
import com.here.platform.hereAccount.ui.HereLoginSteps;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import io.qameta.allure.Issue;
import io.qameta.allure.Step;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("dynamic_ui")
@Dashboard
@DisplayName("Dashboard with consents")
public class DashboardWithConsentRequest extends BaseUITests {

    @Step("Open consent request id link, open login form, login HERE account, fill VIN and open Offer Details page")
    private ConsentObject openConsentLoginUserAndFillVIN () {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        DataSubject dataSubjectIm = UserAccountSteps.generateNewHereAccount(provider.getVinLength());
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(provider);

        ConsentObject consentObj = new ConsentObject(mpConsumer, MPProviders.DAIMLER_REFERENCE, targetContainer);
        consentObj.setDataSubject(dataSubjectIm);
        var crid = new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(dataSubjectIm.getVin())
                .getId();

        open(crid);
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginNewDataSubjectWithHEREConsentApprove(dataSubjectIm);

        new VINEnteringPage().isLoaded().fillVINAndContinue(dataSubjectIm.getVin());
        return consentObj;
    }

    @Test
    @DisplayName("Possible to open dashboard page from offer details page")
    void verifyOpenDashBoardTest () {
        openConsentLoginUserAndFillVIN();

        OfferDetailsPageSteps.closeCurrentOffer();
        new DashBoardPage().isLoaded();
    }

    @Test
    @DisplayName("Success flow for consent request Revoke action via Offer details page")
    void verifyRevokeDashboardTest () {
        ConsentObject consentObj = openConsentLoginUserAndFillVIN();

        OfferDetailsPageSteps.verifyConsentDetailsPageAndCountinue(consentObj.getConsent());
        ReferenceApprovePage.approveReferenceScopesAndSubmit(consentObj.getDataSubject().getVin());
        SuccessConsentPageSteps.verifyFinalPage(consentObj.getConsent());
        SuccessConsentPageSteps.openAllOffersLink();

        new DashBoardPage()
                .isLoaded()
                .openDashboardProductName()
                .openConsentRequestOfferBox(consentObj.getConsent());
        OfferDetailsPageSteps.verifyConsentDetailsPage(consentObj.getConsent());
        OfferDetailsPageSteps.revokeConsent();
        OfferDetailsPageSteps.revokeConsentPopupYes();

        DashBoardPage.header.openDashboardRevokedTab()
                .isLoaded()
                .verifyConsentOfferTab(0, consentObj.getConsent(), consentObj.getDataSubject().getVin(), REVOKED)
                .openConsentRequestOfferBox(consentObj.getConsent());
        OfferDetailsPageSteps.verifyConsentDetailsPage(consentObj.getConsent());
    }

    @Test
    @Issue("NS-3095")
    @DisplayName("Header tabs should be displayed on the consent request 'success approve' screen and details")
    void verifyHeaderTabsAreDisplayedTest () {
        ConsentObject consentObj = openConsentLoginUserAndFillVIN();

        OfferDetailsPageSteps.verifyConsentDetailsPageAndCountinue(consentObj.getConsent());
        ReferenceApprovePage.approveReferenceScopesAndSubmit(consentObj.getDataSubject().getVin());
        SuccessConsentPageSteps.verifyFinalPage(consentObj.getConsent());
        SuccessConsentPageSteps.openAllOffersLink();

        new DashBoardPage()
                .isLoaded();

        new Header()
                .openDashboardAcceptedTab()
                .isLoaded();

        new Header()
                .openDashboardRevokedTab()
                .isLoaded();

        new Header()
                .openDashboardNewTab()
                .isLoaded();

        DashBoardPage.header.openDashboardAcceptedTab()
                .isLoaded()
                .verifyConsentOfferTab(0, consentObj.getConsent(), consentObj.getDataSubject().getVin(), APPROVED)
                .openConsentRequestOfferBox(consentObj.getConsent());

           new Header().verifyHeadersDislayed();
           new Header()
                   .openDashboardAcceptedTab()
                   .isLoaded();

    }

    @Test
    @Issue("NS-2998")
    @DisplayName("Approved ConsentRequest should be shown after clicking on 'View all accepted offers' link")
    void verifyApprovedConsentRequestIsShownTest () {
        ConsentObject consentObj = openConsentLoginUserAndFillVIN();

        OfferDetailsPageSteps.verifyConsentDetailsPageAndCountinue(consentObj.getConsent());
        ReferenceApprovePage.approveReferenceScopesAndSubmit(consentObj.getDataSubject().getVin());
        SuccessConsentPageSteps.verifyFinalPage(consentObj.getConsent());
        SuccessConsentPageSteps.openAllOffersLink();

        new DashBoardPage()
                .isLoaded()
                .openDashboardProductName()
                .openConsentRequestOfferBox(consentObj.getConsent());
        OfferDetailsPageSteps.verifyConsentDetailsPage(consentObj.getConsent());

    }

    @Test
    @Purpose
    @DisplayName("Verify Dashboard open more info page")
    void verifyOpenDashBoardMoreInfoTest () {
        ConsentObject consentObj = openConsentLoginUserAndFillVIN();

        OfferDetailsPageSteps.verifyConsentDetailsPage(consentObj.getConsent());
        OfferDetailsPageSteps.openFullInfo();
        new PurposePage().verifyPurposeInfoPage(
                consentObj.getConsumer(),
                consentObj.getConsent(),
                consentObj.getContainer()
        );
    }
}
