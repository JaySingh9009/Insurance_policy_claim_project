package com.insurance.demo.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.insurance.demo.dto.ClaimHistoryResponse;
import com.insurance.demo.repository.ClaimStatusHistoryRepository;
import com.insurance.demo.service.ClaimHistoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClaimHistoryServiceImpl
        implements ClaimHistoryService {

    private final ClaimStatusHistoryRepository
            historyRepository;

    @Override
    public List<ClaimHistoryResponse>
    getClaimHistory(Long claimId) {

        return historyRepository
                .findByClaimClaimId(claimId)
                .stream()
                .map(history ->
                        new ClaimHistoryResponse(
                                history
                                .getPreviousStatus()
                                .name(),

                                history
                                .getNewStatus()
                                .name(),

                                history.getRemarks(),

                                history.getUpdatedBy() != null
                                ? history.getUpdatedBy()
                                         .getFullName()
                                : "SYSTEM",

                                history
                                .getUpdatedDate()
                                .toString()
                        ))
                .toList();
    }
}