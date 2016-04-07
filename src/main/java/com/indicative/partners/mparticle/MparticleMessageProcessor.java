package com.indicative.partners.mparticle;

import com.google.common.collect.Lists;
import com.mparticle.sdk.MessageProcessor;
import com.mparticle.sdk.model.MessageSerializer;
import com.mparticle.sdk.model.audienceprocessing.AudienceMembershipChangeRequest;
import com.mparticle.sdk.model.audienceprocessing.AudienceMembershipChangeResponse;
import com.mparticle.sdk.model.audienceprocessing.AudienceSubscriptionRequest;
import com.mparticle.sdk.model.audienceprocessing.AudienceSubscriptionResponse;
import com.mparticle.sdk.model.eventprocessing.*;
import com.mparticle.sdk.model.registration.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.fest.util.Strings;

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
    private static final String DESCRIPTION = "Indicative is a behavioral analytics platform for growth marketers, " +
        "product managers, and data analysts to optimize customer acquisition, conversion, and retention. When you sign " +
        "up with Indicative, you will receive API keys which correspond to all supported platforms on mParticle. " +
        "Register at: <a href=\"https://app.indicative.com/partners/#/mparticle\" target=\"_blank\"> Indicative </a>";
    private static final String SETTINGS_API_KEY = "apiKey";
    private static final String LIST_ID = "listId";
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

        permissions.setDeviceIdentities(Arrays.asList(
                new DeviceIdentityPermission(DeviceIdentity.Type.APPLE_PUSH_NOTIFICATION_TOKEN, Identity.Encoding.RAW),
                new DeviceIdentityPermission(DeviceIdentity.Type.ANDROID_ID, Identity.Encoding.RAW),
                new DeviceIdentityPermission(DeviceIdentity.Type.IOS_VENDOR_ID, Identity.Encoding.RAW),
                new DeviceIdentityPermission(DeviceIdentity.Type.GOOGLE_CLOUD_MESSAGING_TOKEN, Identity.Encoding.RAW)
        ));

        permissions.setAllowAccessIpAddress(true);
        permissions.setAllowAccessLocation(true);

        List<Setting> eventSettings = Lists.newArrayList();
        eventSettings.add(apiKey);
        EventProcessingRegistration eventProcessingRegistration = new EventProcessingRegistration()
                .setSupportedEventTypes(Lists.newArrayList(Event.Type.values()))
                .setAccountSettings(eventSettings);

        List<Setting> audienceSettings = Lists.newArrayList();
        audienceSettings.add(apiKey);
        TextSetting listIdSetting = new TextSetting(LIST_ID, "List ID");
        listIdSetting.setIsRequired(true);
        audienceSettings.add(listIdSetting);
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
        if(account == null) {
            return;
        }

        String apiKey = account.getStringSetting(SETTINGS_API_KEY, true, null);

        if(Strings.isNullOrEmpty(apiKey)) {
            return;
        }

        HttpPost request = new HttpPost(INDICATIVE_INPUT_URL + apiKey);
        request.setEntity(new StringEntity(messageText, ContentType.APPLICATION_JSON));

        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;

        try {
            client = HttpClients.createDefault();
            response = client.execute(request);
            if (response == null || response.getStatusLine().getStatusCode() != 200) {
                if (response != null) {
                    log.debug("Status code of the request is {}", response.getStatusLine().getStatusCode());
                }
            }
        } catch (Exception e){
            log.debug("Exception caught ", e);
        } finally {
            if(response != null) response.close();
            if(client != null) client.close();
        }
    }
}
