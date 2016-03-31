package com.indicative.partners.mparticle;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.mparticle.sdk.model.Message;
import com.mparticle.sdk.model.MessageSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by prajjwol on 3/23/16.
 */
public class MparticleLambdaEndpoint implements RequestStreamHandler {
    private final MessageSerializer serializer = new MessageSerializer();

    private MparticleMessageProcessor processor;

    public MparticleLambdaEndpoint(){
        this.processor = new MparticleMessageProcessor();
    }

    public MparticleLambdaEndpoint(MparticleMessageProcessor processor){
        this.processor = processor;
    }

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        Message request = serializer.deserialize(inputStream, Message.class);
        Message response = processor.processMessage(request);
        serializer.serialize(outputStream, response);
    }
}
