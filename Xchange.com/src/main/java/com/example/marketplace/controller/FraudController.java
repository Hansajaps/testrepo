package com.example.marketplace.controller;

import com.example.marketplace.model.FraudReport;
import com.example.marketplace.service.FraudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fraud")
public class FraudController {
    @Autowired
    private FraudService fraudService;

    @PostMapping("/reports")
    public ResponseEntity<FraudReport> report(@RequestBody FraudReport report, Authentication auth) {
        return ResponseEntity.ok(fraudService.report(report, auth.getName()));
    }

    @GetMapping("/reports")
    public ResponseEntity<List<FraudReport>> getReports() {
        return ResponseEntity.ok(fraudService.getReports());
    }
}
