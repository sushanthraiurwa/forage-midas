package com.jpmc.midascore.service;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionProcessor {

    private static final Logger logger = LoggerFactory.getLogger(TransactionProcessor.class);

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public TransactionProcessor(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void process(Transaction transaction) {
        UserRecord sender = userRepository.findById(transaction.getSenderId()).orElse(null);
        UserRecord recipient = userRepository.findById(transaction.getRecipientId()).orElse(null);

        if (sender == null || recipient == null) {
            logger.warn("❌ Invalid Transaction - Sender or Recipient not found: {}", transaction);
            return;
        }

        float amount = transaction.getAmount();
        if (sender.getBalance() < amount) {
            logger.warn("💸 Skipped Transaction - Insufficient Balance for Sender [{}]", sender.getName());
            return;
        }

        // ✅ Adjust balances
        sender.setBalance(sender.getBalance() - amount);
        recipient.setBalance(recipient.getBalance() + amount);

        // ✅ Save updates
        userRepository.save(sender);
        userRepository.save(recipient);

        // ✅ Record transaction
        TransactionRecord record = new TransactionRecord(sender, recipient, amount);
        transactionRepository.save(record);

        logger.info("✅ Transaction Recorded: {} -> {} | Amount: {}", sender.getName(), recipient.getName(), amount);
    }
}
