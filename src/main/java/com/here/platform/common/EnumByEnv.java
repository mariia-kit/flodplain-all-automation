package com.here.platform.common;

import com.google.common.base.Strings;
import lombok.SneakyThrows;


public interface EnumByEnv {

    @SneakyThrows
    static <T extends Enum<T>> T get(Class<T> enumClass) {
        String envValue = System.getProperty("env");
        try {
            return T.valueOf(enumClass, envValue.toUpperCase());
        } catch (Exception e) {
            var delimiter = Strings.repeat("*", 50);
            var illegalEnvValueExeption = new StringBuilder().append("\n\n")
                    .append(delimiter).append("\n\n")
                    .append("Valid values for 'env' = [local, dev, sit, prod]").append("\n")
                    .append("But was: ").append(envValue).append("\n")
                    .append("Please set system property 'env' before run:").append("\n")
                    .append("- via -Denv=envValue").append("\n")
                    .append("- via System.setProperty(\"env\", \"envValue\");").append("\n\n")
                    .append(delimiter).append("\n\n")
                    .toString();

            throw new IllegalArgumentException(illegalEnvValueExeption);
        }
    }

}
