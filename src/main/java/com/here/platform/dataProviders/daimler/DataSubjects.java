package com.here.platform.dataProviders.daimler;

import com.here.platform.cm.controllers.HERETokenController;
import com.here.platform.common.FileIO;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;


public enum DataSubjects {

    //todo clean up dev/sit/prod "vin" table for CM
    _26CFFF5D2475F27C70("26CFFF5D2475F27C70", "noxohi7910@aprimail.com", "7wdh@#*EDjdk"),
    B9B1096165AF65FBB2("B9B1096165AF65FBB2", "bogix@appmail24.com", "$GAB.km_A2v6a@#"),
    CED702C914AA587F98("CED702C914AA587F98", "birahe7954@etcone.net", "GK9?TgFBsR9#ajt"),
    CF42CFD028810C284E("CF42CFD028810C284E", "xeviy@5sun.net", "ZD9ZnnvKt@&sd6t"),
    _33258E60D7384D1B3A("33258E60D7384D1B3A", "xavisil788@etopmail.com", "XB2HP2P/EQ@ZFEd"),
    _1AE0B89918406F0957("1AE0B89918406F0957", "jafej85911@win-777.net", "Eyc?!B,Bnfjp4N9"),
    _8AF07AED81A33D2C36("8AF07AED81A33D2C36", "poyiyap577@eliteseo.net", "vGi6294_uYp$M*v"),
    _605D2FE73389C15F05("605D2FE73389C15F05", "xobele7657@it-smart.org", "#BwC27YjH_&q$R_"),
    _52C0A3CD17548A0ADF("52C0A3CD17548A0ADF", "comoy82249@win-777.net", "CTwB@j&yqDCAKw8"),
    B978FB280D54D80444("B978FB280D54D80444", "riyeyij632@lywenw.com", "%6Lx_8uUnYHC%As"),
    _8AB90888E052A342F2("8AB90888E052A342F2", "kowemok405@lywenw.com", "Ayepa9W$+jCvkNg"),
    CC7F49BF608F46D1DB("CC7F49BF608F46D1DB", "cadaji7867@lefaqr5.com", "#-8Z8-2?4t!WLBt"),
    AD1D74EE1D30352A5E("AD1D74EE1D30352A5E", "tegiro9762@seberkd.com", "f,aeEP_84_B.H5&");

    private static final AtomicInteger atomicInteger = new AtomicInteger(-1);
    public final String vin, username, password;

    DataSubjects(String vin, String username, String password) {
        this.vin = vin;
        this.username = username;
        this.password = password;
    }

    public static DataSubjects getNext() {
        var dataSubjectsArray = values();
        if (atomicInteger.getAcquire() > dataSubjectsArray.length - 2) {
            atomicInteger.set(-1);
        }
        return dataSubjectsArray[atomicInteger.incrementAndGet()];
    }

    public static DataSubjects getByVin(String vin) {
        try {
            return valueOf(vin);
        } catch (IllegalArgumentException notFound) {
            return valueOf("_" + vin);
        }
    }

    public String getBearerToken() {
        String cmToken = FileIO.readFile(vinFile());
        if (StringUtils.isBlank(cmToken)) {
            return generateBearerToken();
        }
        return cmToken;
    }

    public void setBearerToken(String targetToken) {
        FileIO.writeStringToFile(vinFile(), targetToken);
    }

    @SneakyThrows
    public String generateBearerToken() {
        var token = new HERETokenController().loginAndGenerateCMToken(username, password);
        setBearerToken(token);
        return token;
    }

    private File vinFile() {
        return new File(String.format("%s/%s.token", FileIO.basePath, vin));
    }

}
