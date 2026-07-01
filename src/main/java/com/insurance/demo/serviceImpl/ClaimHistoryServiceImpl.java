package com.insurance.demo.serviceImpl;

import com.insurance.demo.dto.ClaimHistoryResponse;
import com.insurance.demo.repository.ClaimStatusHistoryRepository;
import com.insurance.demo.service.ClaimHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClaimHistoryServiceImpl implements ClaimHistoryService {

    private final ClaimStatusHistoryRepository historyRepository;

    @Override
    public List<ClaimHistoryResponse> getClaimHistory(Long claimId) {
        return historyRepository.findByClaimClaimId(claimId)
                .stream()
                .map(h -> ClaimHistoryResponse.builder()
                        .historyId(h.getHistoryId())
                        .previousStatus(h.getPreviousStatus() != null ? h.getPreviousStatus().name() : null)
                        .newStatus(h.getNewStatus().name())
                        .remarks(h.getRemarks())
                        .updatedBy(h.getUpdatedBy() != null ? h.getUpdatedBy().getFullName() : "SYSTEM")
                        .updatedAt(h.getUpdatedAt())
                        .build())
                .toList();
    }
}