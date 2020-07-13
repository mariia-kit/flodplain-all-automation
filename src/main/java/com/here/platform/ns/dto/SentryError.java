package com.here.platform.ns.dto;

import java.beans.ConstructorProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@EqualsAndHashCode
public class SentryError {

    private int status;
    private String error;
    private String errorDescription;

    @ConstructorProperties({"error", "error_description"})
    public SentryError(String error, String errorDescription) {
        this.error = error;
        this.errorDescription = errorDescription;
    }

    public SentryError(int statusCode, String error, String errorDescription) {
        status = statusCode;
        this.error = error;
        this.errorDescription = errorDescription;
    }

}
