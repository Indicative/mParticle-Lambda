package com.indicative.partners.mparticle;

import com.google.common.collect.Lists;
import com.mparticle.sdk.MessageProcessor;
import com.mparticle.sdk.model.Message;
import com.mparticle.sdk.model.MessageSerializer;
import com.mparticle.sdk.model.audienceprocessing.AudienceMembershipChangeRequest;
import com.mparticle.sdk.model.audienceprocessing.AudienceMembershipChangeResponse;
import com.mparticle.sdk.model.audienceprocessing.AudienceSubscriptionRequest;
import com.mparticle.sdk.model.audienceprocessing.AudienceSubscriptionResponse;
import com.mparticle.sdk.model.eventprocessing.*;
import com.mparticle.sdk.model.registration.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by prajjwol on 3/23/16.
 */
@Slf4j
public class MparticleMessageProcessor extends MessageProcessor {
    private static final String NAME = "Indicative";
    private static final String VERSION = "1.0";
    private static final String DESCRIPTION = "<a href=\"https://www.indicative.com\" target=\"_blank\">" +
            "Indicative</a> Simple, Powerful Business Intelligence & Analytics for Marketing, Product, " +
            "and Business Teams\"";
    private static final String SETTINGS_API_KEY = "apiKey";
    private static final String INDICATIVE_INPUT_URL = "https://api.indicative.com/service/mparticle/";

    private final MessageSerializer serializer = new MessageSerializer();


    @Override
    public ModuleRegistrationResponse processRegistrationRequest(ModuleRegistrationRequest moduleRegistrationRequest) {
        ModuleRegistrationResponse response = new ModuleRegistrationResponse(NAME, VERSION);

        Setting apiKey = new TextSetting(SETTINGS_API_KEY, "API Key")
                .setIsRequired(true)
                .setIsConfidential(true);

        Permissions permissions = new Permissions();
        permissions.setUserIdentities(
                Arrays.asList(
                        new UserIdentityPermission(UserIdentity.Type.EMAIL, Identity.Encoding.RAW),
                        new UserIdentityPermission(UserIdentity.Type.CUSTOMER, Identity.Encoding.RAW, true)
                )
        );
        permissions.setAllowAccessIpAddress(true);
        permissions.setAllowAccessIpAddress(true);

        List<Setting> eventSettings = Lists.newArrayList();
        eventSettings.add(apiKey);
        EventProcessingRegistration eventProcessingRegistration = new EventProcessingRegistration()
                .setSupportedEventTypes(Lists.newArrayList(Event.Type.values()))
                .setAccountSettings(eventSettings);

        List<Setting> audienceSettings = Lists.newArrayList();
        audienceSettings.add(apiKey);
        AudienceProcessingRegistration audienceProcessingRegistration = new AudienceProcessingRegistration();
        audienceProcessingRegistration.setAccountSettings(audienceSettings);

        response.setDescription(DESCRIPTION)
                .setPermissions(permissions)
                .setEventProcessingRegistration(eventProcessingRegistration)
                .setAudienceProcessingRegistration(audienceProcessingRegistration);

        return response;
    }

    @Override
    public EventProcessingResponse processEventProcessingRequest(EventProcessingRequest request) throws IOException {
        EventProcessingResponse response = new EventProcessingResponse();
        postMessage(request.getAccount(), serializer.serialize(request));
        return response;
    }

    @Override
    public AudienceMembershipChangeResponse processAudienceMembershipChangeRequest(AudienceMembershipChangeRequest request) throws IOException {
        postMessage(request.getAccount(), serializer.serialize(request));
        return super.processAudienceMembershipChangeRequest(request);
    }

    @Override
    public AudienceSubscriptionResponse processAudienceSubscriptionRequest(AudienceSubscriptionRequest request) throws IOException {
        postMessage(request.getAccount(), serializer.serialize(request));
        return super.processAudienceSubscriptionRequest(request);
    }

    private void postMessage(Account account, String messageText) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(INDICATIVE_INPUT_URL + account.getStringSetting(SETTINGS_API_KEY, true, null));
        request.setEntity(new StringEntity(messageText, ContentType.APPLICATION_JSON));
        HttpResponse response = client.execute(request);

        if(response == null || response.getStatusLine().getStatusCode() != 200){
            if(response != null){
                log.debug("Status code of the request is {}", response.getStatusLine().getStatusCode());
            }
        }
    }
}
