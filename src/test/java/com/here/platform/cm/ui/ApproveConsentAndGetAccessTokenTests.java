package com.here.platform.cm.ui;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.refresh;
import static com.codeborne.selenide.Selenide.switchTo;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.common.ResponseAssertion;
import com.here.platform.cm.controllers.AccessTokenController;
import com.here.platform.cm.controllers.UserAccountController;
import com.here.platform.cm.enums.ConsentPageUrl;
import com.here.platform.cm.enums.MPConsumers;
import com.here.platform.cm.rest.model.AccessTokenResponse;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.steps.RemoveEntitiesSteps;
import com.here.platform.common.ResponseExpectMessages.StatusCode;
import com.here.platform.common.VIN;
import com.here.platform.common.VinsToFile;
import com.here.platform.dataProviders.DataSubjects;
import io.qameta.allure.Step;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
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
    private final MPConsumers mpConsumer = MPConsumers.OLP_CONS_1;
    private String crid;

    @BeforeEach
    void beforeEach() {
        var privateBearer = dataSubject.generateBearerToken();
        var userAccountController = new UserAccountController();
        userAccountController.deleteConsumerForUser(mpConsumer.getRealm(), privateBearer);
        userAccountController.deleteVINForUser(dataSubject.vin, privateBearer);
    }

    @AfterEach
    void afterEach() {
        new UserAccountController().deleteVINForUser(dataSubject.vin, dataSubject.getBearerToken());
        RemoveEntitiesSteps.forceRemoveConsentRequestWithConsents(crid, new VinsToFile(dataSubject.vin).json());
    }


    @Test
    @DisplayName("E2E create approve consent and get access token")
    void e2eTest() {
        var consentRequest = generateConsentData(mpConsumer);

        var vehicles = List.of(dataSubject);
        crid = requestConsentAddVin(mpConsumer, consentRequest,
                vehicles.stream().map(vehicle -> vehicle.vin).toArray(String[]::new));

        for (DataSubjects vehicle : vehicles) {
            var vin = vehicle.vin;
            open(crid);
            System.out.println(Configuration.baseUrl + crid);

            loginDataSubjectHERE(vehicle);
            submitVin(vin);
            verifyConsentDetailsPage(mpConsumer, consentRequest, vin);
            acceptAndContinueConsent();
            loginDataSubjectOnDaimlerSite(vin);

            approveDaimlerScopesAndSubmit();

            verifyFinalPage(mpConsumer, consentRequest, vin);

            dataSubject.setBearerToken(getUICmToken());

            Selenide.clearBrowserCookies();
            Selenide.clearBrowserLocalStorage();

            var accessTokenController = new AccessTokenController();
            accessTokenController.withCMToken();
            var accessTokenResponse = accessTokenController
                    .getAccessToken(crid, vin, consentRequest.getConsumerId());

            new ResponseAssertion(accessTokenResponse)
                    .statusCodeIsEqualTo(StatusCode.OK)
                    .bindAs(AccessTokenResponse.class);
        }
    }

    @Test
    @DisplayName("Verify Purpose page")
    void verifyPurposePageTest() {
        var mpConsumer = MPConsumers.OLP_CONS_1;
        var container = testContainer;
        var consentRequest = generateConsentData(mpConsumer);
        crid = requestConsentAddVin(mpConsumer, consentRequest, dataSubject.vin);

        open(crid);
        loginDataSubjectHERE(dataSubject);
        fuSleep();
        updateSessionStorageData(crid, dataSubject.vin);
        dataSubject.setBearerToken(getUICmToken());
        openStaticPurposePage();
        verifyStaticPurposeInfoPage();
        openPurposePageLink();
        verifyPurposeInfoPage(mpConsumer, consentRequest, container);
    }

    @Step
    private void verifyConsentDetailsPage(MPConsumers mpConsumer, ConsentRequestData consentRequest, String vinNumber) {
        $(".container-content [data-cy='title']").shouldHave(Condition.text(consentRequest.getTitle()));
        $(".container-content [data-cy='consumerName']")
                .shouldHave(Condition.text("Offer from " + mpConsumer.getConsumerName()));
        $(".container-content [data-cy='purpose']").shouldHave(Condition.text(consentRequest.getPurpose()));

        $$(".container-content [data-cy='resource']")
                .shouldHave(CollectionCondition.textsInAnyOrder(testContainer.resources));

        $(".container-content [data-cy='vin-code']")
                .shouldHave(Condition.text("*********" + new VIN(vinNumber).label()));
    }

    @Step
    private void loginDataSubjectOnDaimlerSite(String vin) {
        Selenide.clearBrowserCookies();
        Selenide.clearBrowserLocalStorage();
        refresh();
        $("[name=username]").setValue(DataSubjects.getByVin(vin).username);
        $("#password").setValue(DataSubjects.getByVin(vin).password);
        $("#ciam-weblogin-auth-login-button").click();
    }

    @Step
    @SneakyThrows
    private void approveDaimlerScopesAndSubmit() {
        for (SelenideElement scope : $$("[name*='scope:mb']")) {
            scope.click();
        }
        $("#consent-btn").click();
    }

    @Step
    private void verifyFinalPage(MPConsumers mpConsumer, ConsentRequestData consentRequest, String vinNumber) {
        $("lui-notification[impact='negative'] div.notification > span")
                .shouldNot(Condition.appear);
        $(".container-offers.current .offer-box .main-details")
                .shouldHave(Condition.text(consentRequest.getTitle()))
                .shouldHave(Condition.text(consentRequest.getPurpose()))
                .shouldHave(Condition.text(mpConsumer.getConsumerName()))
                .shouldHave(Condition.text(new VIN(vinNumber).label()));
        $(".container-offers.current .offer-box .status").shouldHave(Condition.text("APPROVED"));
    }


    @Step
    private void openStaticPurposePage() {
        open(staticPageUrl);
    }

    @Step
    private void verifyStaticPurposeInfoPage() {
        switchTo().window("HERE Consent");
        $("lui-notification[impact='negative'] div.notification > span")
                .shouldNot(Condition.appear);
        $(".container-content h4").shouldHave(Condition.text("Purpose of the request"));

        String rootUrl = ConsentPageUrl.getEnvUrlRoot().substring(0, ConsentPageUrl.getEnvUrlRoot().length() -1)
                .replace("https://", StringUtils.EMPTY)
                .replace("http://", StringUtils.EMPTY);

        $(".container-content p:nth-child(3)")
                .shouldHave(Condition.text("You can continue to manage and revoke your consents at " + rootUrl));
        $(".container-content p:nth-child(3) a")
                .shouldHave(Condition.attribute("href", ConsentPageUrl.getEnvUrlRoot()));

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
        switchTo().window("HERE Consent");
        $("lui-notification[impact='negative']")
                .shouldNot(Condition.appear);
        $(".purpose-content h2").shouldHave(Condition.text(consentRequest.getTitle()));
        $(".purpose-content .from p").shouldHave(Condition.text(mpConsumer.getConsumerName()));
        $(".purpose-content h4 + p").shouldHave(Condition.text(consentRequest.getPurpose()));
        $(".source.description").shouldHave(Condition.text("Requested data\n" + String.join("\n", container.resources)));
        $(".source p").shouldHave(Condition.text(container.containerDescription));

        //TODO: uncomment when page fixed
        /*
        $(".source p a")
                .shouldHave(Condition.attribute("href", ConsentPageUrl.getEnvUrlRoot()));
        String pPolicyUrl = "https://legal.here.com/privacy/policy";
        $(".purpose-content p:nth-child(6)")
                .shouldHave(Condition.text("To learn more about privacy practices of " + mpConsumer.getConsumerName() + ", visit their privacy policy."));
        $(".purpose-content p:nth-child(6) a")
                .shouldHave(Condition.attribute("href", pPolicyUrl));

        */
    }

    @Step
    private void openPurposePageLink() {
        $(".container-content p:nth-child(5) a")
                .click();
    }

}
