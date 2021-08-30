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

    public static ProxyError getDeletedResourceNotFoundError(Long resourceId) {
        return new ProxyError(
                404,
                "Service provider resource not found",
                "E504013",
                "Attempt to access service provider resource not registered or accessible (resourceId=" + resourceId + ")",
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

    public static ProxyError getNotValidFieldNotUniqueResourcePath() {
        return new ProxyError(
                400,
                "Invalid JSON field",
                "E504003",
                "<path> field must be distinct",
                "Field <path> must comply with expected format",
                "invalid_json_field");
    }

    public static ProxyError getNotValidFieldNotUniqueResourceTitle() {
        return new ProxyError(
                400,
                "Invalid JSON field",
                "E504003",
                "<title> field must be distinct",
                "Field <title> must comply with expected format",
                "invalid_json_field");
    }

    public static ProxyError getProviderResourceAlreadyExistsError(String title, String path) {
        return new ProxyError(
                409,
                "Service provider resource already exists",
                "E504019",
                "Attempt to add resources when the same resources already exists [title='" + title + "', path='" + path + "']",
                "Use unique Resource title and path",
                "service_provider_resource_already_exists");
    }

    public static ProxyError getWrongPoviderType(String providerType) {
        return new ProxyError(
                400,
                "Error processing JSON",
                "E504018",
                "Could not resolve type id 'REST' as a subtype of `com.here.platform.mktproxy.backend.rest.model.NewServiceProvider`: known type ids = [AWS, REST_API]\n at [Source: (InputStreamReader); line: 1, column: 32]",
                "Verify JSON payload for valid syntax",
                "json_processing_failure");
    }

    public static ProxyError getEmptyPoviderType(String providerType) {
        return new ProxyError(
                400,
                "Error processing JSON",
                "E504018",
                "Could not resolve type id '' as a subtype of `com.here.platform.mktproxy.backend.rest.model.NewServiceProvider`: known type ids = [AWS, REST_API]\n at [Source: (InputStreamReader); line: 1, column: 32]",
                "Verify JSON payload for valid syntax",
                "json_processing_failure");
    }

    public static ProxyError getNoPoviderType() {
        return new ProxyError(
                400,
                "Error processing JSON",
                "E504018",
                "Could not resolve subtype of [simple type, class com.here.platform.mktproxy.backend.rest.model.NewServiceProvider]: missing type id property 'providerType'\n at [Source: (InputStreamReader); line: 1, column: 148]",
                "Verify JSON payload for valid syntax",
                "json_processing_failure");
    }
}
