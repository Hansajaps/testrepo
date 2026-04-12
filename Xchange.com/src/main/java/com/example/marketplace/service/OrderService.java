package com.example.marketplace.service;

import com.example.marketplace.model.Order;
import com.example.marketplace.model.Product;
import com.example.marketplace.model.Vehicle;
import com.example.marketplace.model.User;
import com.example.marketplace.model.Notification;
import com.example.marketplace.model.Review;
import com.example.marketplace.repository.OrderRepository;
import com.example.marketplace.repository.ProductRepository;
import com.example.marketplace.repository.UserRepository;
import com.example.marketplace.repository.NotificationRepository;
import com.example.marketplace.repository.VehicleRepository;
import com.example.marketplace.repository.ReviewRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    public Order placeSingleOrder(String email, String productId, int quantity, String shippingAddress,
            String buyerName, String buyerPhone) {
        System.out.println("Placing single order for buyer: " + email + ", product or vehicle: " + productId);

        // Try Product
        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            // Handle missing sellerId for older products
            if (product.getSellerId() == null && product.getShopId() != null) {
                userRepository.findByShopId(product.getShopId()).ifPresent(u -> {
                    product.setSellerId(u.getEmail());
                    productRepository.save(product);
                });
            }

            // Handle missing stockQuantity for older products
            if (product.getStockQuantity() == null) {
                product.setStockQuantity(100);
                productRepository.save(product);
            }

            if (product.getStockQuantity() < quantity) {
                throw new RuntimeException("Insufficient stock. Available: " + product.getStockQuantity());
            }

            if (product.getPrice() == null) {
                product.setPrice(BigDecimal.ZERO);
            }

            BigDecimal totalPrice = product.getPrice().multiply(new BigDecimal(quantity));

            Order order = Order.builder()
                    .buyerId(email)
                    .buyerName(buyerName)
                    .buyerPhone(buyerPhone)
                    .sellerId(product.getSellerId())
                    .items(Collections.singletonList(Order.OrderItem.builder()
                            .productId(productId)
                            .name(product.getName())
                            .price(product.getPrice())
                            .quantity(quantity)
                            .build()))
                    .productName(product.getName())
                    .productImage(product.getImages() != null && !product.getImages().isEmpty() ? product.getImages().get(0) : null)
                    .totalPrice(totalPrice)
                    .shippingAddress(shippingAddress)
                    .status("PENDING")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            // Deduct stock
            product.setStockQuantity(product.getStockQuantity() - quantity);
            if (product.getStockQuantity() == 0) {
                product.setStatus("SOLD");
            }
            productRepository.save(product);

            order.initTracking();
            Order saved = orderRepository.save(order);
            notifySellers(saved);
            return saved;
        }

        // Try Vehicle
        Vehicle vehicle = vehicleRepository.findById(productId).orElse(null);
        if (vehicle != null) {
            BigDecimal totalPrice = vehicle.getPrice() != null ? vehicle.getPrice() : BigDecimal.ZERO;

            Order order = Order.builder()
                    .buyerId(email)
                    .buyerName(buyerName)
                    .buyerPhone(buyerPhone)
                    .sellerId(vehicle.getSellerId())
                    .items(Collections.singletonList(Order.OrderItem.builder()
                            .productId(productId)
                            .name(vehicle.getTitle())
                            .price(totalPrice)
                            .quantity(1)
                            .build()))
                    .productName(vehicle.getTitle())
                    .productImage(vehicle.getImages() != null && !vehicle.getImages().isEmpty() ? vehicle.getImages().get(0) : null)
                    .totalPrice(totalPrice)
                    .shippingAddress(shippingAddress)
                    .status("PENDING")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            // Mark vehicle as sold
            vehicle.setStatus("SOLD");
            vehicleRepository.save(vehicle);

            order.initTracking();
            Order saved = orderRepository.save(order);
            notifySellers(saved);
            return saved;
        }

        throw new RuntimeException("Product or Vehicle not found: " + productId);
    }

    private void notifySellers(Order order) {
        if (order.getSellerId() == null)
            return;
        notificationRepository.save(Notification.builder()
                .recipientId(order.getSellerId())
                .senderId(order.getBuyerId())
                .title("New Order Received")
                .message("You have received a new order for " + order.getProductName())
                .type("NEW_ORDER")
                .relatedId(order.getId())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build());
    }

    public Order acceptOrder(String orderId, String sellerEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getSellerId().equals(sellerEmail)) {
            throw new RuntimeException("Unauthorized");
        }

        order.setStatus("ACCEPTED");
        order.setUpdatedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        // Notify Buyer
        notificationRepository.save(Notification.builder()
                .recipientId(order.getBuyerId())
                .senderId(sellerEmail)
                .title("Order Accepted")
                .message("Your order for " + order.getItems().get(0).getName() + " has been accepted by the seller.")
                .type("ORDER_ACCEPTED")
                .relatedId(order.getId())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build());

        return savedOrder;
    }

    public Order declineOrder(String orderId, String sellerEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getSellerId().equals(sellerEmail)) {
            throw new RuntimeException("Unauthorized");
        }

        order.setStatus("DECLINED");
        order.setUpdatedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        // Notify Buyer
        notificationRepository.save(Notification.builder()
                .recipientId(order.getBuyerId())
                .senderId(sellerEmail)
                .title("Order Declined")
                .message("Your order for " + order.getItems().get(0).getName() + " has been declined by the seller.")
                .type("ORDER_DECLINED")
                .relatedId(order.getId())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build());

        return savedOrder;
    }

    public List<Order> getOrdersForSeller(String sellerEmail) {
        if (sellerEmail == null) {
            System.err.println("Attempted to fetch orders for null sellerEmail");
            return Collections.emptyList();
        }
        System.out.println("Fetching orders for seller: " + sellerEmail);
        try {
            List<Order> orders = orderRepository.findBySellerIdOrderByCreatedAtDesc(sellerEmail);
            System.out.println("Found " + (orders != null ? orders.size() : 0) + " orders for " + sellerEmail);
            return orders != null ? orders : Collections.emptyList();
        } catch (Exception e) {
            System.err.println("Error fetching orders for seller " + sellerEmail + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch seller orders", e);
        }
    }

    public Order placeOrderFromWishlist(String email, String shippingAddress) {
        System.out.println("Placing wishlist order for buyer: " + email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Set<String> wishlist = user.getWishlist();
        if (wishlist == null || wishlist.isEmpty()) {
            throw new RuntimeException("Wishlist is empty");
        }

        List<Order.OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        String firstSellerId = null;
        String firstProductImage = null;

        for (String itemId : wishlist) {
            // Check Product
            Product product = productRepository.findById(itemId).orElse(null);
            if (product != null) {
                // Repair older products
                if (product.getSellerId() == null && product.getShopId() != null) {
                    userRepository.findByShopId(product.getShopId()).ifPresent(u -> {
                        product.setSellerId(u.getEmail());
                        productRepository.save(product);
                    });
                }
                if (product.getStockQuantity() == null) {
                    product.setStockQuantity(100);
                    productRepository.save(product);
                }

                if (product.getStockQuantity() >= 1) {
                    orderItems.add(Order.OrderItem.builder()
                            .productId(itemId)
                            .name(product.getName())
                            .price(product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO)
                            .quantity(1)
                            .build());
                    totalPrice = totalPrice.add(product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO);
                    if (firstSellerId == null)
                        firstSellerId = product.getSellerId();
                    if (firstProductImage == null)
                        firstProductImage = product.getImages() != null && !product.getImages().isEmpty() ? product.getImages().get(0) : null;

                    // Deduct stock
                    product.setStockQuantity(product.getStockQuantity() - 1);
                    if (product.getStockQuantity() == 0)
                        product.setStatus("SOLD");
                    productRepository.save(product);
                }
                continue;
            }

            // Check Vehicle
            Vehicle vehicle = vehicleRepository.findById(itemId).orElse(null);
            if (vehicle != null) {
                orderItems.add(Order.OrderItem.builder()
                        .productId(itemId)
                        .name(vehicle.getTitle())
                        .price(vehicle.getPrice() != null ? vehicle.getPrice() : BigDecimal.ZERO)
                        .quantity(1)
                        .build());
                totalPrice = totalPrice.add(vehicle.getPrice() != null ? vehicle.getPrice() : BigDecimal.ZERO);
                if (firstSellerId == null)
                    firstSellerId = vehicle.getSellerId();
                if (firstProductImage == null)
                    firstProductImage = vehicle.getImages() != null && !vehicle.getImages().isEmpty() ? vehicle.getImages().get(0) : null;

                // Mark as SOLD
                vehicle.setStatus("SOLD");
                vehicleRepository.save(vehicle);
            }
        }

        if (orderItems.isEmpty()) {
            throw new RuntimeException("No valid items found in wishlist or items are out of stock");
        }

        Order order = Order.builder()
                .buyerId(email)
                .sellerId(firstSellerId)
                .items(orderItems)
                .productName(orderItems.size() > 1
                        ? orderItems.get(0).getName() + " & " + (orderItems.size() - 1) + " others"
                        : orderItems.get(0).getName())
                .productImage(firstProductImage)
                .totalPrice(totalPrice)
                .shippingAddress(shippingAddress)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        order.initTracking();

        // Clear wishlist
        user.getWishlist().clear();
        userRepository.save(user);

        Order saved = orderRepository.save(order);
        notifySellers(saved);
        return saved;
    }

    public Order submitReview(String orderId, String buyerEmail, Integer rating, String review) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getBuyerId().equals(buyerEmail)) {
            throw new RuntimeException("Only the buyer can review this order");
        }

        String productId = order.getItems().get(0).getProductId();

        // Prevent duplicate reviews — update existing if it exists
        java.util.Optional<Review> existing = reviewRepository.findByOrderId(orderId);
        if (existing.isPresent()) {
            // Delegate to edit path
            return editReview(orderId, buyerEmail, rating, review);
        }

        order.setRating(rating);
        order.setReview(review);
        order.setUpdatedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        // Save to the Review collection
        Review reviewDoc = Review.builder()
                .orderId(orderId)
                .productId(productId)
                .productName(order.getProductName())
                .buyerId(buyerEmail)
                .buyerName(order.getBuyerName())
                .rating(rating)
                .comment(review)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        reviewRepository.save(reviewDoc);

        // Recalculate product rating from scratch
        recalculateProductRating(productId);

        return savedOrder;
    }

    public Order editReview(String orderId, String buyerEmail, Integer rating, String comment) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getBuyerId().equals(buyerEmail)) {
            throw new RuntimeException("Unauthorized: only the buyer can edit this review");
        }

        String productId = order.getItems().get(0).getProductId();

        // Update order fields
        order.setRating(rating);
        order.setReview(comment);
        order.setUpdatedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        // Update the Review document
        Review reviewDoc = reviewRepository.findByOrderId(orderId)
                .orElseGet(() -> Review.builder()
                        .orderId(orderId)
                        .productId(productId)
                        .productName(order.getProductName())
                        .buyerId(buyerEmail)
                        .buyerName(order.getBuyerName())
                        .createdAt(LocalDateTime.now())
                        .build());

        reviewDoc.setRating(rating);
        reviewDoc.setComment(comment);
        reviewDoc.setUpdatedAt(LocalDateTime.now());
        reviewRepository.save(reviewDoc);

        // Recalculate product rating from scratch
        recalculateProductRating(productId);

        return savedOrder;
    }

    public void deleteReview(String orderId, String buyerEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getBuyerId().equals(buyerEmail)) {
            throw new RuntimeException("Unauthorized: only the buyer can delete this review");
        }

        String productId = order.getItems().get(0).getProductId();

        // Clear review fields on the order
        order.setRating(null);
        order.setReview(null);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Remove the Review document
        reviewRepository.findByOrderId(orderId).ifPresent(reviewRepository::delete);

        // Recalculate product rating from scratch
        recalculateProductRating(productId);
    }

    private void recalculateProductRating(String productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return;

        java.util.List<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
        int count = reviews.size();
        double average = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        product.setReviewCount(count);
        product.setAverageRating(count > 0 ? Math.round(average * 10.0) / 10.0 : 0.0);
        productRepository.save(product);
    }

    public List<Order> getMyOrders(String email) {
        return orderRepository.findByBuyerIdOrderByCreatedAtDesc(email);
    }
}

