package com.here.platform.ns.helpers;

import io.qameta.allure.Step;

import java.util.logging.Logger;


public class LoggerHelper {

    private final static Logger logger = Logger.getLogger(LoggerHelper.class.getCanonicalName());

    @Step("{0}")
    public static void logStep(String message) {
        logger.info(message);
    }

}
