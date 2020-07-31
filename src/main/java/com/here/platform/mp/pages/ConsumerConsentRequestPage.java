package com.here.platform.mp.pages;

import static com.codeborne.selenide.Selenide.$;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;


public class ConsumerConsentRequestPage extends BaseMPPage {

    private final SelenideElement copyToClipboardIcon = $(".consent-request-details__share div [icon*=copy-clipboard]");

    @Step
    public String copyConsentRequestURLViaClipboard() {
        copyToClipboardIcon.click();
        String clipboard = "";

        try {
            clipboard = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            e.printStackTrace();
        }
        return clipboard;
    }

    public ConsumerConsentRequestPage isLoaded() {
        copyToClipboardIcon.waitUntil(Condition.visible, 20000);
        return this;
    }

}
