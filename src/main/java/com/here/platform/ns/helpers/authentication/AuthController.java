package com.here.platform.ns.helpers.authentication;

import com.here.platform.aaa.ApplicationTokenController;
import com.here.platform.aaa.DaimlerTokenController;
import com.here.platform.aaa.HERECMTokenController;
import com.here.platform.aaa.PortalTokenController;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.utils.NS_Config;
import com.here.platform.ns.utils.PropertiesLoader;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


public class AuthController {

    private final static Logger logger = Logger.getLogger(AuthController.class);
    private static String appToken = StringUtils.EMPTY;

    public synchronized static void setUserToken(User user) {
        String token = loadOrGenerate(user, () -> {
            switch (user.getType()) {
                case NS:
                    if ("prod".equalsIgnoreCase(System.getProperty("env"))) {
                        logger.info("------------- Creating new APP LIKE user token ------------");
                        String host = NS_Config.URL_AUTH.toString() + NS_Config.GET_TOKEN_PATH.toString();
                        String clientIdValue = NS_Config.APP_KEY.toString();
                        String clientSecretValue = NS_Config.APP_SECRET.toString();
                        return ApplicationTokenController.createConsumerAppToken(host, clientIdValue, clientSecretValue);
                    } else {
                        logger.info("------------- Creating new portal user token ------------");
                        return PortalTokenController.produceToken(user.getRealm(), user.getEmail(), user.getPass());
                    }
                case MP:
                    logger.info("------------- Creating new portal user token ------------");
                    return PortalTokenController.produceToken(user.getRealm(), user.getEmail(), user.getPass());
                case CM:
                    logger.info("------------- Creating new CM user token ------------");
                    return HERECMTokenController.loginAndGenerateCMToken(user.getEmail(), user.getPass());
                case APP:
                    logger.info("------------- Creating new APP LIKE user token ------------");
                    String host = NS_Config.URL_AUTH.toString() + NS_Config.GET_TOKEN_PATH.toString();
                    String clientIdValue = NS_Config.APP_KEY.toString();
                    String clientSecretValue = NS_Config.APP_SECRET.toString();
                    return ApplicationTokenController.createConsumerAppToken(host, clientIdValue, clientSecretValue);
                case DAIMLER:
                    logger.info("------------- Creating new Daimler user token ------------");
                    String clientId = PropertiesLoader.getInstance().mainProperties.getProperty("daimler.clientId");
                    String clientSecret = PropertiesLoader.getInstance().mainProperties.getProperty("daimler.clientSecret");
                    String callbackUrl = PropertiesLoader.getInstance().mainProperties.getProperty("daimler.callbackurl");
                    String code = DaimlerTokenController.produceConsentAuthCode(
                            clientId,
                            clientSecret,
                            user.getEmail(), user.getPass(),
                            callbackUrl);
                    return DaimlerTokenController.createDaimlerToken(code, clientId, clientSecret, callbackUrl);
                case AA:
                    logger.info("------------- Creating new AA user token ------------");
                    return ApplicationTokenController.createConsumerAppToken(
                            NS_Config.URL_AUTH.toString() + NS_Config.GET_TOKEN_PATH.toString(),
                            NS_Config.AAA_ID.toString(),
                            NS_Config.AAA_SECRET.toString());
                default:
                    return StringUtils.EMPTY;
            }
        });
        user.setToken(token);
    }

    public static String loadOrGenerate(User user, Supplier<String> supplier) {
        String currentT = PropertiesLoader.getInstance().loadToken(user.getEmail() + "_" + user.getRealm());
        if (StringUtils.isEmpty(currentT)) {
            String token = supplier.get();
            PropertiesLoader.getInstance().saveToken(user.getEmail() + "_" + user.getRealm(), token);
            return token;
        } else {
            return currentT;
        }
    }

}
