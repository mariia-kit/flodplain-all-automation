package com.here.platform.cm.steps.ui;

import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.here.platform.cm.rest.model.ConsentInfo;
import io.qameta.allure.Step;
import lombok.experimental.UtilityClass;


@UtilityClass
public class SuccessConsentPageSteps {

    @Step("Verify Success Consent page content")
    public void verifyFinalPage(ConsentInfo consentRequest) {
        $("lui-notification[impact='negative'] div.notification > span")
                .shouldNot(Condition.appear);
        $(".container-offers.current .offer-box .main-details")
                .waitUntil(Condition.visible, 10000) //hotfix cos of FE developer rotation
                .shouldHave(Condition.text(consentRequest.getTitle()))
                .shouldHave(Condition.text(consentRequest.getPurpose()))
                .shouldHave(Condition.text(consentRequest.getConsumerName()))
                .shouldHave(Condition.text(consentRequest.getVinLabel()));
        $(".container-offers.current .offer-box .status").shouldHave(Condition.text("APPROVED"));
    }

    @Step("Open all offers from success page")
    public void openAllOffersLink() {
        $(".container-offers-link").click();
    }

    public boolean isLoaded() {
        Selenide.sleep(5000);
        return $(".container").isDisplayed();
    }

}
