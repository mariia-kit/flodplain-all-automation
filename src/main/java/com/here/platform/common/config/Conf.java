package com.here.platform.common.config;

import java.util.Optional;


public class Conf {

    private static NsConfig nsConf;
    private static NsUserConfig hereUser;
    private static CmConfig cmConfig;
    private static CmUserConfig cmUserConfig;
    private static MpConfig mpConfig;
    private static MpUserConfig mpUserConfig;

    public static NsConfig ns() {
        return returnConfig(nsConf, NsConfig.class);
    }

    public static NsUserConfig nsUsers() {
        return returnConfig(hereUser, NsUserConfig.class);
    }

    public static CmConfig cm() {
        return returnConfig(cmConfig, CmConfig.class);
    }

    public static CmUserConfig cmUsers() {
        return returnConfig(cmUserConfig, CmUserConfig.class);
    }

    public static MpConfig mp() {
        return returnConfig(mpConfig, MpConfig.class);
    }

    public static MpUserConfig mpUsers() {
        return returnConfig(mpUserConfig, MpUserConfig.class);
    }

    private static <T> T returnConfig(T conf, Class<T> type) {
        if (conf == null) {
            conf = getConfig(type);
        }
        return conf;
    }

    public static <T> T getConfig(Class<T> type) {
        YamlConfUrl annotation = Optional.ofNullable(type.getAnnotation(YamlConfUrl.class))
                .orElseThrow(() -> new RuntimeException("Config class " + type.getName() + " not properly configured!"));
        String env = System.getProperty("env");
        if ("stg".equalsIgnoreCase(env)) {
            env = "sit";
        }
        String url = annotation.configUrl().replace("{env}", env);

        if (ConfigLoader.isConfigExist(url)) {
            return ConfigLoader.yamlLoadConfig(url, type);
        } else {
            return ConfigLoader.yamlLoadConfig(annotation.configUrl().replace("{env}", "dev"), type);
        }
    }
}
