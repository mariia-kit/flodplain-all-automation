package com.here.platform.common.strings;

import com.github.javafaker.Faker;
import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;


public class VIN {

    private final String vinValue;

    public VIN(String vinValue) {
        this.vinValue = vinValue;
    }

    /**
     * Generate valid VIN by standard but with custom length
     *
     * @param withSize 17 symbols applicable for "daimler_real" and other data provider IDs 18 symbols applicable only
     * for "daimler" provider ID
     * @return VIN in size
     */
    public static String generate(int withSize) {
        return new Faker().crypto().sha256()
                .replace("I", "").replace("O", "").replace("Q", "")
                .substring(0, withSize)
                .toUpperCase();
    }

    /**
     * Get the last 8 symbols from the VIN
     * @return
     */
    public String label() {
        return vinValue.substring(vinValue.length() - 8);
    }

    /**
     * Get hashed in sha512 VIN value
     */
    public String hashed() {
        return Hashing.sha512().hashString(vinValue, StandardCharsets.UTF_8).toString();
    }

}
