package com.here.platform.cm.ui;

import static com.codeborne.selenide.Selectors.byLinkText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

import com.codeborne.selenide.Condition;
import com.here.platform.cm.dataAdapters.ConsentInfoToConsentRequestData;
import com.here.platform.cm.enums.ConsentPageUrl;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPConsumers;
import com.here.platform.cm.enums.ProviderApplications;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.steps.api.ConsentRequestSteps;
import com.here.platform.cm.steps.api.UserAccountSteps;
import com.here.platform.common.extensions.ConsentRequestCascadeRemoveExtension;
import com.here.platform.dataProviders.daimler.DataSubjects;
import com.here.platform.hereAccount.ui.HereLoginSteps;
import io.qameta.allure.Step;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;


@DisplayName("Purpose page")
public class PurposePageTests extends BaseUITests {

    private final ProviderApplications providerApplicationForPurpose = ProviderApplications.DAIMLER_CONS_1;
    private final ConsentRequestContainer generatedContainerForPurpose =
            ConsentRequestContainers.generateNew(providerApplicationForPurpose.provider);
    @RegisterExtension
    ConsentRequestCascadeRemoveExtension cascadeRemoveExtension = new ConsentRequestCascadeRemoveExtension();
    private DataSubjects registeredDataSubject;
    private ConsentInfo testConsentRequest;

    @BeforeEach
    void beforeEach() {
        registeredDataSubject = DataSubjects.getNextBy18VINLength();

        UserAccountSteps.removeVINFromDataSubject(registeredDataSubject);
        UserAccountSteps.attachDataSubjectVINToUserAccount(registeredDataSubject);

        testConsentRequest = ConsentRequestSteps.createValidConsentRequestWithNSOnboardings(
                providerApplicationForPurpose,
                registeredDataSubject.getVin(),
                generatedContainerForPurpose
        );

        var consentReqToRemove = new ConsentInfoToConsentRequestData(
                testConsentRequest,
                providerApplicationForPurpose.provider.getName(),
                providerApplication.consumer.getRealm()
        ).consentRequestData();

        cascadeRemoveExtension.consentRequestToCleanUp(consentReqToRemove);

    }

    @AfterEach
    void afterEach() {
        UserAccountSteps.removeVINFromDataSubject(registeredDataSubject);
    }


    @Test
    @DisplayName("Verify Purpose page for registeredAccount")
    void verifyPurposePageTest() {
        open(ConsentPageUrl.getStaticPurposePageLinkFor(
                providerApplicationForPurpose.consumer.getRealm(),
                generatedContainerForPurpose.getId())
        );

        verifyStaticPurposeInfoPage();
        $(byLinkText("Consent Request")).click();
        HereLoginSteps.loginRegisteredDataSubject(registeredDataSubject.dataSubject);
        verifyPurposeInfoPage(
                providerApplicationForPurpose.consumer,
                testConsentRequest,
                generatedContainerForPurpose
        );
    }


    @Step
    private void verifyStaticPurposeInfoPage() {
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

}
