package com.here.platform.cm.steps.ui;

import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.Condition;
import com.here.platform.cm.rest.model.ConsentInfo;
import io.qameta.allure.Step;
import lombok.experimental.UtilityClass;


@UtilityClass
public class OfferDetailsPageSteps {

    @Step("Verify consent details page and continue")
    public void verifyConsentDetailsPageAndCountinue(ConsentInfo consentInfo) {
        $(".container-content [data-cy=title]").shouldHave(Condition.text(consentInfo.getTitle()));
        $(".container-content [data-cy=consumerName]")
                .shouldHave(Condition.text("Offer from " + consentInfo.getConsumerName()));
        $(".container-content [data-cy=purpose]").shouldHave(Condition.text(consentInfo.getPurpose()));

        $(".container-content [data-cy=vin-code]").shouldHave(Condition.text("*********" + consentInfo.getVinLabel()));
        acceptAndContinueConsent();
    }

    @Step("Accept with HERE and continue to data provider site")
    private void acceptAndContinueConsent() {
        $("a[href='javascript:void(0)']").click();
    }

}
