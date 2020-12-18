package com.here.platform.cm.ui;

import static com.codeborne.selenide.Selenide.open;
import static com.here.platform.cm.rest.model.ConsentInfo.StateEnum.REVOKED;

import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.pages.DashBoardPage;
import com.here.platform.cm.pages.LandingPage;
import com.here.platform.cm.pages.PurposePage;
import com.here.platform.cm.pages.VINEnteringPage;
import com.here.platform.cm.rest.model.ConsentInfo;
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
import io.qameta.allure.Step;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("dynamic_ui")
@Dashboard
public class DashboardWithConsentRequest extends BaseUITests {

    private String crid;
    private ConsentInfo consentRequest;
    private ConsentRequestContainer targetContainer;
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

        targetContainer = ConsentRequestContainers
                .generateNew(providerApplication.provider);
        consentRequest = ConsentRequestSteps
                .createValidConsentRequestWithNSOnboardings(providerApplication, dataSubjectIm.getVin(), targetContainer);
        crid = consentRequest.getConsentRequestId();
        cridsToRemove.add(crid);
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

    @Step("Open consent request id link, open login form, login HERE account, fill VIN and open Offer Details page")
    private void openConsentLoginUserAndFillVIN () {
        open(crid);
        new LandingPage().isLoaded().clickSignIn();
        HereLoginSteps.loginNewDataSubjectWithHEREConsentApprove(dataSubjectIm);

        new VINEnteringPage().isLoaded().fillVINAndContinue(dataSubjectIm.getVin());
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
        openConsentLoginUserAndFillVIN();

        OfferDetailsPageSteps.verifyConsentDetailsPageAndCountinue(consentRequest);
        ReferenceApprovePage.approveReferenceScopesAndSubmit(dataSubjectIm.getVin());
        SuccessConsentPageSteps.verifyFinalPage(consentRequest);
        SuccessConsentPageSteps.openAllOffersLink();

        new DashBoardPage()
                .isLoaded()
                .openDashboardProductName()
                .openConsentRequestOfferBox(consentRequest);
        OfferDetailsPageSteps.verifyConsentDetailsPage(consentRequest);
        OfferDetailsPageSteps.revokeConsent();
        OfferDetailsPageSteps.revokeConsentPopupYes();

        DashBoardPage.header.openDashboardRevokedTab()
                .isLoaded()
                .verifyConsentOfferTab(0, consentRequest, dataSubjectIm.getVin(), REVOKED)
                .openConsentRequestOfferBox(consentRequest);
        OfferDetailsPageSteps.verifyConsentDetailsPage(consentRequest);
    }

    @Test
    @Purpose
    @DisplayName("Verify Dashboard open more info page")
    void verifyOpenDashBoardMoreInfoTest () {
        openConsentLoginUserAndFillVIN();

        OfferDetailsPageSteps.verifyConsentDetailsPage(consentRequest);
        OfferDetailsPageSteps.openFullInfo();
        new PurposePage().verifyPurposeInfoPage(
                providerApplication.consumer,
                consentRequest,
                targetContainer
        );
    }
}
