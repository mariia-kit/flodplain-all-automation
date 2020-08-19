package com.here.platform.common.config;

import com.here.platform.ns.dto.User;
import lombok.Data;


@Data
@YamlConfUrl(configUrl = "{env}/mp-users.yaml")
public class MpUserConfig {

    private User mpConsumer;
    private User mpProvider;
}
