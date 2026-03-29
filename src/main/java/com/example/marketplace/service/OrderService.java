package com.example.marketplace.service;

import com.example.marketplace.model.Order;
import com.example.marketplace.model.Product;
import com.example.marketplace.model.User;
import com.example.marketplace.model.Notification;
import com.example.marketplace.model.Vehicle;
import com.example.marketplace.repository.NotificationRepository;
import com.example.marketplace.repository.OrderRepository;
import com.example.marketplace.repository.ProductRepository;
import com.example.marketplace.repository.UserRepository;
import com.example.marketplace.repository.VehicleRepository;
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
                    .totalPrice(totalPrice)
                    .shippingAddress(shippingAddress)
                    .status("PENDING")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            // Mark vehicle as sold
            vehicle.setStatus("SOLD");
            vehicleRepository.save(vehicle);

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
        System.out.println("Fetching orders for seller: " + sellerEmail);
        List<Order> orders = orderRepository.findBySellerIdOrderByCreatedAtDesc(sellerEmail);
        System.out.println("Found " + orders.size() + " orders for " + sellerEmail);
        return orders;
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
                .totalPrice(totalPrice)
                .shippingAddress(shippingAddress)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Clear wishlist
        user.getWishlist().clear();
        userRepository.save(user);

        Order saved = orderRepository.save(order);
        notifySellers(saved);
        return saved;
    }

    public List<Order> getMyOrders(String email) {
        return orderRepository.findByBuyerId(email);
    }
}
