package com.here.platform.cm.ui;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.here.platform.cm.controllers.AccessTokenController;
import com.here.platform.cm.controllers.HERETokenController;
import com.here.platform.cm.enums.ConsentPageUrl;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPConsumers;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.pages.VINEnteringPage;
import com.here.platform.cm.rest.model.AccessTokenResponse;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.ui.OfferDetailsPageSteps;
import com.here.platform.cm.steps.ui.SuccessConsentPageSteps;
import com.here.platform.common.DataSubject;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.config.Conf;
import com.here.platform.common.strings.VIN;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.dataProviders.daimler.steps.DaimlerLoginPage;
import com.here.platform.dataProviders.reference.steps.ReferenceApprovePage;
import com.here.platform.hereAccount.controllers.HereUserManagerController;
import com.here.platform.hereAccount.controllers.HereUserManagerController.HereUser;
import com.here.platform.hereAccount.ui.HereLoginSteps;
import io.qameta.allure.Step;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponentsBuilder;


@DisplayName("Approve and get access token E2E")
@Tag("ui")
class ApproveConsentAndGetAccessTokenTests extends BaseUITests {

    private static final String staticPageUrl = ConsentPageUrl.getEnvUrlRoot() + "purpose/info";
    private final MPConsumers mpConsumer = providerApplication.consumer;
    private final List<String> cridsToRemove = new ArrayList<>();
    HereUser hereUser = null;
    DataSubject dataSubjectIm;
    private String crid;

    //todo refactor as extension https://www.baeldung.com/junit-5-extensions
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
        var privateBearer = new HERETokenController().loginAndGenerateCMToken(dataSubjectIm.getEmail(), dataSubjectIm.getPass());
        userAccountController.deleteVINForUser(dataSubjectIm.getVin(), privateBearer);
        if (hereUser != null) {
            new HereUserManagerController().deleteHereUser(hereUser);
        }
    }


    @Test
    @DisplayName("E2E create approve consent and get access token")
    @Tag("dynamic_ui")
    void e2eTest() {
        var vin = dataSubjectIm.getVin();
        ConsentInfo consentRequest = ConsentRequestSteps
                .createValidConsentRequestWithNSOnboardings(providerApplication, vin, testContainer);
        crid = consentRequest.getConsentRequestId();

        open(crid);
        System.out.println(Configuration.baseUrl + crid);

        System.out.println(dataSubjectIm);
        HereLoginSteps.loginDataSubject(dataSubjectIm);
        new VINEnteringPage().isLoaded().fillVINAndContinue(vin);
        cridsToRemove.add(vin);

        OfferDetailsPageSteps.verifyConsentDetailsPageAndCountinue(consentRequest);

        ReferenceApprovePage.approveReferenceScopesAndSubmit(vin);

        SuccessConsentPageSteps.verifyFinalPage(consentRequest);

        var accessTokenController = new AccessTokenController();
        accessTokenController.withConsumerToken();
        var accessTokenResponse = accessTokenController.getAccessToken(crid, vin, mpConsumer.getRealm());

        new ResponseAssertion(accessTokenResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(AccessTokenResponse.class);
    }

    @Test
    @DisplayName("E2E create approve consent and get access token Daimler")
    @Tag("dynamic_ui")
    void e2eTestDaimler() {
        providerApplication = ProviderApplications.DAIMLER_CONS_1;
        testContainer = ConsentRequestContainers.generateNew(providerApplication.provider.getName());
        testContainer.setClientId(Conf.cmUsers().getDaimlerApp().getClientId());
        testContainer.setClientSecret(Conf.cmUsers().getDaimlerApp().getClientSecret());
        dataSubjectIm.setVin(VIN.generate(providerApplication.provider.vinLength));
        var vin = dataSubjectIm.getVin();
        ConsentInfo consentRequest = ConsentRequestSteps
                .createValidConsentRequestWithNSOnboardings(providerApplication, vin, testContainer);
        crid = consentRequest.getConsentRequestId();

        open(crid);
        System.out.println(Configuration.baseUrl + crid);

        HereLoginSteps.loginDataSubject(dataSubjectIm);
        new VINEnteringPage().isLoaded().fillVINAndContinue(vin);
        cridsToRemove.add(vin);

        OfferDetailsPageSteps.verifyConsentDetailsPageAndCountinue(consentRequest);

        DaimlerLoginPage.loginDataSubjectOnDaimlerSite(DataSubjects._1AE0B89918406F0957.dataSubject);
        if (!SuccessConsentPageSteps.isLoaded()) {
            DaimlerLoginPage.approveDaimlerLegalAndSubmit();
            DaimlerLoginPage.approveDaimlerScopesAndSubmit();
        }

        SuccessConsentPageSteps.verifyFinalPage(consentRequest);

        var accessTokenController = new AccessTokenController();
        accessTokenController.withConsumerToken();
        var accessTokenResponse = accessTokenController.getAccessToken(crid, vin, mpConsumer.getRealm());

        new ResponseAssertion(accessTokenResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(AccessTokenResponse.class);
    }


    @Test
    @DisplayName("Verify Purpose page")
    @Disabled("Disable until purpose page is fixed. NS-3004")
    void verifyPurposePageTest() {
        var mpConsumer = providerApplication.consumer;
        var consentRequest = ConsentRequestSteps
                .createValidConsentRequestWithNSOnboardings(providerApplication, dataSubjectIm.getVin(), testContainer);

        String purposePageUrl = UriComponentsBuilder.fromUriString(staticPageUrl)
                .queryParam("consumerId", mpConsumer.getRealm())
                .queryParam("containerId", consentRequest.getContainerName())
                .toUriString();

        open(purposePageUrl);
        fuSleep();
        verifyStaticPurposeInfoPage();
        openPurposePageLink();
        HereLoginSteps.loginDataSubject(dataSubjectIm);
        new VINEnteringPage().isLoaded().fillVINAndContinue(dataSubjectIm.getVin());
        verifyPurposeInfoPage(mpConsumer, consentRequest, testContainer);
    }

    @Step
    private void verifyStaticPurposeInfoPage() {
        $("lui-notification[impact='negative'] div.notification > span")
                .shouldNot(Condition.appear);
        $(".container-content h4").shouldHave(Condition.text("Purpose of the request"));

        $(".container-content p:nth-child(3)")
                .shouldHave(Condition
                        .text("You can continue to manage and revoke your consents at Consent Management Dashboard."));
        $(".container-content p:nth-child(3) a")
                .shouldHave(Condition.attribute("href", ConsentPageUrl.getAcceptedOffersUrl()));

        String pPolicyUrl = "https://legal.here.com/privacy/policy";
        $(".container-content p:nth-child(4)")

                .shouldHave(Condition.text("To learn more about privacy practices of HERE, see our privacy policy."));
        $(".container-content p:nth-child(4) a")
                .shouldHave(Condition.attribute("href", pPolicyUrl));

        String faq = ConsentPageUrl.getEnvUrlRoot() + "faq";
        $(".container-content p:nth-child(6)")
                .shouldHave(Condition.text("To learn more about this concept, see our FAQ."));
        $(".container-content p:nth-child(6) a")
                .shouldHave(Condition.attribute("href", faq));

        $(".container-content p:nth-child(5)")
                .shouldHave(Condition.text("Consent Request"));
    }

    @Step
    private void verifyPurposeInfoPage(MPConsumers mpConsumer, ConsentInfo consentRequest,
            ConsentRequestContainer container) {
        $("lui-notification[impact='negative']")
                .shouldNot(Condition.appear);
        $(".purpose-content h2").shouldHave(Condition.text(consentRequest.getTitle()));
        $(".purpose-content .from p").shouldHave(Condition.text(mpConsumer.getConsumerName()));
        $(".purpose-content h4 + p").shouldHave(Condition.text(consentRequest.getPurpose()));
        $(".source.description")
                .shouldHave(Condition.text("Requested data\n" + String.join("\n", container.getResources())));
        $(".source p").shouldHave(Condition.text(container.getContainerDescription()));

        $(".source p a").shouldHave(Condition.attribute("href", ConsentPageUrl.getAcceptedOffersUrl()));
        $(".purpose-content p:nth-child(6)")
                .shouldHave(Condition.text("To learn more about privacy practices of " + mpConsumer.getConsumerName()
                        + ", visit their privacy policy."));
        $(".purpose-content p:nth-child(6) a")
                .shouldHave(Condition.attribute("href", "https://" + consentRequest.getPrivacyPolicy() + "/"));
    }

    @Step
    private void openPurposePageLink() {
        $(".container-content p:nth-child(5) a").click();
    }

}
