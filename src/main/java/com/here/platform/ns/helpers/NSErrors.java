package com.here.platform.ns.helpers;

import static com.here.platform.common.strings.SBB.sbb;

import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.DataProvider;
import com.here.platform.ns.dto.NSError;
import com.here.platform.ns.dto.ProviderResource;
import org.apache.http.HttpStatus;

//todo refactor with SBB
public class NSErrors {

    public static NSError getContainersForProviderNotFoundError(String providerName) {
        return new NSError("Incorrect input data",
                HttpStatus.SC_NOT_FOUND,
                "E502116",
                "Containers for data provider '" + providerName + "' not found",
                "Request a valid container");
    }

    public static NSError getContainersNotFoundError(Container container) {
        return new NSError("Incorrect input data",
                HttpStatus.SC_NOT_FOUND,
                "E502116",
                String.format("Container name '%s' for data provider '%s' not found",
                        container.getId(), container.getDataProviderName()),
                "Request a valid container");
    }

    public static NSError getContainerIncorrectBodyError() {
        return new NSError("Message is not readable",
                HttpStatus.SC_BAD_REQUEST,
                "E502104",
                "Incorrect request body received",
                "Create request body according to NS OpenAPI specification");
    }

    public static NSError getContainerDataManipulationError(Container container) {
        return new NSError("Data manipulation failed",
                HttpStatus.SC_BAD_REQUEST,
                "E502118",
                "Couldn't save container: " + container.toString(),
                "Provide correct data");
    }

    public static NSError getResourceDataManipulationError(ProviderResource resource, DataProvider provider) {
        return new NSError("Data manipulation failed",
                HttpStatus.SC_BAD_REQUEST,
                "E502118",
                "Couldn't save resource '" + resource.getName() + "' for data provider: '"
                        + provider.getName() + "'",
                "Provide correct data");
    }

    public static NSError getContainerInvalidFieldError(String fieldName) {
        return new NSError("Input data failed validation",
                HttpStatus.SC_BAD_REQUEST,
                "E502117",
                "Container property '" + fieldName + "' must be provided",
                "Request a valid container id");
    }

    public static NSError getContainerFieldValidationError(String fieldName) {
        return new NSError("Input data failed validation",
                HttpStatus.SC_BAD_REQUEST,
                "E502117",
                "Container property '" + fieldName + "' must be matched with '[a-z0-9\\-]{1,100}' regex",
                "Request a valid container id");
    }

    public static NSError getProviderInvalidError(String fieldName) {
        return new NSError("Input data failed validation",
                HttpStatus.SC_BAD_REQUEST,
                "E502124",
                String.format("Data provider property '%s' must be provided", fieldName),
                "Request a valid provider id");
    }

    public static NSError getCouldntDeleteProviderError(String providerName,
            String containersNames) {
        return new NSError("Incorrect input data",
                HttpStatus.SC_CONFLICT,
                "E502108",
                String.format(
                        "Couldn't delete data provider: %s, it is used in containers info: %s",
                        providerName, containersNames),
                "Please delete provider relation mentioned in cause field.");
    }

    public static NSError getProviderNotFoundError(DataProvider provider) {
        return new NSError("Incorrect input data",
                HttpStatus.SC_NOT_FOUND,
                "E502123",
                String.format("Data provider '%s' not found", provider.getName()),
                "Create request with valid data provider");
    }

    public static NSError getProviderDataManipulationError(DataProvider provider) {
        return new NSError("Data manipulation failed",
                HttpStatus.SC_BAD_REQUEST,
                "E502118",
                "Couldn't save data provider: " + provider.getName(),
                "Provide correct data");
    }

    public static NSError getProviderInternalFuelError() {
        return new NSError("Request processing exception",
                HttpStatus.SC_INTERNAL_SERVER_ERROR,
                "E9000001",
                "Data provider 'daimler' response: Invalid response was retrieved from High Mobility. No fuel information is available.",
                "Modify request according to data provider requirements");
    }

    public static NSError getDaimlerResourceNotFoundError() {
        return new NSError("Request processing exception",
                HttpStatus.SC_NOT_FOUND,
                "E501114",
                "Data provider 'daimler' response: Resource not found.",
                "Modify request according to data provider requirements");
    }

    public static NSError getCMInvalidVehicleError(String consentId) {
        return new NSError("Consent token not found",
                HttpStatus.SC_NOT_FOUND,
                "E501126",
                sbb("Token is not provided for consent request id").w().sQuoted(consentId).bld(),
                "Clarify with Consent Management team if requested vehicle id has consent");
    }

    public static NSError getCMNoConsentIdProvided() {
        return new NSError("Consent token not found",
                HttpStatus.SC_BAD_REQUEST,
                "E501126",
                "Consent parameter 'consentRequestId' must be provided in request header 'ConsentRequestId'",
                "Clarify with Consent Management team if requested vehicle id has consent");
    }

    public static NSError getCMNoConsentIdProvided(String consentRequestId) {
        return new NSError("Consent token not found",
                HttpStatus.SC_NOT_FOUND,
                "E501126",
                sbb("Token is not provided for consent request id ").sQuoted(consentRequestId).bld(),
                "Clarify with Consent Management team if requested vehicle id has consent");
    }

    public static NSError getProviderResourceNotFoundError(String providerName,
            String resourceName) {
        return new NSError("Provider resource not found",
                HttpStatus.SC_NOT_FOUND,
                "E502106",
                String.format("Resource '%s' for provider '%s' not found", resourceName,
                        providerName),
                "Use existing provider resource name");
    }

    public static NSError getCantDeleteContainerWithSubs(Container container) {
        return new NSError("Incorrect input data",
                HttpStatus.SC_CONFLICT,
                "E502109",
                sbb("You can't delete container").w().sQuoted(container.getName()).w()
                        .append("for provider").w()
                        .sQuoted(container.getDataProviderName()).w().append(", because it has subscriptions").bld(),
                "Delete subscriptions and try again.");
    }

    public static NSError getCantEditContainerWithSubs(Container container) {
        return new NSError("Incorrect input data",
                HttpStatus.SC_CONFLICT,
                "E502109",
                sbb("You can't update container").w().sQuoted(container.getName()).w()
                        .append("for provider").w().sQuoted(container.getDataProviderName()).w()
                        .append(" because it has subscriptions").bld(),
                "Delete subscriptions and try again.");
    }

    public static NSError getCantDeleteResourceWithSubs(ProviderResource resource, DataProvider provider) {
        return new NSError("Incorrect input data",
                HttpStatus.SC_CONFLICT,
                "E502113",
                String.format(
                        "You can't delete resource '%s' for provider '%s', because it has subscriptions",
                        resource.getName(), provider.getName()),
                "Delete resource relations mentioned above and try again");
    }

    public static NSError getInvalidRequestMethod(String methodName) {
        return new NSError("Invalid request method",
                HttpStatus.SC_METHOD_NOT_ALLOWED,
                "E502101",
                "Request method '" + methodName + "' not supported",
                "Use NS OpenAPI specification methods only");
    }

    public static NSError getTokenForConsentNotFoundError(String consentId) {
        return new NSError("Consent token not found",
                HttpStatus.SC_NOT_FOUND,
                "E501126",
                String.format("Token is not provided for consent request id '%s'",
                        consentId),
                "Clarify with Consent Management team if requested vehicle id has consent");
    }

    public static NSError getTokenForConsentRevokeError(String consentId) {
        return new NSError("Consent token not found",
                HttpStatus.SC_UNAUTHORIZED,
                "E501126",
                String.format("Token is not provided for consent request id '%s'",
                        consentId),
                "Clarify with Consent Management team if requested vehicle id has consent");
    }

    public static NSError getResourceIsMissingInProviderError(ProviderResource res,
            DataProvider provider) {
        return new NSError("Provider does not have specified resource",
                HttpStatus.SC_BAD_REQUEST,
                "E502107",
                String.format("Resource '%s' is missing in provider '%s'", res.getName(),
                        provider.getName()),
                "Please use resources which belongs to specified provider");
    }

    public static NSError getResourceCantBeDeletedError(ProviderResource res, Container container) {
        return new NSError("Incorrect input data",
                HttpStatus.SC_CONFLICT,
                "E502113",
                String.format(
                        "You can't delete resource '%s' because it is used in following containers: '%s'",
                        res.getName(), container.getId()),
                "Delete resource relations mentioned above and try again");
    }

}
