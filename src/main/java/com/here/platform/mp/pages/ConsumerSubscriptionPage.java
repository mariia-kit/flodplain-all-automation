package com.here.platform.mp.pages;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.sleep;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import java.io.File;


public class ConsumerSubscriptionPage extends BaseMPPage {

    public ConsumerSubscriptionPage isLoaded() {
        $(".subscription__header").waitUntil(Condition.visible, 20000);
        return this;
    }

    public ConsumerSubscriptionPage createConsentRequest() {
        $(dataTest("create-consent-request")).click();
        return this;
    }

    public ConsumerSubscriptionPage fillConsentRequestTitle(String title) {
        $(dataTest("consent-form-consent-title")).val(title);
        return this;
    }

    public ConsumerSubscriptionPage fillConsentRequestDescription(String description) {
        $(dataTest("consent-form-consent-description")).val(description);
        return this;
    }

    public ConsumerSubscriptionPage attachFileWithVINs(File fileWithVINs) {
        $("input.gal-file-upload__field").uploadFile(fileWithVINs);
        return this;
    }

    public ConsumerSubscriptionPage saveConsentRequest() {
        Configuration.clickViaJs = true;
        sleep(5000); //I don't know why but it is not working in normal way.
        $(dataTest("submit-consent")).click();
        Configuration.clickViaJs = false;
        return this;
    }

}
