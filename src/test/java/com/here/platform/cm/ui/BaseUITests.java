package com.here.platform.cm.ui;

import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static com.here.platform.common.strings.SBB.sbb;
import static io.qameta.allure.Allure.step;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.junit5.TextReportExtension;
import com.codeborne.selenide.logevents.SelenideLogger;
import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.enums.ConsentPageUrl;
import com.here.platform.common.SeleniumContainerHandler;
import com.here.platform.common.annotations.CMFeatures.ZephyrComponent;
import io.qameta.allure.Allure;
import io.qameta.allure.selenide.AllureSelenide;
import io.qameta.allure.selenide.LogType;
import java.net.URL;
import java.util.logging.Level;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteExecuteMethod;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.html5.RemoteWebStorage;
import org.testcontainers.junit.jupiter.Testcontainers;


@ExtendWith(TextReportExtension.class)
@Testcontainers
@Execution(ExecutionMode.CONCURRENT)
@Tag("ui")
@ZephyrComponent("CM-UI")
public class BaseUITests extends BaseCMTest {

    static {
        Configuration.baseUrl = ConsentPageUrl.getConsentRequestsUrl();
        Configuration.driverManagerEnabled = true;
        Configuration.pollingInterval = 400;
        Configuration.browserSize = "1366x1000";
        SelenideLogger.addListener("AllureListener", new AllureSelenide().enableLogs(LogType.BROWSER, Level.ALL));
    }

    @BeforeEach
    @SneakyThrows
    void setUpBrowserContainer() {
        initDriver();
    }


    @AfterEach
    public void tearDownBrowser() {
        WebDriverRunner.closeWebDriver();
        String testId = Allure.getLifecycle().getCurrentTestCase().get();
        SeleniumContainerHandler.close(testId);
    }

    public static String getUICmToken() {
        var webStorage = new RemoteWebStorage(new RemoteExecuteMethod((RemoteWebDriver) getWebDriver()));
        LocalStorage storage = webStorage.getLocalStorage();
        var tokenValue = storage.getItem("cmToken") == null ? storage.getItem("CM_TOKEN") : storage.getItem("cmToken");
        //todo temp hot fix to pass UI tests for both implementations
        return sbb("Bearer").w().append(tokenValue).bld();
    }

    @SneakyThrows
    public void initDriver() {
        DesiredCapabilities capability = DesiredCapabilities.chrome();
        String testId = Allure.getLifecycle().getCurrentTestCase().get();
        String seleniumHost = SeleniumContainerHandler.get(testId);
        step("Start selenium host" + seleniumHost);
        RemoteWebDriver driver = new RemoteWebDriver(new URL("http://" + seleniumHost + ":4444/wd/hub/"),
                capability);
        WebDriverRunner.setWebDriver(driver);
        WebDriverRunner.getWebDriver().manage().window().setSize(new Dimension(1366, 1000));
    }

    public void restartBrowser() {
        WebDriverRunner.closeWindow();
        initDriver();
    }

}
