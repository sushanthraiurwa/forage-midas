package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionListener {

    private static final Logger logger = LoggerFactory.getLogger(TransactionListener.class);
    private int count = 0;

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-consumer")
    public void listen(Transaction transaction) {
        count++;
        logger.info("=================================================");
        logger.info("*** TRANSACTION #{} RECEIVED ***", count);
        logger.info("Sender ID: {}", transaction.getSenderId());
        logger.info("Recipient ID: {}", transaction.getRecipientId());
        logger.info("*** AMOUNT: {} ***", transaction.getAmount());
        logger.info("=================================================");
    }
}