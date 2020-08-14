package com.here.platform.ns.dto;

import java.beans.ConstructorProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;


@Getter
@Setter
@ToString
@EqualsAndHashCode(exclude = "correlationId")
public class NSError {

    private String title;
    private int status;
    private String code;
    private String cause;
    private String action;
    private String correlationId;

    @ConstructorProperties({"title", "status", "code", "cause", "action", "correlationId",
            "exveNote", "exveErrorId", "exveErrorMsg", "exveErrorRef"})
    public NSError(String title, int status, String code, String cause, String action,
            String correlationId, String a1, String a2, String a3, String a4) {
        this.title = title;
        this.status = status;
        this.code = code;
        this.cause = cause;
        this.action = action;
        this.correlationId = correlationId;
    }

    public NSError(String title, int status, String code, String cause, String action) {
        this.title = title;
        this.status = status;
        this.code = code;
        this.cause = cause;
        this.action = action;
        this.correlationId = StringUtils.EMPTY;
    }

}
