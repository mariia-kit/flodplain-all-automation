package com.here.platform.cm.enums;

import com.here.platform.cm.rest.model.ConsentInfo.StateEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum BMWStatus {
    APPROVED(StateEnum.APPROVED),
    REJECTED(StateEnum.REJECTED),
    REVOKED(StateEnum.REVOKED),
    TIMED_OUT(StateEnum.EXPIRED);

    private final StateEnum cmStatus;
}
