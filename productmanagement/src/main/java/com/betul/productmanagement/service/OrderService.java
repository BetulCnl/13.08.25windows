package com.betul.productmanagement.service;

import com.betul.productmanagement.client.PaymentServiceClient;
import com.betul.productmanagement.dto.OrderDTO;
import com.betul.productmanagement.dto.PaymentRequestDTO;
import com.betul.productmanagement.dto.PaymentResponseDTO;
import com.betul.productmanagement.entity.*;
import com.betul.productmanagement.enums.OrderStatus;
import com.betul.productmanagement.enums.PaymentStatus;
import com.betul.productmanagement.enums.PaymentType;
import com.betul.productmanagement.Mapper.OrderMapper;
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

    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PaymentServiceClient paymentServiceClient; // Feign Client'ı ekledik

    @Transactional
    public OrderDTO checkout(User user) {
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // 1. Siparişi PENDING durumuyla oluştur
        Order order = new Order();
        order.setUser(user);
        order.setCreatedAt(Instant.now());
        order.setOrderStatus(OrderStatus.PENDING);

        BigDecimal totalAmount = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);

        // Sipariş kalemlerini (OrderItem) oluştur ve stokları düş
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (cartItem.getQuantity() > product.getStock()) {
                throw new RuntimeException("Bu ürün için yetersiz stok: " + product.getName());
            }
            product.setStock(product.getStock() - cartItem.getQuantity());
            try {
                productRepository.save(product);
            } catch (OptimisticLockingFailureException e) {
                throw new RuntimeException("Stok güncelleme hatası, lütfen tekrar deneyin");
            }
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            order.getItems().add(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        // Sepeti burada TEMİZLEMİYORUZ! Ödeme başarılı olunca temizlenecek.
        return OrderMapper.toOrderDTO(savedOrder);
    }

    private void revertStock(Order order) {
        for (OrderItem item : order.getItems()) {
            productRepository.findById(item.getProductId()).ifPresent(product -> {
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            });
        }
    }

    // ... (Diğer metotlar değişmedi)

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

    @Transactional
    public OrderDTO completeOrder(Long orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new SecurityException("User is not authorized to complete this order");
        }

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be completed.");
        }

        // Ödeme servisine BAŞARILI isteği gönder
        PaymentRequestDTO paymentRequest = PaymentRequestDTO.builder()
                .orderId(order.getId())
                .amount(order.getTotalAmount())
                .paymentType(PaymentType.PURCHASE)
                .paymentStatus(PaymentStatus.SUCCESS) // Bu siparişin başarılı olmasını istiyoruz
                .build();

        try {
            PaymentResponseDTO paymentResponse = paymentServiceClient.createPayment(paymentRequest);
            if (paymentResponse.getStatus() == PaymentStatus.SUCCESS) {
                order.setOrderStatus(OrderStatus.COMPLETED);
                cartItemRepository.deleteByUser(user); // Sepeti temizle
            } else {
                // Ödeme servisi FAILED dönerse (beklenmedik durum), siparişi FAILED yap ve stoğu iade et
                order.setOrderStatus(OrderStatus.FAILED);
                revertStock(order);
            }
        } catch (Exception e) {
            // Ödeme servisine ulaşılamazsa, siparişi şimdilik PENDING bırakabilir veya FAILED yapabiliriz.
            // Kullanıcının tekrar deneyebilmesi için PENDING bırakmak daha iyi olabilir.
            // Ancak bu durumda stokların durumu kritik hale gelir.
            // Şimdilik FAILED varsayalım ve stoğu iade edelim.
            order.setOrderStatus(OrderStatus.FAILED);
            revertStock(order);
            throw new RuntimeException("Payment service is currently unavailable. Please try again later.", e);
        }

        Order savedOrder = orderRepository.save(order);
        return OrderMapper.toOrderDTO(savedOrder);
    }

    @Transactional
    public OrderDTO failOrder(Long orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new SecurityException("User is not authorized to fail this order");
        }

        if (order.getOrderStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be marked as failed.");
        }

        // Ödeme servisine BAŞARISIZ isteği gönder
        PaymentRequestDTO paymentRequest = PaymentRequestDTO.builder()
                .orderId(order.getId())
                .amount(order.getTotalAmount())
                .paymentType(PaymentType.PURCHASE) // veya CANCEL, duruma göre
                .paymentStatus(PaymentStatus.FAILED) // Bu siparişin başarısız olmasını istiyoruz
                .build();
        
        try {
            paymentServiceClient.createPayment(paymentRequest);
        } catch (Exception e) {
            // Ödeme servisine ulaşılamasa bile siparişi FAILED olarak işaretleyip devam edebiliriz.
            // Çünkü zaten amacımız bu.
            System.err.println("Could not contact payment service to mark as FAILED, but proceeding anyway: " + e.getMessage());
        }

        order.setOrderStatus(OrderStatus.FAILED);
        revertStock(order); // Stokları iade et

        Order savedOrder = orderRepository.save(order);
        return OrderMapper.toOrderDTO(savedOrder);
    }
}
