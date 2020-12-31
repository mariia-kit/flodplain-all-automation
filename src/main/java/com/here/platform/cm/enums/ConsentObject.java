package com.here.platform.cm.enums;

import com.github.javafaker.Crypto;
import com.github.javafaker.Faker;
import com.here.platform.cm.rest.model.AdditionalLink;
import com.here.platform.cm.rest.model.ConsentInfo;
import com.here.platform.cm.rest.model.ConsentRequest;
import com.here.platform.cm.rest.model.ConsentRequestData;
import com.here.platform.common.DataSubject;
import com.here.platform.common.config.Conf;
import com.here.platform.common.strings.VIN;
import com.here.platform.ns.dto.Container;
import com.here.platform.ns.dto.User;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;


@Getter
@Setter
public class ConsentObject {
    protected static Faker faker = new Faker();

    private ConsentRequestData consentRequestData;
    private List<ConsentInfo> consents;
    private DataSubject dataSubject;
    private User consumer;
    private MPProviders provider;
    private ConsentRequestContainer container;
    private String crid;

    public ConsentInfo getConsent() {
        return consents.get(0);
    }

    public ConsentInfo getConsent(String vinNumber) {
        String hash = new VIN(vinNumber).hashed();
        return consents.stream().filter(cons -> cons.getVinHash().equals(hash)).findFirst().orElse(null);
    }

    public void setCrid(String crid) {
        this.crid = crid;
        consents.forEach(cons -> cons.setConsentRequestId(crid));
    }

    public void addVin(String vin) {
        ConsentInfo consentInfo = new ConsentInfo()
                .consentRequestId(crid)
                .consumerId(consumer.getRealm())
                .consumerName(consumer.getName())
                .containerId(container.getId())
                .containerName(container.getName())
                .containerDescription(container.getContainerDescription())
                .resources(container.getResources())

                .title(consentRequestData.getTitle())
                .purpose(consentRequestData.getPurpose())
                .privacyPolicy(consentRequestData.getPrivacyPolicy())
                .additionalLinks(consentRequestData.getAdditionalLinks())

                .vinHash(new VIN(vin).hashed())
                .vinLabel(new VIN(vin).label())

                .createTime(null)
                .revokeTime(null)
                .expiredTime(null);
        this.consents.add(consentInfo);
    }

    public ConsentObject(User consumer, MPProviders provider, ConsentRequestContainer container) {
        this.consumer = consumer;
        this.provider = provider;
        this.container = container;
        consentRequestData = new ConsentRequestData()
                .consumerId(consumer.getRealm())
                .providerId(provider.getName())
                .title(Conf.cm().getQaTestDataMarker() + faker.gameOfThrones().quote())
                .purpose(faker.commerce().productName())
                .privacyPolicy(faker.internet().url())
                .addAdditionalLinksItem(
                        new AdditionalLink().title(faker.commerce().department()).url(faker.internet().url()))
                .containerId(container.getId());
        this.consents = new ArrayList<>();
        crid = StringUtils.EMPTY;
    }

    public ConsentRequest generateResponseForCreation() {
        return new ConsentRequest()
                .consentRequestId(crid)
                .additionalLinks(getConsentRequestData().getAdditionalLinks())
                .privacyPolicy(getConsentRequestData().getPrivacyPolicy() == null ? "/" : getConsentRequestData().getPrivacyPolicy())
                .consumerId(getConsentRequestData().getConsumerId())
                .providerId(getConsentRequestData().getProviderId())
                .containerId(getConsentRequestData().getContainerId())
                .purpose(getConsentRequestData().getPurpose())
                .title(getConsentRequestData().getTitle());
    }

    public Container getNSContainer() {
        return new Container(
                container.getId(),
                container.getName(),
                container.getProvider().getName(),
                container.getContainerDescription(),
                String.join(",", container.getResources()),
                true,
                container.getScopeValue());
    }
}
