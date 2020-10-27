package com.here.platform.cm.ui;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.closeWebDriver;
import static com.codeborne.selenide.Selenide.open;
import static com.here.platform.cm.rest.model.ConsentInfo.StateEnum.PENDING;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.here.platform.cm.controllers.HERETokenController;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPConsumers;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.pages.DashBoardPage;
import com.here.platform.cm.pages.VINEnteringPage;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.steps.api.ConsentFlowSteps;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.cm.steps.ui.OfferDetailsPageSteps;
import com.here.platform.cm.steps.ui.SuccessConsentPageSteps;
import com.here.platform.common.DataSubject;
import com.here.platform.common.VIN;
import com.here.platform.common.VinsToFile;
import com.here.platform.common.annotations.CMFeatures.UserAccount;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.dataProviders.reference.steps.ReferenceApprovePage;
import com.here.platform.hereAccount.controllers.HereUserManagerController;
import com.here.platform.hereAccount.controllers.HereUserManagerController.HereUser;
import com.here.platform.hereAccount.ui.HereLoginSteps;
import io.qameta.allure.Issue;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@UserAccount
@Tag("ui")
@DisplayName("User Account")
public class UserAccountUITests extends BaseUITests {

    protected ProviderApplications targetApp = ProviderApplications.REFERENCE_CONS_1;
    private ConsentRequestContainers testContainer = ConsentRequestContainers.generateNew(targetApp.provider.getName());
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
        dataSubjectIm.setVin(VIN.generate(targetApp.provider.vinLength));
        new HereUserManagerController().createHereUser(hereUser);


    }

    @AfterEach
    void afterEach() {
        var privateBearer =  new HERETokenController().loginAndGenerateCMToken(dataSubjectIm.getEmail(), dataSubjectIm.getPass());
        vinsToRemove.forEach(vin -> userAccountController.deleteVINForUser(vin, privateBearer));
        if (hereUser != null) {
            new HereUserManagerController().deleteHereUser(hereUser);
        }
    }

    @Test
    @Issue("NS-1475")
    @DisplayName("Second time opened the approved consent request link for registered user")
    void secondTimeOpenTheApprovedConsentLinkForRegisteredUserTest() {
        consentRequestInfo = ConsentRequestSteps
                .createValidConsentRequestWithNSOnboardings(targetApp, dataSubjectIm.getVin(), testContainer);
        crid = consentRequestInfo.getConsentRequestId();
        vinsToRemove.add(dataSubjectIm.getVin());

        consentRequestInfo.resources(targetApp.container.resources);
        String token = new HERETokenController().loginAndGenerateCMToken(dataSubjectIm.getEmail(), dataSubjectIm.getPass());
        ConsentFlowSteps.approveConsentForVIN(crid, testContainer, dataSubjectIm.getVin(), token);

        open(crid);
        HereLoginSteps.loginDataSubject(dataSubjectIm);
        $(".container-offers.current").waitUntil(Condition.visible, 10000);
        $(".offer-box .offer-title").shouldHave(Condition.text(consentRequestInfo.getTitle()));
        $("lui-status").shouldHave(Condition.textCaseSensitive("ACCEPTED"));
        $(".offer-box").click();
        $$(".container-content [data-cy='resource']")
                .shouldHave(CollectionCondition.textsInAnyOrder(consentRequestInfo.getResources()));
    }

    @Test
    @Issue("NS-1475")
    @DisplayName("Second time opened the approved consent and proceed with new vehicle")
    void openSecondTimeApprovedConsentAndProceedWithNewVehicleTest() {
        consentRequestInfo = ConsentRequestSteps
                .createValidConsentRequestWithNSOnboardings(targetApp, dataSubjectIm.getVin(), testContainer);
        crid = consentRequestInfo.getConsentRequestId();
        vinsToRemove.add(dataSubjectIm.getVin());

        open(Configuration.baseUrl + crid);
        HereLoginSteps.loginDataSubject(dataSubjectIm);
        new VINEnteringPage().isLoaded().fillVINAndContinue(dataSubjectIm.getVin());
        String token = getUICmToken();

        var secondVIN = VIN.generate(targetApp.provider.vinLength);
        userAccountController.attachVinToUserAccount(secondVIN, token);
        ConsentRequestSteps.addVINsToConsentRequest(targetApp, crid, secondVIN);
        vinsToRemove.add(secondVIN);

        closeWebDriver();

        open(crid);
        HereLoginSteps.loginDataSubject(dataSubjectIm);

        new DashBoardPage().isLoaded().openDashboardNewTab()
                .verifyConsentOfferTab(1, targetApp.consumer, consentRequestInfo, dataSubjectIm.getVin(), PENDING)
                .verifyConsentOfferTab(0, targetApp.consumer, consentRequestInfo, secondVIN, PENDING);
    }

    @Test
    @DisplayName("Open UI with vehicle already attached to account")
    void openSecondTimeWithVehicleTest() {
        consentRequestInfo = ConsentRequestSteps
                .createValidConsentRequestWithNSOnboardings(targetApp, dataSubjectIm.getVin(), testContainer);
        crid = consentRequestInfo.getConsentRequestId();
        vinsToRemove.add(dataSubjectIm.getVin());

        open(Configuration.baseUrl + crid);
        HereLoginSteps.loginDataSubject(dataSubjectIm);
        new VINEnteringPage().isLoaded().fillVINAndContinue(dataSubjectIm.getVin());

        OfferDetailsPageSteps.verifyConsentDetailsPageAndCountinue(consentRequestInfo);
        ReferenceApprovePage.approveReferenceScopesAndSubmit(dataSubjectIm.getVin());
        SuccessConsentPageSteps.verifyFinalPage(consentRequestInfo);

        closeWebDriver();

        open(crid);
        HereLoginSteps.loginDataSubject(dataSubjectIm);
        $(".vin-code", 1).shouldHave(Condition.not(Condition.visible).because("No vin page if vin already attached"));
    }

}
