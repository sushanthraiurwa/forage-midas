package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.service.TransactionProcessor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionListener {

    private final TransactionProcessor processor;

    public TransactionListener(TransactionProcessor processor) {
        this.processor = processor;
    }

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-consumer")
    public void listen(Transaction transaction) {
        processor.process(transaction);
    }
}
