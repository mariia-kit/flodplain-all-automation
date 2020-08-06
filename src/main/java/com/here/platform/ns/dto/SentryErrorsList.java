package com.here.platform.ns.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum SentryErrorsList {
    TOKEN_NOT_FOUND(new SentryError(
            HttpStatus.SC_UNAUTHORIZED,
            "Unauthorized",
            "Bearer token invalid. Bearer missing or bearer value missing.")),
    TOKEN_INVALID(new SentryError(
            HttpStatus.SC_UNAUTHORIZED,
            "Unauthorized",
            "Token Validation Failure - Unable to verify signature segment")),
    METHOD_NOT_ALLOWED(new SentryError(
            HttpStatus.SC_METHOD_NOT_ALLOWED,
            "Method not allowed for this action",
            "Method not allowed for this action")),
    FORBIDDEN(new SentryError(
            HttpStatus.SC_FORBIDDEN,
            "Forbidden",
            "These credentials do not authorize access"));

    private final SentryError error;
}
