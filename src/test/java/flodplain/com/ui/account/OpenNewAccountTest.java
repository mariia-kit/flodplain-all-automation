package flodplain.com.ui.account;

import static com.codeborne.selenide.Selenide.open;

import flodplain.com.ui.BaseUiTest;
import flodplain.com.web.enums.AccountPageUrl;
import flodplain.com.web.pages.AccountPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


@DisplayName("[UI] Open new account")
@Tag("ui")
public class OpenNewAccountTest extends BaseUiTest {

    AccountPage accountPage = new AccountPage();

    @Test
    @DisplayName("Verify that new account can be created")
    public void verifyOpenNewAccount() {
        open(AccountPageUrl.getLoginUrl());
        //todo sign in with credentials
        new AccountPage()
                .isLoaded()
                .clickAccounts();

    }

}
