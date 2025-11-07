package com.jpmc.midascore.service;

import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class IncentiveService {
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String INCENTIVE_URL = "http://localhost:8080/incentive";

    public float fetchIncentive(Transaction transaction) {
        Incentive response = restTemplate.postForObject(INCENTIVE_URL, transaction, Incentive.class);
        return response != null ? response.getAmount() : 0;
    }
}
