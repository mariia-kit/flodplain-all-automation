package com.here.platform.common.syncpoint;

import static com.here.platform.common.strings.SBB.sbb;

import com.here.platform.dataProviders.reference.controllers.ReferenceProviderController;
import com.here.platform.ns.helpers.UniqueId;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.List;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;


@UtilityClass
public class SyncPointIO {

    private final ReferenceProviderController referenceProviderController = new ReferenceProviderController();

    @SneakyThrows
    public String readSyncToken(String key) {
        SyncEntity record;
        Response getResp = referenceProviderController.readSyncEntity(key);
        String mySignature = sbb("new-").append(UniqueId.getUniqueKey()).bld();

        if (getResp.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
            getResp = referenceProviderController.readSyncEntity(key);
        }
        if (getResp.getStatusCode() == HttpStatus.SC_OK) {
            record = getResp.then().extract().as(SyncEntity.class);
        } else if (getResp.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            writeNewTokenValue(key, mySignature, 3599);
            lock(key);
            return StringUtils.EMPTY;
        } else {
            throw new RuntimeException(sbb()
                    .append("Error reading sync entity fro server:").w()
                    .append(getResp.getStatusCode()).w()
                    .append(getResp.body().print()).build());
        }

        if (record.isLocked() || record.getValue().contains("new")) {
            if (record.getValue().isBlank()) {
                writeNewTokenValue(key, mySignature, 3599);
                lock(key);
                return waitIsItMine(key, mySignature);
            } else {
                Thread.sleep(1000);
                record = waitForUnLock(key);
            }
        }
        return record.getValue();
    }

    @SneakyThrows
    public String waitIsItMine(String key, String mySignature) {
        Thread.sleep(1000);
        SyncEntity record = referenceProviderController.readSyncEntity(key).then().extract().as(SyncEntity.class);
        if (record.getValue().equals(mySignature)) {
            return StringUtils.EMPTY;
        } else {
            return waitForUnLock(key).getValue();
        }
    }

    public void writeNewTokenValue(String key, String value, long expirationTime) {
        referenceProviderController.writeSyncEntity(key, value, expirationTime);
    }

    public List<SyncEntity> getAllEtityes() {
        return Arrays.asList(referenceProviderController.getAllEntities().as(SyncEntity[].class));
    }

    public void deleteEntity(String key) {
        referenceProviderController.deleteSyncEtity(key);
    }

    @SneakyThrows
    private SyncEntity waitForUnLock(String key) {
        int maxTimesToWait = 10;
        while (maxTimesToWait > 0) {
            Thread.sleep(3000);
            maxTimesToWait--;
            SyncEntity record = referenceProviderController.readSyncEntity(key).as(SyncEntity.class);
            if (!record.isLocked()) {
                return record;
            }
        }
        throw new RuntimeException(sbb("Failed to wait for Sync to unlock:").append(key).bld());
    }

    public void unlock(String key) {
        referenceProviderController.unlockSyncEtity(key);
    }

    public void lock(String key) {
        referenceProviderController.lockSyncEtity(key);
    }

}
