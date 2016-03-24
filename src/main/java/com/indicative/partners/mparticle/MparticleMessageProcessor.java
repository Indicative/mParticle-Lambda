package com.indicative.partners.mparticle;

import com.mparticle.sdk.MessageProcessor;
import com.mparticle.sdk.model.registration.ModuleRegistrationRequest;
import com.mparticle.sdk.model.registration.ModuleRegistrationResponse;

/**
 * Created by prajjwol on 3/23/16.
 */
public class MparticleMessageProcessor extends MessageProcessor {
    @Override
    public ModuleRegistrationResponse processRegistrationRequest(ModuleRegistrationRequest moduleRegistrationRequest) {
        return null;
    }

    public void processCustomEvent(){

    }
}
