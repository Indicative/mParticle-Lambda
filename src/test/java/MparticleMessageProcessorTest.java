import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.indicative.partners.mparticle.MparticleLambdaEndpoint;
import com.indicative.partners.mparticle.MparticleMessageProcessor;
import com.mparticle.sdk.model.MessageSerializer;
import com.mparticle.sdk.model.audienceprocessing.AudienceMembershipChangeRequest;
import com.mparticle.sdk.model.audienceprocessing.AudienceSubscriptionRequest;
import com.mparticle.sdk.model.audienceprocessing.UserProfile;
import com.mparticle.sdk.model.eventprocessing.EventProcessingRequest;
import com.mparticle.sdk.model.registration.ModuleRegistrationRequest;
import com.mparticle.sdk.model.registration.ModuleRegistrationResponse;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import com.yammer.dropwizard.testing.JsonHelpers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Created by prajjwol on 3/25/16.
 */
@Slf4j
public class MparticleMessageProcessorTest {
    private final static String MODULE_REGISTRATION = "ModuleRegistration";
    private final static String AUDIENCE_MEMBERSHIP = "AudienceMembershipChange";
    private final static String EVENT_PROCESSING = "EventProcessing";
    private final static String AUDIENCE_SUBSCRIPTION = "AudienceSubscription";
    private MparticleMessageProcessor mparticleMessageProcessor;
    private MparticleLambdaEndpoint endpoint;

    @Before
    public void setUp() throws Exception{
        mparticleMessageProcessor = Mockito.mock(MparticleMessageProcessor.class);
        endpoint = new MparticleLambdaEndpoint();
        endpoint.setProcessor(mparticleMessageProcessor);
    }

    private String loadMparticleFixture(String name) throws IOException {
        return JsonHelpers.jsonFixture("fixtures/" + name + "Request.json");
    }

    @Test
    public void testProcessEventProcessing() throws IOException{
        InputStream inputStream = IOUtils.toInputStream(loadMparticleFixture(EVENT_PROCESSING));
        OutputStream outputStream = new ByteOutputStream();
        Context context = new TestContext();
        endpoint.handleRequest(inputStream, outputStream, context);
        verify(mparticleMessageProcessor, times(1)).processEventProcessingRequest(any(EventProcessingRequest.class));
    }

    @Test
    public void testProcessAudienceSubscription() throws IOException{
        InputStream inputStream = IOUtils.toInputStream(loadMparticleFixture(AUDIENCE_SUBSCRIPTION));
        OutputStream outputStream = new ByteOutputStream();
        Context context = new TestContext();
        endpoint.handleRequest(inputStream, outputStream, context);
        verify(mparticleMessageProcessor, times(1)).processAudienceSubscriptionRequest(any(AudienceSubscriptionRequest.class));
    }
    @Test
    public void testProcessAudienceMembershipChange() throws IOException{
        InputStream inputStream = IOUtils.toInputStream(loadMparticleFixture(AUDIENCE_MEMBERSHIP));
        OutputStream outputStream = new ByteOutputStream();
        Context context = new TestContext();
        endpoint.handleRequest(inputStream, outputStream, context);
        verify(mparticleMessageProcessor, times(1)).processAudienceMembershipChangeRequest(any(AudienceMembershipChangeRequest.class));
    }
    @Test
    public void testProcessModuleRegistration() throws IOException{
        InputStream inputStream = IOUtils.toInputStream(loadMparticleFixture(MODULE_REGISTRATION));
        OutputStream outputStream = new ByteOutputStream();
        Context context = new TestContext();
        endpoint.handleRequest(inputStream, outputStream, context);
        verify(mparticleMessageProcessor, times(1)).processRegistrationRequest(any(ModuleRegistrationRequest.class));
    }

    @Test
    public void testProcessEventProcessingRequest() throws Exception{
        EventProcessingRequest request = new EventProcessingRequest();
        mparticleMessageProcessor.processMessage(request);
        verify(mparticleMessageProcessor, times(1)).processEventProcessingRequest(request);
        verifyNoMoreInteractions(mparticleMessageProcessor);
    }

    @Test
    public void testAudienceMembershipChangeRequest() throws Exception{
        AudienceMembershipChangeRequest request = new AudienceMembershipChangeRequest();
        List<UserProfile> userProfiles = Lists.newArrayList();
        request.setUserProfiles(userProfiles);
        mparticleMessageProcessor.processMessage(request);
        verify(mparticleMessageProcessor, times(1)).processAudienceMembershipChangeRequest(request);
        verifyNoMoreInteractions(mparticleMessageProcessor);
    }

    @Test
    public void testAudienceSubscriptionRequest() throws Exception{
        AudienceSubscriptionRequest request = new AudienceSubscriptionRequest();
        mparticleMessageProcessor.processMessage(request);
        verify(mparticleMessageProcessor, times(1)).processAudienceSubscriptionRequest(request);
        verifyNoMoreInteractions(mparticleMessageProcessor);
    }

    @Test
    // Should not invoke processAudienceMembershipChangeRequest when user profiles is null
    public void testNoInteractionAudienceMembership() throws Exception{
        AudienceMembershipChangeRequest request = new AudienceMembershipChangeRequest();
        mparticleMessageProcessor.processMessage(request);
        verifyNoMoreInteractions(mparticleMessageProcessor);
    }

    @Test
    public void getModuleRegistrationJSON() throws Exception{
        MessageSerializer serializer = new MessageSerializer();
        MparticleMessageProcessor processor = new MparticleMessageProcessor();
        ModuleRegistrationResponse response = processor.processRegistrationRequest(new ModuleRegistrationRequest());
        System.out.println();
        System.out.println("JSON:");
        System.out.println();
        System.out.println(serializer.serialize(response));
        System.out.println();
    }
}
