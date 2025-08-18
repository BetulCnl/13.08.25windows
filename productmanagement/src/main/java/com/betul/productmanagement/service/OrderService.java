package com.betul.productmanagement.service;

import com.betul.productmanagement.Mapper.OrderMapper;
import com.betul.productmanagement.dto.OrderDTO;
import com.betul.productmanagement.entity.*;
import com.betul.productmanagement.repository.CartItemRepository;
import com.betul.productmanagement.repository.OrderRepository;
import com.betul.productmanagement.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartItemRepository cartItemRepository; //kullanıcı sepetindeki ürünleri almak ve silmek için
    private final OrderRepository orderRepository; //siparişleri db ye kaydetmek listelemek getirmek
    private final ProductRepository productRepository; //ürün stoklarını güncellemek için

    @Transactional
    public OrderDTO checkout(User user) {
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Create and save the Order first to get an ID
        Order order = new Order();
        order.setUser(user);
        order.setCreatedAt(Instant.now());
        order.setOrderStatus(Order.OrderStatus.PENDING); // Set initial status
        order.setTotalAmount(BigDecimal.ZERO); // Will be calculated

        // Pre-calculate total to avoid saving order twice
        BigDecimal totalAmount = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        // Create OrderItems from CartItems
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            // Stock validation
            if (cartItem.getQuantity() > product.getStock()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            // Decrease stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            try {
                productRepository.save(product);
            } catch (OptimisticLockingFailureException e) {
                throw new RuntimeException("Concurrent stock update, please retry");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProductId(product.getId()); // Set the product ID
            orderItem.setProductName(product.getName()); // Snapshotting name
            orderItem.setUnitPrice(product.getPrice()); // Snapshotting price
            orderItem.setQuantity(cartItem.getQuantity());
            savedOrder.getItems().add(orderItem);
        }

        // Save the order again with the items
        Order finalOrder = orderRepository.save(savedOrder);

        // Clear cart
        cartItemRepository.deleteByUser(user);

        return OrderMapper.toOrderDTO(finalOrder);
    }

    public Page<OrderDTO> listUserOrders(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage = orderRepository.findByUser(user, pageable);
        List<OrderDTO> dtoList = orderPage.getContent().stream()
                .map(OrderMapper::toOrderDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, orderPage.getTotalElements());
    }

    public Page<OrderDTO> listAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage = orderRepository.findAll(pageable);
        List<OrderDTO> dtoList = orderPage.getContent().stream()
                .map(OrderMapper::toOrderDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtoList, pageable, orderPage.getTotalElements());
    }

    public OrderDTO getOrder(Long id) {
        return orderRepository.findById(id)
                .map(OrderMapper::toOrderDTO)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
}