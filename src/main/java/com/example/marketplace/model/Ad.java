package com.example.marketplace.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ads")
public class Ad {
    @Id
    private String id;
    private String title;
    private String imageUrl;
    private String link;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private boolean active;
}
