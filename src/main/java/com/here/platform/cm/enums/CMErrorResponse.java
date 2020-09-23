package com.here.platform.cm.enums;


import static com.here.platform.cm.enums.CMErrorResponse.Constants.CORRECT_DATA;
import static com.here.platform.cm.enums.CMErrorResponse.Constants.CORRECT_VALUE;
import static com.here.platform.cm.enums.CMErrorResponse.Constants.TRY_AGAIN_ACTION;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;


@Getter
@AllArgsConstructor
@ToString
public enum CMErrorResponse {

    INTERNAL_SERVER_ERROR("E503100", "Unexpected processing exception", Constants.TRY_AGAIN_ACTION),
    METHOD_NOT_SUPPORTED("E503101", "Invalid request method",
            "Use Consent Management REST Service OpenAPI specification methods only"),
    MEDIA_TYPE_NOT_SUPPORTED("E503102", "Invalid request media type",
            "Use Consent Management REST Service OpenAPI specification media types only"),
    CONSENT_NOT_APPROVED("E503103", "Consent not approved", CORRECT_DATA + " for consent approval"),
    CONSENT_NOT_FOUND("E503104", "Consent not found", CORRECT_DATA + " for consent"),
    CONSENT_REQUEST_VALIDATION("E503105", "Consent request validation",
            CORRECT_VALUE + " for consent request creation"),
    CONSENT_REQUEST_UPDATE("E503131", "Consent request update error", CORRECT_DATA),
    CONSENT_VALIDATION("E503106", "Consent validation failed", CORRECT_VALUE + " for consent"),
    CONSUMER_VALIDATION("E503107", "Consumer validation failed", CORRECT_VALUE + " for consumer"),
    DATA_MANIPULATION("E503108", "Data manipulation failed", CORRECT_DATA),
    PROVIDER_TOKEN_REFRESH("E503109", "Token refresh exception", CORRECT_DATA),
    TOKEN_VALIDATION("E503110", "Token validation failed", "Provide correct token"),
    METRIC_EXCEPTION("E503111", "Metrics exception", TRY_AGAIN_ACTION),
    CONSUMER_NOT_FOUND("E503112", "Consumer not found", CORRECT_DATA),
    PROVIDER_NOT_FOUND("E503129", "Provider not found", CORRECT_DATA),
    PROVIDER_APPLICATION_NOT_FOUND("E503130", "Provider application not found", CORRECT_DATA),
    CONSENT_REQUEST_NOT_FOUND("E503113", "Consent request not found", CORRECT_DATA),
    CREDENTIALS_EXCEPTION("E503114", "Invalid credentials", CORRECT_DATA),
    CONSUMER_OAUTH_SERVICE_NOT_SUPPORTED("E503115", "No implementation found for consumer",
            "Please contact Consent Management team"),
    SESSION_ATTRIBUTE_NOT_PRESENT("E503116", "Session attribute not present",
            "Ensure your browser supports cookies and try full consent flow from the beginning"),
    OAUTH_PROCESS_EXCEPTION("E503117", "Error response from CM OAUTH service",
            "Try again later or contact Consent Management team"),
    ACCESS_TOKEN_VIN_MISMATCH("E503118", "Access token does not match with provided VIN",
            "Please " + CORRECT_DATA.toLowerCase()),
    COMMUNICATION_EXCEPTION("E503119", "Error while communicating to external service", TRY_AGAIN_ACTION),
    DAIMLER_EXCEPTION("E503120", "Data can't be processed by Daimler", CORRECT_DATA),
    CHAIN_CODES_EXCEPTION("E503121", "Data can't be processed by Chaincodes Service", CORRECT_DATA),
    VIN_VERIFICATION("E503123", "VIN verification", "Requested VIN is not a part of consent"),
    PARAMETER_VALIDATION("E503124", "Request parameter validation failed", CORRECT_DATA),
    TOO_LARGE_EXCEPTION("E503125", "Content is too large", "Write less data"),
    CONSENT_UPDATE_INFO_NOT_FOUND("E503136", "ConsentRequestAsyncUpdateInfo is not found", CORRECT_DATA),
    VIN_NOT_FOUND_EXCEPTION("E503142", "VIN not found", CORRECT_DATA),
    ALREADY_EXIST_EXCEPTION("E503143", "Entity already exist", CORRECT_DATA),
    CONSENT_ALREADY_REVOKED("E503148", "Revoked consent couldn't be approved", CORRECT_DATA);


    private final String code, title, action;


    public interface Constants {

        String
                CORRECT_DATA = "Provide correct data",
                CORRECT_VALUE = "Provide correct value",
                TRY_AGAIN_ACTION = "Try again later or contact Consent Management team";

    }

}
