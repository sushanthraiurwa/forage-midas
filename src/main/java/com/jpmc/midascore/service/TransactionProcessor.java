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

import java.util.Optional;

@Service
public class TransactionProcessor {

    private static final Logger logger = LoggerFactory.getLogger(TransactionProcessor.class);

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final IncentiveService incentiveService;

    public TransactionProcessor(UserRepository userRepository,
                                TransactionRepository transactionRepository,
                                IncentiveService incentiveService) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.incentiveService = incentiveService;
    }

    @Transactional
    public void process(Transaction transaction) {
        Optional<UserRecord> senderOpt = userRepository.findById(transaction.getSenderId());
        Optional<UserRecord> recipientOpt = userRepository.findById(transaction.getRecipientId());

        if (senderOpt.isEmpty() || recipientOpt.isEmpty()) {
            logger.warn("⚠️ Invalid sender or recipient ID — skipping transaction: {}", transaction);
            return;
        }

        UserRecord sender = senderOpt.get();
        UserRecord recipient = recipientOpt.get();
        float amount = transaction.getAmount();

        if (sender.getBalance() >= amount) {
            // ✅ Get incentive from REST API
            float incentive = incentiveService.fetchIncentive(transaction);

            // ✅ Update balances
            sender.setBalance(sender.getBalance() - amount);
            recipient.setBalance(recipient.getBalance() + amount + incentive);

            userRepository.save(sender);
            userRepository.save(recipient);

            // ✅ Save record with incentive included
            TransactionRecord record = new TransactionRecord(sender, recipient, amount, incentive);
            transactionRepository.save(record);

            logger.info("✅ Transaction Recorded: {} -> {} | Amount: {} | Incentive: {}",
                    sender.getName(), recipient.getName(), amount, incentive);
        } else {
            logger.warn("💸 Skipped Transaction - Insufficient Balance for Sender [{}]", sender.getName());
        }
    }
}
