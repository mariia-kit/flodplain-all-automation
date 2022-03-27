package flodplain.com.web.pages;


import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.sleep;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;


public class AccountPage {

    private final SelenideElement accountNameInput = $("");

    public AccountPage isLoaded() {
        return this;
    }

    @Step("Click Accounts button on dashboard page")
    public AccountPage clickAccounts() {
        $(".").click();
        return this;
    }

    @Step("Fill account page with account name {name}")
    public AccountPage inputAccountName(String name) {
        this.accountNameInput.setValue(name);
        //TODO selector click
        this.accountNameInput.waitUntil(Condition.hidden, 10000);
        return this;
    }

}
