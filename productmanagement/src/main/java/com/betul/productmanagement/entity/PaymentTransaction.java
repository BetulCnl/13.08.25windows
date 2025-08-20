package com.betul.productmanagement.entity;
//bu PaymentTransaction entitysi PaymentStatus'ı kullanacak ve Order'a olan bağlantıyı içerecek.


import com.betul.productmanagement.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private Instant createdAt;

    private String paymentGatewayReference;

    @ManyToOne(fetch = FetchType.LAZY) //hangi siparişin (sipariş id si ile) ödemesi olduğunu anlar burdan
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
}
