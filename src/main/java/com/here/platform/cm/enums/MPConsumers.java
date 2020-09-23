package com.here.platform.cm.enums;

import com.here.platform.aaa.PortalTokenController;
import com.here.platform.common.FileIO;
import com.here.platform.common.config.Conf;
import com.here.platform.hereAccount.controllers.HereUserManagerController.HereUser;
import java.io.File;
import lombok.Data;
import lombok.Getter;


@Getter
public enum MPConsumers {

    OLP_CONS_1(
            Conf.mpUsers().getMpConsumer().getRealm(),
            "OLP Market Place Consumer Realm - 1",
            new HereUser(Conf.mpUsers().getMpConsumer().getEmail(), Conf.mpUsers().getMpConsumer().getPass(), Conf.mpUsers().getMpConsumer().getRealm())
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
        var accessToken = PortalTokenController.produceToken(hereUserRepresentation.getRealm(),
                hereUserRepresentation.getEmail(), hereUserRepresentation.getPassword());
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
