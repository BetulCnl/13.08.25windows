package com.betul.paymentmanagement.controller;

import com.betul.paymentmanagement.dto.PaymentRequestDTO;
import com.betul.paymentmanagement.dto.PaymentResponseDTO;
import com.betul.paymentmanagement.service.PaymentProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentProcessingService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponseDTO> createPayment(@RequestBody PaymentRequestDTO request) {
        PaymentResponseDTO response = paymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }
}

// BU SERVİS
//* http://localhost:PORT/api/v1/payments adresine gönderilen POST isteklerini kabul edebilir.
//   * Gelen istekle bir ödeme sürecini simüle edip sonucu veritabanına kaydedebilir.
//   * İşlem sonucunu PaymentResponseDTO olarak geri dönebilir.