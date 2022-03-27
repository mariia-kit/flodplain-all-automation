package flodplain.com.customerdatamaster.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum UserType {

    MOBILE("mobile"),
    WEB("web"),
    SUPER_ADMIN("super_admin");

    private final String prefix;
}
