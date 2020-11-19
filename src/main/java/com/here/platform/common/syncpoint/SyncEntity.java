package com.here.platform.common.syncpoint;

import lombok.Data;


@Data
public class SyncEntity {
    private String key;
    private String value;

    private long created;
    private long expireTime;

    private boolean locked;
}
