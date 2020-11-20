package com.here.platform.ns.helpers.authentication;

import com.here.platform.aaa.ApplicationTokenController;
import com.here.platform.aaa.PortalTokenController;
import com.here.platform.cm.controllers.HERETokenController;
import com.here.platform.common.config.Conf;
import com.here.platform.common.syncpoint.SyncPointIO;
import com.here.platform.ns.dto.User;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


public class AuthController {

    private final static Logger logger = Logger.getLogger(AuthController.class);

    public synchronized static void setUserToken(User user) {
        String token = getUserToken(user);
        user.setToken(token);
    }

    public synchronized static String getUserToken(User user) {
        String token = loadOrGenerate(user.getEmail() + "_" + user.getRealm(), () -> {
            switch (user.getType()) {
                case NS:
                    if ("prod".equalsIgnoreCase(System.getProperty("env"))) {
                        logger.info("------------- Creating new APP LIKE user token ------------");
                        String host = Conf.ns().getAuthUrlBase() + Conf.ns().getAuthUrlGetToken();
                        String clientIdValue = Conf.nsUsers().getConsumerApp().getAppKeyId();
                        String clientSecretValue = Conf.nsUsers().getConsumerApp().getAppKeySecret();
                        return ApplicationTokenController
                                .createConsumerAppToken(host, clientIdValue, clientSecretValue);
                    } else {
                        logger.info("------------- Creating new portal user token ------------");
                        return PortalTokenController.produceToken(user.getRealm(), user.getEmail(), user.getPass());
                    }
                case MP:
                    logger.info("------------- Creating new portal user token ------------");
                    return PortalTokenController.produceToken(user.getRealm(), user.getEmail(), user.getPass());
                case CM:
                    logger.info("------------- Creating new CM user token ------------");
                    return new HERETokenController().loginAndGenerateCMToken(user.getEmail(), user.getPass());
                case APP:
                    logger.info("------------- Creating new APP LIKE user token ------------");
                    String host = Conf.ns().getAuthUrlBase() + Conf.ns().getAuthUrlGetToken();
                    String clientIdValue = Conf.nsUsers().getConsumerApp().getAppKeyId();
                    String clientSecretValue = Conf.nsUsers().getConsumerApp().getAppKeySecret();
                    return ApplicationTokenController.createConsumerAppToken(host, clientIdValue, clientSecretValue);
                case DAIMLER:
                    logger.info("------------- Creating new Daimler user token ------------");
                    //temporary stub daimler token
                    return "11:22";
                case AA:
                    logger.info("------------- Creating new AA user token ------------");
                    return ApplicationTokenController.createConsumerAppToken(
                            Conf.ns().getAuthUrlBase() + Conf.ns().getAuthUrlGetToken(),
                            Conf.nsUsers().getAaService().getAppKeyId(),
                            Conf.nsUsers().getAaService().getAppKeySecret());
                default:
                    return StringUtils.EMPTY;
            }
        });
        return token;
    }

    public static String loadOrGenerate(String key, Supplier<String> supplier) {
        String currentT = SyncPointIO.readSyncToken(key);
        if (StringUtils.isEmpty(currentT)) {
            String token = supplier.get();
            if (StringUtils.isEmpty(token) || token.equals("Bearer null")) {
                //no valid token generated, no sync to server, unlock record...
                SyncPointIO.unlock(key);
            } else {
                SyncPointIO.writeNewTokenValue(key, token, 3599);
            }
            return token;
        } else {
            return currentT;
        }
    }

}
