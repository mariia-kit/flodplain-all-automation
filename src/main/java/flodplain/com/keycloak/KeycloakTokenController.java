package flodplain.com.keycloak;

import com.here.account.auth.OAuth1ClientCredentialsProvider;
import com.here.account.http.apache.ApacheHttpClientProvider;
import com.here.account.oauth2.ClientCredentialsGrantRequest;
import com.here.account.oauth2.HereAccount;


public class KeycloakTokenController {

    public synchronized static String createConsumerAppToken(String host, String clientIdValue,
            String clientSecretValue) {

        var tokenEndpoint = HereAccount.getTokenEndpoint(
                ApacheHttpClientProvider.builder().build(),
                new OAuth1ClientCredentialsProvider(host, clientIdValue, clientSecretValue)
        );
        var freshToken = tokenEndpoint.requestAutoRefreshingToken(new ClientCredentialsGrantRequest());

        return freshToken.get().getAccessToken();
    }

}
