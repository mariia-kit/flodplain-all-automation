package flodplain.com.customerdatamaster.dto;

import flodplain.com.common.config.Conf;
import lombok.AllArgsConstructor;


@AllArgsConstructor
public enum UserEnum {

    WEB(Conf.nsUsers().getConsumer());

    private final User user;

    public User getUser() {
        return user;
    }

    public String getToken() {
        return user.getToken();
    }
}
