package com.here.platform.ns.helpers;

import com.here.platform.ns.controllers.access.AccessServiceController;
import com.here.platform.ns.controllers.provider.ProviderServiceController;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import lombok.Getter;
import org.apache.log4j.Logger;


public class EnvDataCollector {

    private final static Logger logger = Logger.getLogger(EnvDataCollector.class);
    @Getter
    private final static String fileURL = "build/tmp/environment.properties";

    @Getter
    private static Map<String, String> collectedData = new HashMap<>();


    public static void create() {
        EnvDataCollector
                .getCollectedData().put("BuildVersionProvider",
                new ProviderServiceController().getVersion().getBody().print());
        EnvDataCollector
                .getCollectedData().put("BuildVersionAccess",
                new AccessServiceController().getVersion().getBody().print());
        EnvDataCollector
                .getCollectedData().put("ProviderHealth",
                new ProviderServiceController().getVersion().getBody().print());
        EnvDataCollector.getCollectedData().put("AccessHealth",
                new AccessServiceController().getHealth().getBody().print());

        File yourFile = new File(fileURL);
        try {
            yourFile.createNewFile();
        } catch (IOException e) {
            logger.error("IO problem when creating allure properties file", e);
        }
        try (FileOutputStream fos = new FileOutputStream(yourFile)) {
            Properties props = new Properties();
            collectedData.forEach(props::setProperty);
            props.store(fos, "Properties Related to current test run.");
        } catch (IOException e) {
            logger.error("IO problem when writing allure properties file", e);
        }
    }

}
