package com.betul.productmanagement.service;

import com.betul.productmanagement.dto.PaymentRequestDTO;
import com.betul.productmanagement.dto.PaymentResponseDTO;
import com.betul.productmanagement.entity.PaymentTransaction;
import com.betul.productmanagement.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.betul.productmanagement.enums.PaymentStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class PaymentTransactionService {

    private final PaymentTransactionRepository repository;

    public PaymentResponseDTO processPayment(PaymentRequestDTO request) {
        // 1. Gelen isteğe göre bir transaction nesnesi oluştur ve PENDING olarak kaydet.
        PaymentTransaction tx = PaymentTransaction.builder()
                .orderId(request.getOrderId())
                .status(PaymentStatus.PENDING)
                .createdAt(Instant.now())
                .build();
        repository.save(tx);
        return null; // Placeholder
    }
}
