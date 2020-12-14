package com.here.platform.common.syncpoint;

import com.here.platform.common.strings.SBB;
import com.here.platform.dataProviders.reference.controllers.ReferenceProviderController;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;


public class SyncPointIO {

    @SneakyThrows
    public static String readSyncToken(String key) {
        SyncEntity record;
        ReferenceProviderController referenceProviderController = new ReferenceProviderController();
        Response getResp = referenceProviderController.readSyncEntity(key);

        if (getResp.getStatusCode() == HttpStatus.SC_BAD_REQUEST) {
            getResp = referenceProviderController.readSyncEntity(key);
        }
        if (getResp.getStatusCode() == HttpStatus.SC_OK) {
            record = getResp.then().extract().as(SyncEntity.class);
        } else if (getResp.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            writeNewTokenValue(key, "", 3599);
            lock(key);
            return StringUtils.EMPTY;
        } else {
            throw new RuntimeException(SBB.sbb()
                    .append("Error reading sync entity fro server:").w()
                    .append(getResp.getStatusCode()).w()
                    .append(getResp.body().print()).build());
        }

        if (record.isLocked()) {
            if (record.getValue().isBlank()) {
                return StringUtils.EMPTY;
            } else {
                record = waitForUnLock(key);
            }
        }
        return record.getValue();
    }

    public static void writeNewTokenValue(String key, String value, long expirationTime) {
        new ReferenceProviderController().writeSyncEntity(key, value, expirationTime);
    }

    public static List<SyncEntity> getAllEtityes() {
        return Arrays.asList(new ReferenceProviderController().getAllEntities().then().extract().as(SyncEntity[].class));
    }

    public static void deleteEntity(String key) {
        new ReferenceProviderController().deleteSyncEtity(key);
    }

    @SneakyThrows
    private static SyncEntity waitForUnLock(String key) {
        ReferenceProviderController referenceProviderController = new ReferenceProviderController();
        int max = 10;
        while(max > 0) {
            Thread.sleep(3000);
            max--;
            SyncEntity record = referenceProviderController.readSyncEntity(key).then().extract().as(SyncEntity.class);
            if (!record.isLocked()) {
                return record;
            }
        }
        throw new RuntimeException("Failed to wait for Sync to unlock:" + key);
    }

    public static void unlock(String key) {
        new ReferenceProviderController().unlockSyncEtity(key);
    }

    public static void lock(String key) {
        new ReferenceProviderController().lockSyncEtity(key);
    }
}
