package com.here.platform.common;

import com.github.javafaker.Faker;
import java.io.File;


public class VinsToFile implements FileIO {

    private final String
            fileName = Faker.instance().crypto().sha1().subSequence(0, 20).toString();
    private final String[] items;

    public VinsToFile(String... items) {
        this.items = items;
    }

    public File json() {
        var jsonFile = new File(String.format("%s/%s.json", basePath, fileName));
        FileIO.writeStringToFile(jsonFile, new JConvert(items).toJson());

        return jsonFile;
    }

    public File csv() {
        var csvFile = new File(String.format("%s/%s.csv", basePath, fileName));
        FileIO.writeStringToFile(csvFile, String.join("\n", items));

        return csvFile;
    }

}
