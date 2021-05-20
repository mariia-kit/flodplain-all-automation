package com.here.platform.common.config;

import java.util.Optional;


public class Conf {

    private static NsConfig nsConf;
    private static NsUserConfig hereUser;
    private static CmConfig cmConfig;
    private static CmUserConfig cmUserConfig;
    private static MpConfig mpConfig;
    private static MpUserConfig mpUserConfig;
    private static ProxyConfig proxyConfig;

    public static NsConfig ns() {
        return getConfig(nsConf, NsConfig.class);
    }

    public static NsUserConfig nsUsers() {
        return getConfig(hereUser, NsUserConfig.class);
    }

    public static CmConfig cm() {
        return getConfig(cmConfig, CmConfig.class);
    }

    public static CmUserConfig cmUsers() {
        return getConfig(cmUserConfig, CmUserConfig.class);
    }

    public static MpConfig mp() {
        return getConfig(mpConfig, MpConfig.class);
    }

    public static MpUserConfig mpUsers() {
        return getConfig(mpUserConfig, MpUserConfig.class);
    }

    public static ProxyConfig proxy() {
        return getConfig(proxyConfig, ProxyConfig.class);
    }

    private static <T> T getConfig(T conf, Class<T> type) {
        if (conf == null) {
            conf = loadConfig(type);
        }
        return conf;
    }

    private static <T> T loadConfig(Class<T> type) {
        YamlConfUrl annotation = Optional.ofNullable(type.getAnnotation(YamlConfUrl.class))
                .orElseThrow(() -> new RuntimeException(type.getName() + " not configured"));

        String environment = System.getProperty("env", "dev");
        if ("stg".equalsIgnoreCase(environment)) {
            environment = "sit";
        }

        String partFileName = System.getenv(annotation.propertyName());
        String fileName = environment + "_" + partFileName;
        String filePath = System.getenv(fileName);

        return ConfigLoader.yamlLoadConfig(filePath, type);
    }

}
