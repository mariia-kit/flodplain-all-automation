package com.here.platform.cm.enums;

import static com.here.platform.common.strings.SBB.sbb;

import com.github.javafaker.Crypto;
import com.github.javafaker.Faker;
import com.here.platform.cm.rest.model.AdditionalLink;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentRequest;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.common.config.Conf;
import com.here.platform.ns.dto.User;


public class Consents {
    protected static Faker faker = new Faker();
    protected static Crypto crypto = faker.crypto();

    public static ConsentInfo generateNewConsentInfo(User consumer, ConsentRequestContainer container) {
        return generateNewConsentInfo(consumer.getRealm(), container)
                .consumerName(consumer.getName());
    }

    public static ConsentInfo generateNewConsentInfo(String consumerId, ConsentRequestContainer container) {
        return new ConsentInfo()
                .consumerId(consumerId)
                .consumerName(sbb(consumerId).w().append("name").bld())
                .containerId(container.getId())
                .containerName(container.getName())
                .containerDescription(container.getContainerDescription())
                .resources(container.getResources())

                .title(Conf.cm().getQaTestDataMarker() + faker.gameOfThrones().quote())
                .purpose(faker.commerce().productName())
                .privacyPolicy(faker.internet().url())
                .addAdditionalLinksItem(new AdditionalLink().title(faker.commerce().department()).url(faker.internet().url()))

                .createTime(null)
                .revokeTime(null)
                .expiredTime(null);
    }

    public static ConsentRequestData generateNewConsent(String providerId, ConsentInfo consentInfo) {
        return new ConsentRequestData()
                .consumerId(consentInfo.getConsumerId())
                .providerId(providerId)
                .title(consentInfo.getTitle())
                .purpose(consentInfo.getPurpose())
                .privacyPolicy(consentInfo.getPrivacyPolicy())
                .additionalLinks(consentInfo.getAdditionalLinks())
                .containerId(consentInfo.getContainerId());
    }

    public static ConsentRequestData generateNewConsent(String consumerId, String providerId, String containerId) {
        return new ConsentRequestData()
                .consumerId(consumerId)
                .providerId(providerId)
                .title(Conf.cm().getQaTestDataMarker() + faker.gameOfThrones().quote())
                .purpose(faker.commerce().productName())
                .privacyPolicy(faker.internet().url())
                .addAdditionalLinksItem(
                        new AdditionalLink().title(faker.commerce().department()).url(faker.internet().url()))
                .containerId(containerId);
    }

    public static ConsentRequest generateResponse(ConsentRequestData testConsentRequest, String crid) {
        return new ConsentRequest()
                .consentRequestId(crid)
                .additionalLinks(testConsentRequest.getAdditionalLinks())
                .privacyPolicy(testConsentRequest.getPrivacyPolicy() == null ? "/" : testConsentRequest.getPrivacyPolicy())
                .consumerId(testConsentRequest.getConsumerId())
                .providerId(testConsentRequest.getProviderId())
                .containerId(testConsentRequest.getContainerId())
                .purpose(testConsentRequest.getPurpose())
                .title(testConsentRequest.getTitle());
    }

    public static ConsentRequest generateResponse(String providerId, ConsentInfo consentInfo) {
        return new ConsentRequest()
                .consentRequestId(consentInfo.getConsentRequestId())
                .additionalLinks(consentInfo.getAdditionalLinks())
                .privacyPolicy(consentInfo.getPrivacyPolicy() == null ? "/" : consentInfo.getPrivacyPolicy())
                .consumerId(consentInfo.getConsumerId())
                .providerId(providerId)
                .containerId(consentInfo.getContainerId())
                .purpose(consentInfo.getPurpose())
                .title(consentInfo.getTitle());
    }

}
