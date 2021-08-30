package com.here.platform.proxy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class Authentication {

    String authMethod;
    String authMethodPlaceholder;
    String apiKeyParamName;
    String apiKeyValue;

    public String getAuthMethod() {
        return authMethod;
    }

    public String getAuthMethodPlaceholder() {
        return authMethodPlaceholder;
    }

    public String getApiKeyParamName() {
        return apiKeyParamName;
    }

    public String getApiKeyValue() {
        return apiKeyValue;
    }

    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }

    public void setAuthMethodPlaceholder(String authMethodPlaceholder) {
        this.authMethodPlaceholder = authMethodPlaceholder;
    }

    public void setApiKeyParamName(String apiKeyParamName) {
        this.apiKeyParamName = apiKeyParamName;
    }

    public void setApiKeyValue(String apiKeyValue) {
        this.apiKeyValue = apiKeyValue;
    }

    public Authentication() {
        this.authMethod = authMethod;
        this.authMethodPlaceholder = authMethodPlaceholder;
        this.apiKeyParamName = apiKeyParamName;
        this.apiKeyValue = apiKeyValue;
    }

    public Authentication withAuth() {
      setAuthMethod("API_KEY");
      setAuthMethodPlaceholder("IN_QUERY");
      setApiKeyParamName("apiKey");
      setApiKeyValue("56746746");
      return this;
    }

    public Authentication withNullAuthMethod() {
        setAuthMethod(null);
        setAuthMethodPlaceholder("IN_QUERY");
        setApiKeyParamName("apiKey");
        setApiKeyValue("56746746");
        return this;
    }

    @Override
    public String toString() {
        return "Authentication [authMethod=" + authMethod + ", authMethodPlaceholder=" + authMethodPlaceholder + ", apiKeyParamName=" + apiKeyParamName + ", apiKeyValue=" + apiKeyValue + "]";
    }

}

