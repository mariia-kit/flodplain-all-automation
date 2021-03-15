package com.here.platform.proxy.dto;

import org.apache.commons.lang3.StringUtils;


public class ProxyErrorList {

    public static ProxyError getDeleteConflictError(Long providerId) {
        return new ProxyError(
                403,
                "Error accessing extsvc provider",
                "E504017",
                "Provider " + providerId + " is in use and cannot be modified",
                "Ensure that provider is not in use/locked and try this operation again",
                "extsvc_provider_is_in_use");
    }

    public static ProxyError getProviderNotFoundError(Long providerId) {
        return new ProxyError(
                404,
                "Service provider resource not found",
                "E504013",
                "Attempt to access service provider resource not registered or accessible (id=" + providerId + ")",
                "Use service provider resource register for access",
                "service_provider_resource_not_found");
    }

    public static ProxyTunnelError getResourceNotAccessibleError(String path) {
        return new ProxyTunnelError(
                404,
                "Not Found",
                "No message available",
                path,
                StringUtils.EMPTY);
    }

    public static ProxyError getResourceNotFoundError(String path) {
        return new ProxyError(
                404,
                "Service provider resource not found",
                "E504013",
                "Attempt to access service provider resource not registered or accessible (path=" + path + ")",
                "Use service provider resource register for access",
                "service_provider_resource_not_found");
    }

    public static ProxyError getSQLError() {
        return new ProxyError(
                500,
                "Error executing database request",
                "E504006",
                "Failed to execute SQL statement",
                "",
                "sql_error");
    }

    public static ProxyError getNoAccessError(String userId, String hrn) {
        return new ProxyError(
                403,
                "Error accessing extsvc resource",
                "E504014",
                "User " + userId + " does not have readResource permissions on extsvc resource " + hrn,
                "Verify user has policy/permissions granted via ExtSvc Marketplace subscriptions",
                "extsvc_resource_forbidden");
    }

    public static ProxyError getNotValidField() {
        return new ProxyError(
                400,
                "Missing required JSON field",
                "E504002",
                "Missing JSON field <\"authMethod\":com.here.platform.mktproxy.backend.db.model.CredentialAuthMethod>",
                "Verify JSON payload for missing/wrong fields",
                "missing_required_json_field");
    }
}
