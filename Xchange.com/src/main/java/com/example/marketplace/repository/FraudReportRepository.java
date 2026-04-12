package com.example.marketplace.repository;

import com.example.marketplace.model.FraudReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface FraudReportRepository extends MongoRepository<FraudReport, String> {
    List<FraudReport> findByStatus(String status);

    List<FraudReport> findByVehicleId(String vehicleId);
}
