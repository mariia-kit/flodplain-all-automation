package com.here.platform.cm.ui;

import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static com.here.platform.common.strings.SBB.sbb;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.junit5.TextReportExtension;
import com.codeborne.selenide.logevents.SelenideLogger;
import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.controllers.UserAccountController;
import com.here.platform.cm.enums.ConsentPageUrl;
import com.here.platform.cm.enums.ConsentRequestContainer;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.ProviderApplications;
import io.qameta.allure.selenide.AllureSelenide;
import io.qameta.allure.selenide.LogType;
import java.util.logging.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.remote.RemoteExecuteMethod;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.html5.RemoteWebStorage;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


@ExtendWith(TextReportExtension.class)
@Testcontainers
@Execution(ExecutionMode.SAME_THREAD)
@Tag("ui")
public class BaseUITests extends BaseCMTest {

    static {
        Configuration.baseUrl = ConsentPageUrl.getConsentRequestsUrl();
        Configuration.driverManagerEnabled = true;
        Configuration.pollingInterval = 400;
        Configuration.browserSize = "1366x1000";
        SelenideLogger.addListener("AllureListener", new AllureSelenide().enableLogs(LogType.BROWSER, Level.ALL));
    }

    @Container
    public BrowserWebDriverContainer chrome =
            new BrowserWebDriverContainer()
                    .withCapabilities(new ChromeOptions().addArguments("--no-sandbox"));
    protected ProviderApplications providerApplication = ProviderApplications.REFERENCE_CONS_1;
    protected ConsentRequestContainer testContainer = ConsentRequestContainers
            .generateNew(providerApplication.provider);
    protected UserAccountController userAccountController = new UserAccountController();

    public static String getUICmToken() {
        var webStorage = new RemoteWebStorage(new RemoteExecuteMethod((RemoteWebDriver) getWebDriver()));
        LocalStorage storage = webStorage.getLocalStorage();
        var tokenValue = storage.getItem("cmToken") == null ? storage.getItem("CM_TOKEN") : storage.getItem("cmToken");
        //todo temp hot fix to pass UI tests for both implementations
        return sbb("Bearer").w().append(tokenValue).bld();
    }

    @BeforeEach
    void setUpBrowserContainer() {
        RemoteWebDriver driver = chrome.getWebDriver();
        WebDriverRunner.setWebDriver(driver);
        WebDriverRunner.getWebDriver().manage().window().setSize(new Dimension(1366, 1000));
    }

    @AfterEach
    public void tearDownBrowser() {
        WebDriverRunner.closeWebDriver();
    }

}
