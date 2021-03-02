package com.here.platform.proxy.dto;

import com.here.platform.common.config.Conf;
import com.here.platform.proxy.dto.ProxyProvider.CredentialsAuthMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum ProxyProviders {

    REFERENCE_PROXY(new ProxyProvider(
            "Auto-testing-1",
            Conf.mpUsers().getMpProvider().getRealm(),
            "reference-data-provider.ost.solo-experiments.com",
            CredentialsAuthMethod.NONE)
            .withAuthMethod(CredentialsAuthMethod.API_KEY_IN_QUERY, "Authorization", "1f8647f3-5f86-4b5e-8687-982fd620ef78")),
    ACCUWEATHER(new ProxyProvider(
            "Accuweather", Conf.mpUsers().getMpProvider().getRealm(), "api.accuweather.com",
            CredentialsAuthMethod.NONE)
            .withAuthMethod(CredentialsAuthMethod.API_KEY_IN_QUERY, "apikey", Conf.proxy().getAccuApiKey()));

    private ProxyProvider proxyProvider;
}
