package com.here.platform.cm.steps.ui;

import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.Condition;
import com.here.platform.cm.rest.model.ConsentInfo;
import io.qameta.allure.Step;
import lombok.experimental.UtilityClass;


@UtilityClass
public class SuccessConsentPageSteps {

    @Step
    public void verifyFinalPage(ConsentInfo consentRequest) {
        $("lui-notification[impact='negative'] div.notification > span")
                .shouldNot(Condition.appear);
        $(".container-offers.current .offer-box .main-details")
                .shouldHave(Condition.text(consentRequest.getTitle()))
                .shouldHave(Condition.text(consentRequest.getPurpose()))
                .shouldHave(Condition.text(consentRequest.getConsumerName()))
                .shouldHave(Condition.text(consentRequest.getVinLabel()));
        $(".container-offers.current .offer-box .status").shouldHave(Condition.text("APPROVED"));
    }

}
