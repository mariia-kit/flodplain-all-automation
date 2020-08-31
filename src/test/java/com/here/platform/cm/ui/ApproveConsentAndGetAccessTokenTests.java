package com.here.platform.cm.ui;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.switchTo;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.here.platform.cm.controllers.AccessTokenController;
import com.here.platform.cm.enums.ConsentPageUrl;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPConsumers;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.pages.VINEnteringPage;
import com.here.platform.cm.rest.model.AccessTokenResponse;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.api.RemoveEntitiesSteps;
import com.here.platform.cm.steps.ui.OfferDetailsPageSteps;
import com.here.platform.cm.steps.ui.SuccessConsentPageSteps;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VinsToFile;
import com.here.platform.dataProviders.daimler.steps.DaimlerLoginPage;
import com.here.platform.hereAccount.ui.HereLoginSteps;
import io.qameta.allure.Step;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@DisplayName("Approve and get access token E2E")
@Tag("ui")
class ApproveConsentAndGetAccessTokenTests extends BaseUITests {

    private static final String
            staticPageUrl = ConsentPageUrl.getEnvUrlRoot() + "purpose/info",
            purposePageUrl = ConsentPageUrl.getEnvUrlRoot() + "consentRequests/purpose";
    protected ProviderApplications providerApplication = ProviderApplications.DAIMLER_CONS_1;
    private final MPConsumers mpConsumer = providerApplication.consumer;

    private String crid;

    //todo refactor as extension https://www.baeldung.com/junit-5-extensions
    @BeforeEach
    void beforeEach() {
        var privateBearer = dataSubject.getBearerToken();
        userAccountController.deleteConsumerForUser(mpConsumer.getRealm(), privateBearer);
        userAccountController.deleteVINForUser(dataSubject.getVin(), privateBearer);
    }

    @AfterEach
    void afterEach() {
        userAccountController.deleteVINForUser(dataSubject.getVin(), dataSubject.getBearerToken());
        RemoveEntitiesSteps.forceRemoveConsentRequestWithConsents(crid, new VinsToFile(dataSubject.getVin()).json());
    }


    @Test
    @DisplayName("E2E create approve consent and get access token")
    void e2eTest() {
        var consentRequest = ConsentRequestSteps
                .createConsentRequestWithVINFor(providerApplication, dataSubject.getVin());

        crid = consentRequest.getConsentRequestId();

        var vin = dataSubject.getVin();
        open(crid);
        System.out.println(Configuration.baseUrl + crid);

        HereLoginSteps.loginDataSubject(dataSubject);
        new VINEnteringPage().isLoaded().fillVINAndContinue(vin);
        OfferDetailsPageSteps.verifyConsentDetailsPageAndCountinue(consentRequest);
        this.dataSubject.setBearerToken(getUICmToken());
        DaimlerLoginPage.loginDataSubjectOnDaimlerSite(dataSubject);
        DaimlerLoginPage.approveDaimlerScopesAndSubmit();

        SuccessConsentPageSteps.verifyFinalPage(consentRequest);

        Selenide.clearBrowserCookies();
        Selenide.clearBrowserLocalStorage();

        var accessTokenController = new AccessTokenController();
        accessTokenController.withCMToken();
        var accessTokenResponse = accessTokenController.getAccessToken(crid, vin, mpConsumer.getRealm());

        new ResponseAssertion(accessTokenResponse)
                .statusCodeIsEqualTo(StatusCode.OK)
                .bindAs(AccessTokenResponse.class);
    }


    @Test
    @DisplayName("Verify Purpose page")
    void verifyPurposePageTest() {
        var mpConsumer = MPConsumers.OLP_CONS_1;
        var container = testContainer;
        //todo after implemented ConsentInfo.privacyPolicy/additionalLinks fields refactor to
        // var consentRequest = ConsentRequestSteps.createConsentRequestWithVINFor(providerApplication, dataSubject.vin)
        var consentRequest = generateConsentData(mpConsumer);
        crid = requestConsentAddVin(mpConsumer, consentRequest, dataSubject.getVin());

        open(crid);
        HereLoginSteps.loginDataSubject(dataSubject);
        fuSleep();
        updateSessionStorageData(crid, dataSubject.getVin());
        dataSubject.setBearerToken(getUICmToken());
        openStaticPurposePage();
        verifyStaticPurposeInfoPage();
        openPurposePageLink();
        verifyPurposeInfoPage(mpConsumer, consentRequest, container);
    }

    @Step
    private void verifyConsentDetailsPage(MPConsumers mpConsumer, ConsentInfo consentRequest) {
        $(".container-content [data-cy='title']").shouldHave(Condition.text(consentRequest.getTitle()));
        $(".container-content [data-cy='consumerName']")
                .shouldHave(Condition.text("Offer from " + mpConsumer.getConsumerName()));
        $(".container-content [data-cy='purpose']").shouldHave(Condition.text(consentRequest.getPurpose()));

        $$(".container-content [data-cy='resource']")
                .shouldHave(CollectionCondition.textsInAnyOrder(testContainer.resources));

        $(".container-content [data-cy='vin-code']")
                .shouldHave(Condition.text("*********" + consentRequest.getVinLabel()));
    }


    @Step
    private void openStaticPurposePage() {
        open(staticPageUrl);
    }

    @Step
    private void verifyStaticPurposeInfoPage() {
        switchTo().window("HERE Consent"); //todo code duplication
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
        $(".container-content p:nth-child(5) a")
                .shouldHave(Condition.attribute("href", purposePageUrl));
    }

    @Step
    private void verifyPurposeInfoPage(MPConsumers mpConsumer, ConsentRequestData consentRequest, ConsentRequestContainers container) {
        switchTo().window("HERE Consent"); //todo code duplication
        $("lui-notification[impact='negative']")
                .shouldNot(Condition.appear);
        $(".purpose-content h2").shouldHave(Condition.text(consentRequest.getTitle()));
        $(".purpose-content .from p").shouldHave(Condition.text(mpConsumer.getConsumerName()));
        $(".purpose-content h4 + p").shouldHave(Condition.text(consentRequest.getPurpose()));
        $(".source.description")
                .shouldHave(Condition.text("Requested data\n" + String.join("\n", container.resources)));
        $(".source p").shouldHave(Condition.text(container.containerDescription));

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
