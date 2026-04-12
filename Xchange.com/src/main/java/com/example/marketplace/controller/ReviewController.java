package com.example.marketplace.controller;

import com.example.marketplace.model.Review;
import com.example.marketplace.repository.ProductRepository;
import com.example.marketplace.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@SuppressWarnings("null")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getProductReviews(@PathVariable String productId) {
        return ResponseEntity.ok(reviewRepository.findByProductIdOrderByCreatedAtDesc(productId));
    }

    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<Review>> getBuyerReviews(@PathVariable String buyerId) {
        return ResponseEntity.ok(reviewRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId));
    }

    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        return ResponseEntity.ok(reviewRepository.findAll());
    }

    /**
     * Edit a review by its own ID.
     * The authenticated user must be the original buyer.
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<?> editReview(
            @PathVariable String reviewId,
            @RequestBody Map<String, Object> payload,
            Authentication auth) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getBuyerId().equals(auth.getName())) {
            return ResponseEntity.status(403).body("Unauthorized: you can only edit your own reviews");
        }

        Integer rating = payload.get("rating") instanceof Number
                ? ((Number) payload.get("rating")).intValue() : null;
        String comment = (String) payload.get("comment");

        if (rating != null) review.setRating(rating);
        if (comment != null) review.setComment(comment);
        review.setUpdatedAt(LocalDateTime.now());
        Review saved = reviewRepository.save(review);

        // Recalculate product average rating
        recalculateProductRating(review.getProductId());

        return ResponseEntity.ok(saved);
    }

    /**
     * Delete a review by its own ID.
     * The authenticated user must be the original buyer.
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable String reviewId,
            Authentication auth) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getBuyerId().equals(auth.getName())) {
            return ResponseEntity.status(403).body("Unauthorized: you can only delete your own reviews");
        }

        String productId = review.getProductId();
        reviewRepository.delete(review);

        // Recalculate product average rating
        recalculateProductRating(productId);

        return ResponseEntity.noContent().build();
    }

    private void recalculateProductRating(String productId) {
        productRepository.findById(productId).ifPresent(product -> {
            List<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
            int count = reviews.size();
            double average = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            product.setReviewCount(count);
            product.setAverageRating(count > 0 ? Math.round(average * 10.0) / 10.0 : 0.0);
            productRepository.save(product);
        });
    }
}
