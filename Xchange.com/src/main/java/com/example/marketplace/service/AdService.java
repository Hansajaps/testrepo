package com.example.marketplace.service;

import com.example.marketplace.model.Ad;
import com.example.marketplace.repository.AdRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdService {
    @Autowired
    private AdRepository adRepository;

    public List<Ad> getActiveAds() {
        return adRepository.findByActiveTrue();
    }
}
