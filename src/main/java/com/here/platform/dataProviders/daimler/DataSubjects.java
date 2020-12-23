package com.here.platform.dataProviders.daimler;

import static com.here.platform.common.strings.SBB.sbb;

import com.here.platform.cm.controllers.HERETokenController;
import com.here.platform.common.DataSubject;
import com.here.platform.common.FileIO;
import com.here.platform.common.config.Conf;
import com.here.platform.common.syncpoint.SyncPointIO;
import com.here.platform.ns.helpers.authentication.AuthController;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;


public enum DataSubjects {

    //todo clean up dev/sit/prod "vin" table for CM
    _26CFFF5D2475F27C70(Conf.cmUsers().getDataSubj1()),
    B9B1096165AF65FBB2(Conf.cmUsers().getDataSubj2()),
    CED702C914AA587F98(Conf.cmUsers().getDataSubj3()),
    CF42CFD028810C284E(Conf.cmUsers().getDataSubj4()),
    _33258E60D7384D1B3A(Conf.cmUsers().getDataSubj5()),
    _1AE0B89918406F0957(Conf.cmUsers().getDataSubj6()),
    _8AF07AED81A33D2C36(Conf.cmUsers().getDataSubj7()),
    _605D2FE73389C15F05(Conf.cmUsers().getDataSubj8()),
    _52C0A3CD17548A0ADF(Conf.cmUsers().getDataSubj9()),
    B978FB280D54D80444(Conf.cmUsers().getDataSubj10()),
    _8AB90888E052A342F2(Conf.cmUsers().getDataSubj11()),
    CC7F49BF608F46D1DB(Conf.cmUsers().getDataSubj12()),
    AD1D74EE1D30352A5E(Conf.cmUsers().getDataSubj13()),
    _2AD190A6AD057824E(Conf.cmUsers().getDataSubj14()),
    _857903401504142745(Conf.cmUsers().getDataSubj15()),
    _352903401504142980(Conf.cmUsers().getDataSubj16());

    private static final AtomicInteger atomicInteger = new AtomicInteger(-1);
    @Getter
    public final DataSubject dataSubject;

    DataSubjects(DataSubject dataSubject) {
        this.dataSubject = dataSubject;
    }

    public static DataSubjects getNextBy18VINLength() {
        return getNextVinLength(18);
    }

    public static DataSubjects getNextBy17VINLength() {
        return getNextVinLength(17);
    }

    public static DataSubjects getNextVinLength(int vinLength) {
        var dataSubjectsArray = Stream.of(values())
                .filter(subj -> subj.getVin().length() == vinLength)
                .collect(Collectors.toList());
        if (atomicInteger.getAcquire() >= dataSubjectsArray.size() - 1) {
            atomicInteger.set(-1);
        }
        return dataSubjectsArray.get(atomicInteger.incrementAndGet());
    }

    public static DataSubjects getByVin(String vin) {
        return Stream.of(values())
                .filter(subj -> vin.equals(subj.getVin()))
                .findFirst().orElseThrow(() -> new RuntimeException(
                        sbb("No DataSubject with vin").w().sQuoted(vin).w().append("detected.").bld()
                ));
    }

    public String getUserName() {
        return dataSubject.getEmail();
    }

    public String getPass() {
        return dataSubject.getPass();
    }

    public String getVin() {
        return dataSubject.getVin();
    }

    public String getBearerToken() {
        return getBearerToken(dataSubject);
    }

    public static String getBearerToken(DataSubject dataSubject) {
        return AuthController.getDataSubjectToken(dataSubject);
    }

}
