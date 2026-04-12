package com.example.marketplace.service;

import com.example.marketplace.model.Order;
import com.example.marketplace.model.TrackingStatus;
import com.example.marketplace.model.TrackingStage;
import com.example.marketplace.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderAutomationService {

    private final OrderRepository orderRepository;

    /**
     * Automatically closes orders that have been in DELIVERED status for more than 15 days.
     * Runs every day at midnight.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void autoCloseOldDeliveredOrders() {
        log.info("Starting automatic order closure check...");
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(15);
        
        // Find all orders where trackingStatus is DELIVERED
        List<Order> deliveredOrders = orderRepository.findByTrackingStatus(TrackingStatus.DELIVERED);
        
        int closedCount = 0;
        for (Order order : deliveredOrders) {
            if (order.getUpdatedAt() != null && order.getUpdatedAt().isBefore(cutoffDate)) {
                closeOrder(order);
                closedCount++;
            }
        }
        
        log.info("Automatic closure complete. Closed {} orders.", closedCount);
    }

    private void closeOrder(Order order) {
        try {
            // Update tracking state to CLOSED
            order.setTrackingStatus(TrackingStatus.CLOSED);
            order.setStatus("COMPLETED");
            order.setUpdatedAt(LocalDateTime.now());
            
            // Mark DELIVERED stage as completed (if not already)
            if (order.getTrackingStages() != null) {
                for (TrackingStage stage : order.getTrackingStages()) {
                    if (stage.getStage() == TrackingStatus.DELIVERED) {
                        stage.setCompleted(true);
                        stage.setTimestamp(LocalDateTime.now());
                    }
                }
            }
            
            orderRepository.save(order);
            log.info("Order {} automatically closed after 15 days of delivery.", order.getId());
        } catch (Exception e) {
            log.error("Failed to automatically close order {}: {}", order.getId(), e.getMessage());
        }
    }
}

