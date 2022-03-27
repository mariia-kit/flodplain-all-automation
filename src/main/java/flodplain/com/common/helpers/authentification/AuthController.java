package flodplain.com.common.helpers.authentification;

import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;

import flodplain.com.common.config.Conf;
import flodplain.com.customerdatamaster.dto.User;
import flodplain.com.customerdatamaster.dto.UserType;
import flodplain.com.keycloak.BearerAuthorization;
import flodplain.com.keycloak.KeycloakTokenController;
import io.restassured.response.Response;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


public class AuthController {

    private final static Logger logger = Logger.getLogger(AuthController.class);

    public static String getDataSubjectKey(String email) {
        return email + "_" + System.getProperty("env");
    }

    public static String getUserKey(User user) {
        return user.getClass() + "_" + user.getRealm();
    }

    public synchronized static void setUserToken(User user) {
        String token = getUserToken(user);
        user.setToken(token);
    }

    public synchronized static void writeKeyValue(String key, String value) {
        //SyncPointIO.writeNewTokenValue(key, value, 3500);
    }

    public synchronized static String getUserToken(User user) {
        String key = user.getType().equals(UserType.WEB) ? getDataSubjectKey(user.getEmail()) : getUserKey(user);
        String token = loadOrGenerate(key, () -> {
            switch (user.getType()) {
                case WEB:
                    logger.info("------------- Creating new APP LIKE user token ------------");
                    String host = Conf.ns().getAuthUrlBase() + Conf.ns().getAuthUrlGetToken();
                    String clientIdValue = Conf.nsUsers().getConsumer().getClientId();
                    String clientSecretValue = Conf.nsUsers().getConsumer().getClientId();
                    return KeycloakTokenController.createConsumerAppToken(host, clientIdValue, clientSecretValue);
                case SUPER_ADMIN:
                    logger.info("------------- Creating new CM Consumer token ------------");
                    return BearerAuthorization.init().getCmUserToken();
                default:
                    return StringUtils.EMPTY;
            }
        }, tokenToVerify -> {
            switch (user.getType()) {
                case WEB:
                case SUPER_ADMIN:
                    return true;
                //case CM: return verifyCMToken(tokenToVerify);
                //default: return verifyHEREToken(tokenToVerify);
            }
            return null;
        });
        return token;
    }

    @SneakyThrows
    public static String loadOrGenerate(String key, Supplier<String> supplier, Function<String, Boolean> verifier) {
        try {
            String token = supplier.get();
            if (token == null || token.equals("Bearer null")) {
                System.err.println("Error 1 creating token for " + key + ": " + token);
                token = supplier.get();
            }
            if (StringUtils.isEmpty(token) || token.equals("Bearer null")) {

            } else {
                writeKeyValue(key, token);
            }
            return token;
        } catch (Error er){
            //SyncPointIO.unlock(key);
            throw new RuntimeException("Error Writing sync entity fro server:", er);
        }
    }

    public static boolean verifyHEREToken(String token) {
        String url = Conf.ns().getHost();
        Response verify = given()
                .noFilters()
                .baseUri(url)
                .header("Authorization", "Bearer " + token)
                .get("/user/me");
        return verify.getStatusCode() == 200;
    }

}
