package com.betul.productmanagement.dto;

import com.betul.productmanagement.enums.PaymentStatus;
import lombok.Data;

@Data
public class PaymentResponseDTO {
    private Long paymentTransactionId;
    private PaymentStatus status;
    private String message;
}
