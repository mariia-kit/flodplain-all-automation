package com.here.platform.common;

import static com.here.platform.common.strings.SBB.sbb;

import com.github.javafaker.Faker;
import com.here.platform.common.strings.JConvert;
import io.qameta.allure.Allure;
import java.io.File;


public class VinsToFile implements FileIO {

    private final String
            fileName = Faker.instance().crypto().sha1().subSequence(0, 20).toString();
    private final String[] items;

    public VinsToFile(String... items) {
        this.items = items;
    }

    public File json() {
        var jsonFile = getFileWithType("json");
        logFilePreparationAsAllureStep(jsonFile);
        FileIO.writeStringToFile(jsonFile, new JConvert(items).toJson());

        return jsonFile;
    }

    public File csv() {
        var csvFile = getFileWithType("csv");
        logFilePreparationAsAllureStep(csvFile);
        FileIO.writeStringToFile(csvFile, String.join("\n", items));

        return csvFile;
    }

    /**
     * @return basePath/fileName.targetFileType
     */
    private File getFileWithType(String targetFileType) {
        return new File(sbb(basePath).slash().append(fileName).append(".").append(targetFileType).bld());
    }

    private void logFilePreparationAsAllureStep(File targetFile) {
        Allure.step(
                sbb("Prepare").w()
                        .sQuoted(targetFile.getName()).w()
                        .append("file with following VINs:").w()
                        .append("{").w()
                        .sQuoted(String.join(", ", items)).w()
                        .append("}")
                        .bld()
        );
    }

}
