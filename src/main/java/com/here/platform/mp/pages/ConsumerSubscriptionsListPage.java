package com.here.platform.mp.pages;

import static com.codeborne.selenide.Selectors.byXpath;
import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.Condition;


public class ConsumerSubscriptionsListPage extends BaseMPPage {

    public ConsumerSubscriptionsListPage isLoaded() {
        $(".subscriptions-table").waitUntil(Condition.visible, 10000);
        return this;
    }

    public ConsumerSubscriptionsListPage openSubscriptionWithName(String subscriptionResourceName) {
        $(byXpath("//td[text() = '" + subscriptionResourceName + "']")).click();
        return this;
    }

    public ConsumerSubscriptionsListPage waitSubscriptionWithName(String subscriptionResourceName) {
        $(byXpath("//td[text() = '" + subscriptionResourceName + "']/..//span[text() = 'active']"))
                .waitUntil(Condition.visible.because("Subscription not in active state, while expected!"), 2*60*1000, 10000);
        return this;
    }
}
