package flodplain.com.customerdatamaster.dto;

import flodplain.com.common.helpers.authentification.AuthController;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;


@Data
@Builder
@AllArgsConstructor
public class User {

    private String
    email,
    token,
    refreshToken,
    pass,
    realm,
    userId,
    clientId,
    grantType,
    name;

 private UserType type;

        public User(String email, String pass, String realm, String userId) {
            this.email = email;
            this.pass = pass;
            this.realm = realm;
            this.userId = userId;
        }

    public User(String email, String pass) {
            this.email = email;
            this.pass = pass;
            this.realm = StringUtils.EMPTY;
            this.userId = StringUtils.EMPTY;
        }

    public User() {

        }

        public User withToken(String tokenValue) {
            this.setToken(tokenValue);
            return this;
        }

        public User withRealm(String realm) {
            this.setRealm(realm);
            return this;
        }

        public String getToken() {
            this.token = AuthController.getUserToken(this);
            return this.token;
        }

}
