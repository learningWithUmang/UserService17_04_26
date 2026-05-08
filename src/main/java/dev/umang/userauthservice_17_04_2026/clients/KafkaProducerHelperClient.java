package dev.umang.userauthservice_17_04_2026.clients;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducerHelperClient {

    @Autowired
    private KafkaTemplate<String , String> kafkaTemplate;

    public void sendMessage(String topic, String message) {
        // Logic to send message to Kafka topic

        kafkaTemplate.send(topic, message);
        System.out.println("Message sent to Kafka topic " + topic + ": " + message);
    }
}
