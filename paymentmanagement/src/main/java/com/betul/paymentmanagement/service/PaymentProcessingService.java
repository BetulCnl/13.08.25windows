package com.betul.paymentmanagement.service;

import com.betul.paymentmanagement.dto.PaymentRequestDTO;
import com.betul.paymentmanagement.dto.PaymentResponseDTO;
import com.betul.paymentmanagement.entity.PaymentTransaction;
import com.betul.paymentmanagement.enums.PaymentStatus;
import com.betul.paymentmanagement.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PaymentProcessingService {

    private final PaymentTransactionRepository repository;

    public PaymentResponseDTO processPayment(PaymentRequestDTO request) {
        // 1. Gelen isteğe göre bir transaction nesnesi oluştur ve PENDING olarak kaydet.
        PaymentTransaction tx = PaymentTransaction.builder()
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .status(PaymentStatus.PENDING)
                .createdAt(Instant.now())
                .build();
        repository.save(tx);

        // 2. Ödeme ağ geçidiyle konuşmayı simüle et (şimdilik rastgele başarılı/başarısız yapalım)
        // Gerçek bir projede burada banka veya iyzico gibi bir servise istek atılırdı.
        System.out.println("Processing payment for order: " + request.getOrderId());
        try {
            Thread.sleep(2000); // 2 saniye bekleterek işlemi simüle et
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean paymentSuccess = new Random().nextBoolean();

        // 3. Sonuca göre transaction'ı güncelle
        tx.setStatus(paymentSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        PaymentTransaction savedTx = repository.save(tx);

        // 4. Cevabı oluştur ve dön
        return new PaymentResponseDTO(savedTx.getId(), savedTx.getStatus(), "Payment processed.");
    }
}
