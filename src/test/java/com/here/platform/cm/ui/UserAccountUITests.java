package com.here.platform.cm.ui;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.closeWebDriver;
import static com.codeborne.selenide.Selenide.open;
import static com.here.platform.cm.rest.model.ConsentInfo.StateEnum.PENDING;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.here.platform.cm.controllers.HERETokenController;
import com.here.platform.cm.pages.DashBoardPage;
import com.here.platform.cm.pages.LandingPage;
import com.here.platform.cm.pages.VINEnteringPage;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.ui.OfferDetailsPageSteps;
import com.here.platform.cm.steps.ui.SuccessConsentPageSteps;
import com.here.platform.common.DataSubject;
import com.here.platform.common.annotations.CMFeatures.UserAccount;
import com.here.platform.common.strings.VIN;
import com.here.platform.dataProviders.reference.steps.ReferenceApprovePage;
import com.here.platform.hereAccount.controllers.HereUserManagerController;
import com.here.platform.hereAccount.controllers.HereUserManagerController.HereUser;
import com.here.platform.hereAccount.ui.HereLoginSteps;
import com.here.platform.ns.helpers.authentication.AuthController;
import io.qameta.allure.Issue;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@UserAccount
@DisplayName("User Account UI")
public class UserAccountUITests extends BaseUITests {

    private final List<String> vinsToRemove = new ArrayList<>();
    private ConsentInfo consentRequestInfo;
    private String crid;
    HereUser hereUser = null;
    DataSubject dataSubjectIm;

    @BeforeEach
    void beforeEach() {
        hereUser = new HereUser(faker.internet().emailAddress(), faker.internet().password(), "here");
        dataSubjectIm = new DataSubject();
        dataSubjectIm.setEmail(hereUser.getEmail());
        dataSubjectIm.setPass(hereUser.getPassword());
        dataSubjectIm.setVin(VIN.generate(providerApplication.provider.vinLength));
        new HereUserManagerController().createHereUser(hereUser);


    }

    @AfterEach
    void afterEach() {
        var privateBearer = AuthController.getDataSubjectToken(dataSubjectIm);
        vinsToRemove.forEach(vin -> userAccountController.deleteVINForUser(vin, privateBearer));
        AuthController.deleteToken(dataSubjectIm);
        if (hereUser != null) {
            new HereUserManagerController().deleteHereUser(hereUser);
        }
    }

    @Test
    @Issue("NS-1475")
    @DisplayName("Second time opened the approved consent request link for registered user")
    @Disabled("Disable until vin page in not optional for second try")
    void secondTimeOpenTheApprovedConsentLinkForRegisteredUserTest() {
        consentRequestInfo = ConsentRequestSteps
                .createValidConsentRequestWithNSOnboardings(providerApplication, dataSubjectIm.getVin(), testContainer);
        crid = consentRequestInfo.getConsentRequestId();
        vinsToRemove.add(dataSubjectIm.getVin());

        open(crid);
        System.out.println(Configuration.baseUrl + crid);
        new LandingPage().isLoaded().signIn();
        HereLoginSteps.loginNewDataSubjectWithHEREConsentApprove(dataSubjectIm);
        new VINEnteringPage().isLoaded().fillVINAndContinue(dataSubjectIm.getVin());

        OfferDetailsPageSteps.verifyConsentDetailsPageAndCountinue(consentRequestInfo);

        ReferenceApprovePage.approveReferenceScopesAndSubmit(dataSubjectIm.getVin());

        SuccessConsentPageSteps.verifyFinalPage(consentRequestInfo);

        open(crid);
        new LandingPage().isLoaded().signIn();
        OfferDetailsPageSteps.verifyConsentDetailsPage(consentRequestInfo);

    }

    @Test
    @Issue("NS-1475")
    @DisplayName("Second time opened the approved consent and proceed with new vehicle")
    void openSecondTimeApprovedConsentAndProceedWithNewVehicleTest() {
        consentRequestInfo = ConsentRequestSteps
                .createValidConsentRequestWithNSOnboardings(providerApplication, dataSubjectIm.getVin(), testContainer);
        crid = consentRequestInfo.getConsentRequestId();
        vinsToRemove.add(dataSubjectIm.getVin());

        open(Configuration.baseUrl + crid);
        new LandingPage().isLoaded().signIn();
        HereLoginSteps.loginNewDataSubjectWithHEREConsentApprove(dataSubjectIm);
        new VINEnteringPage().isLoaded().fillVINAndContinue(dataSubjectIm.getVin());
        String token = getUICmToken();

        var secondVIN = VIN.generate(providerApplication.provider.vinLength);
        ConsentRequestSteps.addVINsToConsentRequest(providerApplication, crid, secondVIN);
        vinsToRemove.add(secondVIN);

        closeWebDriver();

        open(crid);
        new LandingPage().isLoaded().signIn();
        HereLoginSteps.loginRegisteredDataSubject(dataSubjectIm);
        new VINEnteringPage().isLoaded().fillVINAndContinue(secondVIN);
        consentRequestInfo.setVinLabel(new VIN(secondVIN).label());
        OfferDetailsPageSteps.verifyConsentDetailsPage(consentRequestInfo);
        new DashBoardPage()
                .openDashboardNewTab()
                .verifyConsentOfferTab(1, providerApplication.consumer, consentRequestInfo, dataSubjectIm.getVin(), PENDING)
                .verifyConsentOfferTab(0, providerApplication.consumer, consentRequestInfo, secondVIN, PENDING);
    }

    @Test
    @DisplayName("Open UI with vehicle already attached to account")
    void openSecondTimeWithVehicleTest() {
        consentRequestInfo = ConsentRequestSteps
                .createValidConsentRequestWithNSOnboardings(providerApplication, dataSubjectIm.getVin(), testContainer);
        crid = consentRequestInfo.getConsentRequestId();
        vinsToRemove.add(dataSubjectIm.getVin());

        open(Configuration.baseUrl + crid);
        new LandingPage().isLoaded().signIn();
        HereLoginSteps.loginNewDataSubjectWithHEREConsentApprove(dataSubjectIm);
        new VINEnteringPage().isLoaded().fillVINAndContinue(dataSubjectIm.getVin());

        OfferDetailsPageSteps.verifyConsentDetailsPageAndCountinue(consentRequestInfo);
        ReferenceApprovePage.approveReferenceScopesAndSubmit(dataSubjectIm.getVin());
        SuccessConsentPageSteps.verifyFinalPage(consentRequestInfo);

        closeWebDriver();

        open(crid);
        new LandingPage().isLoaded().signIn();
        HereLoginSteps.loginRegisteredDataSubject(dataSubjectIm);
        $(".vin-code", 1).shouldHave(Condition.not(Condition.visible).because("No vin page if vin already attached"));
    }

}
