package flodplain.com.keycloak;

import com.here.account.auth.OAuth1ClientCredentialsProvider;
import com.here.account.http.apache.ApacheHttpClientProvider;
import com.here.account.oauth2.ClientCredentialsGrantRequest;
import com.here.account.oauth2.HereAccount;
import java.util.Properties;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;


@Getter
public class BearerAuthorization {

    private final String cmUserToken;

    private BearerAuthorization() {
        this.cmUserToken = generateCMUserToken();
    }

    public static BearerAuthorization init() {
        return LazyHolder.instance;
    }

    private String generateCMUserToken() {
        var tokenEndpoint = HereAccount.getTokenEndpoint(
                ApacheHttpClientProvider.builder().build(),
                CMCredentials.valueOf(System.getProperty("env").toUpperCase()).credentials
        );
        var freshToken = tokenEndpoint.requestAutoRefreshingToken(new ClientCredentialsGrantRequest());

        return freshToken.get().getAccessToken();
    }

    /**
     * Properties are setting from github
     */
    private enum CMCredentials {

        LOCAL(AAAHosts.STAGE, "stg.access.key.id", "stg.access.key.secret"),
        DEV(LOCAL.host, LOCAL.keyId, LOCAL.keySecret),
        SIT(LOCAL.host, LOCAL.keyId, LOCAL.keySecret),
        PROD(AAAHosts.PRODUCTION, "prod.access.key.id", "prod.access.key.secret");

        private final OAuth1ClientCredentialsProvider credentials;
        private final String host;
        private final String keyId;
        private final String keySecret;

        @SneakyThrows
        CMCredentials(String host, String keyId, String keySecret) {
            this.host = host;
            this.keyId = keyId;
            this.keySecret = keySecret;
            String clientIdValue, clientSecretValue;
            if (StringUtils.isBlank(System.getProperty(keyId))) {
                var inputStream = getClass().getClassLoader().getResourceAsStream("cm-access.properties");
                var prop = new Properties();
                prop.load(inputStream);

                clientIdValue = prop.getProperty(keyId);
                clientSecretValue = prop.getProperty(keySecret);
            } else {
                clientIdValue = System.getProperty(keyId);
                clientSecretValue = System.getProperty(keySecret);
            }

            this.credentials = new OAuth1ClientCredentialsProvider(host, clientIdValue, clientSecretValue);
        }

    }

    private static class LazyHolder {

        static final BearerAuthorization instance = new BearerAuthorization();

    }

}
