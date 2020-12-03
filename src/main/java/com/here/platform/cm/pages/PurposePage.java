package com.here.platform.cm.pages;

import static com.codeborne.selenide.Selectors.byLinkText;
import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.Condition;
import com.here.platform.cm.enums.ConsentPageUrl;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.ns.dto.User;
import io.qameta.allure.Step;


public class PurposePage extends BaseCMPage {

    @Step
    public void openConsentRequestLink() {
        $(byLinkText("Consent Request")).click();
    }

    @Step
    public void verifyStaticPurposeInfoPage() {
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
    public void verifyPurposeInfoPage(User mpConsumer, ConsentInfo consentRequest,
            ConsentRequestContainer container) {
        $("lui-notification[impact='negative']")
                .shouldNot(Condition.appear);
        $(".purpose-title").shouldHave(Condition.text(consentRequest.getTitle()));
        $(".purpose-consumer-name").shouldHave(Condition.text(mpConsumer.getName()));
        $(".purpose-purpose-name").shouldHave(Condition.text(consentRequest.getPurpose()));
        $(".source.description")
                .shouldHave(Condition.text("Requested data\n" + String.join("\n", container.getResources())));
        $(".purpose-container-description").shouldHave(Condition.text(container.getContainerDescription()));

        $(".purpose-legal-basis-description a").shouldHave(Condition.attribute("href", ConsentPageUrl.getAcceptedOffersUrl()));
        $(".purpose-privacy-policy")
                .shouldHave(Condition.text("To learn more about privacy practices of " + mpConsumer.getName()
                        + ", visit their privacy policy."));
        $(".purpose-privacy-policy a")
                .shouldHave(Condition.attribute("href", "https://" + consentRequest.getPrivacyPolicy() + "/"));
    }

}
