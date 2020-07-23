package com.here.platform.cm.ui;

import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.sleep;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.junit5.TextReportExtension;
import com.codeborne.selenide.logevents.SelenideLogger;
import com.here.platform.cm.BaseCMTest;
import com.here.platform.cm.enums.ConsentPageUrl;
import com.here.platform.cm.enums.ConsentRequestContainers;
import com.here.platform.cm.enums.MPConsumers;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.cm.rest.model.ConsentRequestIdResponse;
import com.here.platform.common.VinsToFile;
import com.here.platform.dataProviders.DataSubjects;
import io.qameta.allure.Step;
import io.qameta.allure.selenide.AllureSelenide;
import io.qameta.allure.selenide.LogType;
import java.io.File;
import java.util.logging.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.remote.RemoteExecuteMethod;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.html5.RemoteWebStorage;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


@ExtendWith(TextReportExtension.class)
@Testcontainers
@Execution(ExecutionMode.SAME_THREAD)
public class BaseUITests extends BaseCMTest {

    static {
        Configuration.baseUrl = ConsentPageUrl.getEnvUrl();
        Configuration.driverManagerEnabled = true;
        Configuration.pollingInterval = 400;
        SelenideLogger.addListener("AllureListener", new AllureSelenide().enableLogs(LogType.BROWSER, Level.ALL));
    }

    final DataSubjects dataSubject = DataSubjects.getNext();
    @Container
    public BrowserWebDriverContainer chrome =
            new BrowserWebDriverContainer()
                    .withCapabilities(new ChromeOptions().addArguments("--no-sandbox"))
                    .withRecordingMode(VncRecordingMode.RECORD_FAILING, new File("build/video"));

    protected ConsentRequestContainers testContainer = ConsentRequestContainers.CONNECTED_VEHICLE;

    @BeforeEach
    void setUpBrowserContainer() {
        RemoteWebDriver driver = chrome.getWebDriver();
        WebDriverRunner.setWebDriver(driver);
    }

    @AfterEach
    public void tearDownBrowser() {
        WebDriverRunner.closeWebDriver();
    }

    ConsentRequestData generateConsentData(MPConsumers mpConsumer) {
        return new ConsentRequestData()
                .providerId(testContainer.provider.getName())
                .consumerId(mpConsumer.getRealm())
                .containerName(testContainer.id)
                .privacyPolicy(faker.internet().url())
                .purpose(faker.commerce().productName() + "_purpose")
                .title(faker.gameOfThrones().quote() + "_title");
    }

    @Step
    String requestConsentAddVin(MPConsumers mpConsumer, ConsentRequestData consentRequest, String... vinNumbers) {
        consentRequestController.withCMToken();
        var consentRequestId = consentRequestController.createConsentRequest(consentRequest)
                .as(ConsentRequestIdResponse.class).getConsentRequestId();

        consentRequestController.withConsumerToken(mpConsumer);
        consentRequestController.addVinsToConsentRequest(consentRequestId, new VinsToFile(vinNumbers).json());
        return consentRequestId;
    }

    @Step
    void loginDataSubjectHERE(DataSubjects dataSubject) {
        $("#sign-in-email").setValue(dataSubject.username);
        $("#sign-in-password-encrypted").setValue(dataSubject.password);
        $("[for='sign-in-remember-me']").click();
        $("#signInBtn").click();
    }

    @Step
    void acceptAndContinueConsent() {
        $("a[href='javascript:void(0)']").click();
    }

    @Step
    void submitVin(String vin) {
        $("#vinNumber").setValue(vin);
        $(byText("Continue")).click();
    }

    @Step
    void updateSessionStorageData(String consentRequestId, String vinNumber) {
        var webStorage = new RemoteWebStorage(new RemoteExecuteMethod((RemoteWebDriver) getWebDriver()));
        LocalStorage storage = webStorage.getLocalStorage();
        storage.setItem("VIN_NUMBER", vinNumber);
        storage.setItem("CONSENT_REQUEST_ID", consentRequestId);
    }

    String getUICmToken() {
        sleep(1000);
        var webStorage = new RemoteWebStorage(new RemoteExecuteMethod((RemoteWebDriver) getWebDriver()));
        LocalStorage storage = webStorage.getLocalStorage();
        return "Bearer " + storage.getItem("CM_TOKEN");
    }

}