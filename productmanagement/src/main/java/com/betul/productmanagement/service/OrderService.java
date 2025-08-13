package com.betul.productmanagement.service;

import com.betul.productmanagement.entity.*;
import com.betul.productmanagement.repository.CartItemRepository;
import com.betul.productmanagement.repository.OrderRepository;
import com.betul.productmanagement.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartItemRepository cartItemRepository; //kullanıcı sepetindeki ürünleri almak ve silmek için
    private final OrderRepository orderRepository; //siparişleri db ye kaydetmek listelemek getirmek
    private final ProductRepository productRepository; //ürün stoklarını güncellemek için

    @Transactional
    public Order checkout(User user) {
        List<CartItem> items = cartItemRepository.findByUser(user);
        if (items.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Validate stock
        items.forEach(item -> {
            if (item.getQuantity() > item.getProduct().getStock()) {
                throw new RuntimeException("Insufficient stock for product: " + item.getProduct().getName());
            }
        });

        Order order = new Order();
        order.setUser(user);
        order.setCreatedAt(Instant.now());
        order.setTotalAmount(BigDecimal.ZERO);

        BigDecimal total = BigDecimal.ZERO;

        // Decrease stock and build order items
        for (CartItem item : items) {
            Product product = item.getProduct();
            product.setStock(product.getStock() - item.getQuantity());
            try {
                productRepository.save(product);
            } catch (OptimisticLockingFailureException e) {
                throw new RuntimeException("Concurrent stock update, please retry");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductName(product.getName());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setQuantity(item.getQuantity());
            order.getItems().add(orderItem);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);

        // Clear cart
        cartItemRepository.deleteByUser(user);

        return saved;
    }

    public Page<Order> listUserOrders(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findByUser(user, pageable);
        
    }

    public Page<Order> listAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findAll(pageable);
    }

    public Order getOrder(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
    }
} 