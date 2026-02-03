package com.ecomm.ecomm_auth_service_application.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaClient {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    
    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }
}
