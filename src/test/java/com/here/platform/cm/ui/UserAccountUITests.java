package com.here.platform.cm.ui;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.here.platform.cm.rest.model.ConsentInfo.StateEnum.APPROVED;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.here.platform.cm.enums.ConsentObject;
import com.here.platform.cm.enums.ConsentPageUrl;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPProviders;
import com.here.platform.cm.pages.ActualOffersPage;
import com.here.platform.cm.pages.BaseCMPage.Header;
import com.here.platform.cm.pages.DashBoardPage;
import com.here.platform.cm.pages.LandingPage;
import com.here.platform.cm.pages.OopsPage;
import com.here.platform.cm.pages.UserProfilePage;
import com.here.platform.cm.pages.VINEnteringPage;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.api.UserAccountSteps;
import com.here.platform.cm.steps.ui.OfferDetailsPageSteps;
import com.here.platform.cm.steps.ui.SuccessConsentPageSteps;
import com.here.platform.common.DataSubject;
import com.here.platform.common.annotations.CMFeatures.UserAccount;
import com.here.platform.common.strings.VIN;
import com.here.platform.dataProviders.reference.steps.ReferenceApprovePage;
import com.here.platform.hereAccount.ui.HereLoginSteps;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.Users;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@UserAccount
@DisplayName("[UI] User account UI")
public class UserAccountUITests extends BaseUITests {

    @Test
    @Issue("NS-1475")
    @DisplayName("Second time opened the approved consent request link for registered user")
    @Feature("Actual offers page")
    void secondTimeOpenTheApprovedConsentLinkForRegisteredUserTest() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(provider);
        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, targetContainer);
        DataSubject dataSubjectIm = UserAccountSteps.generateNewHereAccount(provider.getVinLength());

        var crid = new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(dataSubjectIm.getVin())
                .getId();

        open(crid);
        System.out.println(Configuration.baseUrl + crid);
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginNewDataSubjectWithHEREConsentApprove(dataSubjectIm);
        new VINEnteringPage().isLoaded().fillVINAndContinue(dataSubjectIm.getVin());

        OfferDetailsPageSteps.verifyConsentDetailsPageAndCountinue(consentObj.getConsent());

        ReferenceApprovePage.approveReferenceScopesAndSubmit(dataSubjectIm.getVin());

        SuccessConsentPageSteps.verifyFinalPage(consentObj.getConsent());

        restartBrowser();
        open(crid);
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginRegisteredDataSubject(dataSubjectIm);
        new DashBoardPage()
                .verifyConsentOfferTab(0, consentObj.getConsent(), dataSubjectIm.getVin(), APPROVED)
                .openConsentRequestOfferBox(consentObj.getConsent());
        OfferDetailsPageSteps.verifyConsentDetailsPage(consentObj.getConsent());

    }

    @Test
    @Issue("NS-1475")
    @Issue("NS-2657")
    @DisplayName("Second time opened the approved consent and proceed with new vehicle")
    void openSecondTimeApprovedConsentAndProceedWithNewVehicleTest() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(provider);
        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, targetContainer);
        DataSubject dataSubjectIm = UserAccountSteps.generateNewHereAccount(provider.getVinLength());

        var step = new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(dataSubjectIm.getVin());
        var crid= step.getId();

        open(crid);
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginNewDataSubjectWithHEREConsentApprove(dataSubjectIm);
        new VINEnteringPage().isLoaded().fillVINAndContinue(dataSubjectIm.getVin());

        var secondVIN = VIN.generate(provider.getVinLength());
        step.addVINsToConsentRequest(secondVIN);
        restartBrowser();

        open(crid);
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginRegisteredDataSubject(dataSubjectIm);

        new ActualOffersPage().isLoaded().clickAddNewVin();
        new VINEnteringPage().fillVINAndContinue(secondVIN);
        new Header().openDashboardNewTab().openConsentRequestOfferBox(consentObj.getConsent(secondVIN));
        OfferDetailsPageSteps.verifyConsentDetailsPage(consentObj.getConsent(secondVIN));
    }

    @Test
    @DisplayName("Open Consent Manager as registered user with vehicle already attached to user")
    void openSecondTimeWithVehicleTest() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(provider);
        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, targetContainer);
        DataSubject dataSubjectIm = UserAccountSteps.generateNewHereAccount(provider.getVinLength());

        var crid = new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(dataSubjectIm.getVin())
                .getId();

        open(crid);
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginNewDataSubjectWithHEREConsentApprove(dataSubjectIm);
        new VINEnteringPage().isLoaded().fillVINAndContinue(dataSubjectIm.getVin());

        OfferDetailsPageSteps.verifyConsentDetailsPageAndCountinue(consentObj.getConsent());
        ReferenceApprovePage.approveReferenceScopesAndSubmit(dataSubjectIm.getVin());
        SuccessConsentPageSteps.verifyFinalPage(consentObj.getConsent());

        restartBrowser();

        open(crid);
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginRegisteredDataSubject(dataSubjectIm);
        $(".vin-code", 1).shouldHave(Condition.not(Condition.visible).because("No vin page if vin already attached"));
    }

    @Test
    @Issue("NS-3506")
    @Feature("VIN Page")
    @DisplayName("Verify Vin Page validation no consent")
    void verifyVinPageWithNoConsent() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        DataSubject dataSubjectIm = UserAccountSteps.generateNewHereAccount(provider.getVinLength());
        open(ConsentPageUrl.getEnvUrlRoot());
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginNewDataSubjectWithHEREConsentApprove(dataSubjectIm);
        new VINEnteringPage().isLoaded().fillVINAndContinue(dataSubjectIm.getVin());

        new UserProfilePage().isLoaded().verifyUserProfileVinDetails(dataSubjectIm.getVin());
    }

    @Test
    @Issue("NS-3067")
    @DisplayName("Verification of not valid consent.")
    @Feature("Actual offers page")
    @Feature("Error page")
    void openNotValidConsentLInk() {
        MPProviders provider = MPProviders.DAIMLER_REFERENCE;
        DataSubject dataSubjectIm = UserAccountSteps.generateNewHereAccount(provider.getVinLength());

        String crid = "1234567890";
        open(crid);

        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginNewDataSubjectWithHEREConsentApprove(dataSubjectIm);
        new OopsPage().isLoaded()
                .verifyError("404", "Couldn't find that record", "Check the URL and try again")
                .verifyRetryButton(false);
    }

    @Test
    @Issue("NS-3501")
    @Feature("Actual offers page")
    @DisplayName("Verify there is no Accepted offer after the connected vin was deleted and add vin again")
    void verifyAcceptedOfferWasRemovedAfterDeletingConnectedVin() {
        MPProviders provider = MPProviders.REFERENCE;
        User mpConsumer = Users.MP_CONSUMER.getUser();
        ConsentRequestContainer targetContainer = ConsentRequestContainers.generateNew(provider);
        ConsentObject consentObj = new ConsentObject(mpConsumer, provider, targetContainer);
        DataSubject dataSubjectIm = UserAccountSteps.generateNewHereAccount(provider.getVinLength());

        var crid = new ConsentRequestSteps(consentObj)
                .onboardAllForConsentRequest()
                .createConsentRequest()
                .addVINsToConsentRequest(dataSubjectIm.getVin())
                .getId();

        open(crid);
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginNewDataSubjectWithHEREConsentApprove(dataSubjectIm);
        new VINEnteringPage().isLoaded().fillVINAndContinue(dataSubjectIm.getVin());

        OfferDetailsPageSteps.verifyConsentDetailsPageAndCountinue(consentObj.getConsent());

        ReferenceApprovePage.approveReferenceScopesAndSubmit(dataSubjectIm.getVin());

        SuccessConsentPageSteps.verifyFinalPage(consentObj.getConsent());

        new Header().openDashboardUserAvatarTab();

        new UserProfilePage()
                .clickProfileInfo();

        new UserProfilePage()
                .clickDeleteVehicle()
                .clickConfirmDelete()
                .verifyNoVehiclesText();

        new Header().openDashboardAcceptedTab();

        new VINEnteringPage()
                .isLoaded()
                .fillVINAndContinue(dataSubjectIm.vin);

        new UserProfilePage()
                .isLoaded()
                .verifyUserProfileVinDetails(dataSubjectIm.vin);
    }

}
