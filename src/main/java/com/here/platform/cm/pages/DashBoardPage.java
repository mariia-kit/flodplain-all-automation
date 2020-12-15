package com.here.platform.cm.pages;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.refresh;
import static com.here.platform.cm.rest.model.ConsentInfo.StateEnum.PENDING;
import static com.here.platform.common.strings.SBB.sbb;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.common.strings.VIN;
import io.qameta.allure.Step;


public class DashBoardPage extends BaseCMPage {

    @Step("Dash Board Page is loaded")
    public DashBoardPage isLoaded() {
        $(".offers-list").waitUntil(Condition.visible.because("Dashboard page should be present"), 10000);
        refresh(); //todo temp hotfix for visibility of dashboard tabs
        return this;
    }

    @Step
    public DashBoardPage openDashboardProductName() {
        $(".lui-productlogo__seed").click();
        return this;
    }


    @Step
    public DashBoardPage openConsentRequestOfferBox(ConsentInfo consentRequest) {
        SelenideElement offerBox = $$("app-offer .offer-title").findBy(text(consentRequest.getTitle()));
        offerBox.shouldBe(Condition.visible
                .because(sbb("Consent request").w()
                        .append(consentRequest.getConsentRequestId()).w()
                        .append("should be present on dashboard!")
                        .bld()
                )
        );
        offerBox.click();
        return this;
    }

    @Step
    public DashBoardPage verifyConsentOfferTab(
            int index,
            ConsentInfo consentRequest,
            String vinNumber,
            ConsentInfo.StateEnum status
    ) {
        SelenideElement offerBox = $("app-offer", index).shouldBe(Condition.visible);
        offerBox.$(".offer-title").shouldHave(text(consentRequest.getTitle()));
        offerBox.$(".provider-name").shouldHave(text(consentRequest.getConsumerName()));
        offerBox.$(".offer-description").shouldHave(text(consentRequest.getPurpose()));
        offerBox.$(".vin-code").shouldHave(text(new VIN(vinNumber).label()));
        if (!PENDING.equals(status)) {
            offerBox.$("lui-status").shouldHave(text(status.name()));
        } else {
            offerBox.$("lui-status").shouldNotBe(Condition.visible);
        }
        return this;
    }

}
