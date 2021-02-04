package com.here.platform.cm.pages;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.sleep;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.openqa.selenium.By;


public class VINEnteringPage extends BaseCMPage {

    private final SelenideElement vinNumberInput = $("#vinNumber");

    @Step("VIN entering page is loaded")
    public VINEnteringPage isLoaded() {
        this.vinNumberInput.waitUntil(Condition.visible.because("Vin page expected to be here."), 10000);
        return this;
    }

    @Step("Fill vin page with vehicle number {vin}")
    public void fillVINAndContinue(String vin) {
        this.vinNumberInput.setValue(vin);
        sleep(1000); //hotfix for slow validation
        $(byText("Continue")).click();
        this.vinNumberInput.waitUntil(Condition.hidden, 10000);
    }

    @Step("Verify if provided VIN already used by this account")
    public void verifyUsedVinError(String vin){
        this.vinNumberInput.setValue(vin);
        sleep(1000);
        $(byText("Continue")).click();
        $(By.cssSelector(".ng-star-inserted p:only-child"))
                .shouldBe(Condition.visible)
                .shouldHave(Condition.text("Provided VIN already used by this account"));
    }

    @Step("Verify Vin page type is Free")
    public VINEnteringPage verifyIsFree() {
        $(byText("Back")).shouldBe(Condition.visible.because("Vin page for free adding of vin expected."));
        return this;
    }

    @Step("Verify Vin page type is Obligatory")
    public VINEnteringPage verifyIsMandatory() {
        $(byText("Back")).shouldBe(Condition.not(Condition.visible.because("Vin page for mandatory adding of vin expected.")));
        return this;
    }

}
