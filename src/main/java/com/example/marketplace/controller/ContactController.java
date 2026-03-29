package com.example.marketplace.controller;

import com.example.marketplace.model.ContactMessage;
import com.example.marketplace.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact")
public class ContactController {
    @Autowired
    private ContactService contactService;

    @PostMapping
    public ResponseEntity<ContactMessage> save(@RequestBody ContactMessage message) {
        return ResponseEntity.ok(contactService.saveMessage(message));
    }
}
