package com.example.marketplace.service;

import com.example.marketplace.model.FraudReport;
import com.example.marketplace.repository.FraudReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FraudService {
    @Autowired
    private FraudReportRepository repository;

    public FraudReport report(FraudReport report, String reporterId) {
        report.setReporterId(reporterId);
        report.setStatus("PENDING");
        report.setCreatedAt(LocalDateTime.now());
        return repository.save(report);
    }

    public List<FraudReport> getReports() {
        return repository.findAll();
    }
}
