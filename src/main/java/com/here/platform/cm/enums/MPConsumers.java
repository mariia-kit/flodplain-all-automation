package com.here.platform.cm.enums;

import com.here.platform.common.FileIO;
import com.here.platform.hereAccount.controllers.HereUserManagerController;
import com.here.platform.hereAccount.controllers.HereUserManagerController.HereUser;
import java.io.File;
import lombok.Data;
import lombok.Getter;


@Getter
public enum MPConsumers {

    OLP_CONS_1(
            "olp-here-mrkt-cons-1",
            "OLP Market Place Consumer Realm - 1",
            new HereUser("ns-automated-data-consumer@here.com", "g{#N2(mP(8", "olp-here-mrkt-cons-1")
    );

    private final String realm, consumerName;
    private final HereUser hereUserRepresentation;

    MPConsumers(
            String realm,
            String consumerName,
            HereUser hereUserRepresentation
    ) {
        this.realm = realm;
        this.consumerName = consumerName;
        this.hereUserRepresentation = hereUserRepresentation;
    }

    public String getToken() {
        var tokenValue = FileIO.readFile(consumerFile());
        if (tokenValue.isBlank()) {
            tokenValue = generateToken();
        }
        return tokenValue;
    }

    public String generateToken() {
        var accessToken = new HereUserManagerController().getHereCurrentToken(getHereUserRepresentation());
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
