package com.here.platform.cm.enums;

import com.here.platform.aaa.AAAHosts;
import com.here.platform.common.FileIO;
import com.here.platform.common.HttpClient;
import com.here.platform.common.JConvert;
import java.io.File;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Map;
import lombok.Data;
import lombok.Getter;


@Getter
public enum MPConsumers {

    OLP_CONS_1(
            "olp-here-mrkt-cons-1",
            "OLP Market Place Consumer Realm - 1",
            Map.of(
                    "email", "ns-automated-data-consumer@here.com",
                    "password", "g{#N2(mP(8",
                    "clientId", "ha-test-app-1",
                    "clientSecret", "ha-test-secret-1",
                    "grantType", "password",
                    "countryCode", "USA",
                    "language", "en",
                    "tokenFormat", "hN"
            )
    );

    private final String realm, consumerName;
    private final Map<String, String> consumerCredentials;

    MPConsumers(
            String realm,
            String consumerName,
            Map<String, String> consumerCredentials
    ) {
        this.realm = realm;
        this.consumerName = consumerName;
        this.consumerCredentials = consumerCredentials;
    }

    public String getToken() {
        var tokenValue = FileIO.readFile(consumerFile());
        if (tokenValue.isBlank()) {
            tokenValue = generateBearerToken();
        }
        return tokenValue;
    }

    public String generateBearerToken() {
        String stgHereOAuthTokenUrl = AAAHosts.STAGE; //dev and sit only

        var consumerCredentials = getConsumerCredentials();

        var oauth2TokenRequest = HttpRequest.newBuilder(URI.create(stgHereOAuthTokenUrl))
                .header("x-ha-realm", getRealm())
                .header("Content-Type", "application/json")
                .POST(new JConvert(consumerCredentials).toBodyPublisher())
                .build();

        var accessToken = new JConvert(new HttpClient().basic().send(oauth2TokenRequest))
                .responseBodyToObject(TokenResponse.class)
                .getAccessToken();

        FileIO.writeStringToFile(consumerFile(), accessToken);
        return accessToken;
    }

    private File consumerFile() {
        return new File(String.format("%s/%s.token", FileIO.basePath, realm));
    }

    @Data
    static class TokenResponse {

        private int expiresIn;
        private String accessToken, tokenType, userId, refreshToken;

    }

}
