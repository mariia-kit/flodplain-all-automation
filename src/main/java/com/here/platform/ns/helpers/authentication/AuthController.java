package com.here.platform.ns.helpers.authentication;

import static com.here.platform.common.strings.SBB.sbb;
import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;

import com.here.platform.aaa.ApplicationTokenController;
import com.here.platform.aaa.BearerAuthorization;
import com.here.platform.aaa.PortalTokenController;
import com.here.platform.cm.controllers.HERETokenController;
import com.here.platform.cm.controllers.UserAccountController;
import com.here.platform.common.DataSubject;
import com.here.platform.common.config.Conf;
import com.here.platform.common.syncpoint.SyncPointIO;
import com.here.platform.ns.dto.User;
import com.here.platform.ns.dto.UserType_NS;
import io.restassured.response.Response;
import java.time.Instant;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


public class AuthController {

    private final static Logger logger = Logger.getLogger(AuthController.class);

    public static String getDataSubjectKey(String email) {
        return email + "_" + System.getProperty("env");
    }

    public static String getUserKey(User user) {
        return user.getEmail() + "_" + user.getRealm();
    }

    public synchronized static void setUserToken(User user) {
        String token = getUserToken(user);
        user.setToken(token);
    }

    public synchronized static String getDataSubjectToken(DataSubject dataSubject) {
        String key = getDataSubjectKey(dataSubject.getEmail());
        return AuthController.loadOrGenerate(key,
                () -> new HERETokenController().loginAndGenerateCMToken(dataSubject.getEmail(), dataSubject.getPass()),
                AuthController::verifyCMToken);
    }

    public synchronized static void deleteToken(DataSubject dataSubject) {
        String key = getDataSubjectKey(dataSubject.getEmail());
        SyncPointIO.deleteEntity(key);
    }

    public synchronized static void deleteToken(User user) {
        String key = getUserKey(user);
        SyncPointIO.deleteEntity(key);
    }

    public synchronized static void writeKeyValue(String key, String value) {
        SyncPointIO.writeNewTokenValue(key, value, 3500);
    }

    public synchronized static String getUserToken(User user) {
        String key = user.getType().equals(UserType_NS.CM) ? getDataSubjectKey(user.getEmail()) : getUserKey(user);
        String token = loadOrGenerate(key, () -> {
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
                case CMCONS:
                    logger.info("------------- Creating new CM Consumer token ------------");
                    return BearerAuthorization.init().getCmUserToken();
                default:
                    return StringUtils.EMPTY;
            }
        }, tokenToVerify -> {
            switch (user.getType()) {
                case AA:
                case CMCONS: return true;
                case CM: return verifyCMToken(tokenToVerify);
                default: return verifyHEREToken(tokenToVerify);
            }
        });
        return token;
    }

    @SneakyThrows
    public static String loadOrGenerate(String key, Supplier<String> supplier, Function<String, Boolean> verifier) {
        String currentT = SyncPointIO.readSyncToken(key);
        if (!StringUtils.isEmpty(currentT) && !currentT.contains("new")) {
            if (verifier.apply(currentT)) {
                return currentT;
            } else {
                step(sbb().append("Token").w().append(key).w().append(" is corrupted, need to re-generate.").bld());
                SyncPointIO.lock(key);
            }
        }
        try {
            String token = supplier.get();
            if (token == null || token.equals("Bearer null")) {
                System.err.println("Error 1 creating token for " + key + ": " + token);
                token = supplier.get();
            }
            if (token == null || token.equals("Bearer null")) {
                System.err.println("Error 2 creating token for " + key + ": " + token);
                token = supplier.get();
            }
            if (StringUtils.isEmpty(token) || token.equals("Bearer null")) {
                SyncPointIO.unlock(key);
                throw new RuntimeException(sbb("Error during generation of new token for sync:")
                        .append(key).append(" token is ").sQuoted(token).bld());
            } else {
                writeKeyValue(key, token);
            }
            return token;
        } catch (Error er){
            SyncPointIO.unlock(key);
            throw new RuntimeException("Error Writing sync entity fro server:", er);
        }
    }

    public static boolean verifyHEREToken(String token) {
        String url = Conf.ns().getAuthUrlBase();
        Response verify = given()
                .noFilters()
                .baseUri(url)
                .header("Authorization", "Bearer " + token)
                .get("/user/me");
        return verify.getStatusCode() == 200;
    }

    public static boolean verifyCMToken(String token) {
        Response verify = new UserAccountController().userAccountGetInfo("Bearer " + token);
        return verify.getStatusCode() == 200;
    }
}
