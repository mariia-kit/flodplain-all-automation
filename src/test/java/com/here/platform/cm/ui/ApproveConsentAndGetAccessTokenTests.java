package com.here.platform.cm.ui;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.switchTo;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.here.platform.cm.controllers.AccessTokenController;
import com.here.platform.cm.controllers.HERETokenController;
import com.here.platform.cm.controllers.UserAccountController;
import com.here.platform.cm.dataAdapters.ConsentInfoToConsentRequestData;
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
import com.here.platform.common.DataSubject;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VIN;
import com.here.platform.common.VinsToFile;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.dataProviders.daimler.steps.DaimlerLoginPage;
import com.here.platform.dataProviders.reference.steps.ReferenceApprovePage;
import com.here.platform.hereAccount.controllers.HereUserManagerController;
import com.here.platform.hereAccount.controllers.HereUserManagerController.HereUser;
import com.here.platform.hereAccount.ui.HereLoginSteps;
import io.qameta.allure.Step;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponentsBuilder;


@DisplayName("Approve and get access token E2E")
@Tag("ui")
class ApproveConsentAndGetAccessTokenTests extends BaseUITests {

    private static final String staticPageUrl = ConsentPageUrl.getEnvUrlRoot() + "purpose/info";
    protected ProviderApplications providerApplication = ProviderApplications.REFERENCE_CONS_1;
    private final MPConsumers mpConsumer = providerApplication.consumer;
    private ConsentRequestContainers testContainer = ConsentRequestContainers.generateNew(providerApplication.provider.getName());
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
        var privateBearer =  new HERETokenController().loginAndGenerateCMToken(dataSubjectIm.getEmail(), dataSubjectIm.getPass());
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
    @DisplayName("Verify Purpose page")
    void verifyPurposePageTest() {
        var mpConsumer = providerApplication.consumer;
        //todo after implemented ConsentInfo.privacyPolicy/additionalLinks fields refactor to
        // var consentRequest = ConsentRequestSteps.createConsentRequestWithVINFor(providerApplication, dataSubject.vin)
        //var consentRequest = generateConsentData(mpConsumer);
        var consentRequest = ConsentRequestSteps
                .createValidConsentRequestWithNSOnboardings(providerApplication, dataSubjectIm.getVin(), testContainer);

        String purposePageUrl = UriComponentsBuilder.fromUriString(staticPageUrl)
                .queryParam("consumerId", consentRequest.getConsumerName())
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
            ConsentRequestContainers container) {
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
