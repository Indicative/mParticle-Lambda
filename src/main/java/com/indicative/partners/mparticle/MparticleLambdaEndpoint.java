package com.indicative.partners.mparticle;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.mparticle.sdk.model.Message;
import com.mparticle.sdk.model.MessageSerializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by prajjwol on 3/23/16.
 */
@Slf4j
public class MparticleLambdaEndpoint implements RequestStreamHandler {
    private final MessageSerializer serializer = new MessageSerializer();

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        MparticleMessageProcessor processor = new MparticleMessageProcessor();
        Message request = serializer.deserialize(inputStream, Message.class);
        Message response = processor.processMessage(request);
        serializer.serialize(outputStream, response);
    }
}
