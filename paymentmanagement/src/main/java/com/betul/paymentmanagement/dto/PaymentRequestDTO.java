package com.betul.paymentmanagement.dto;

// Bu PaymentRequestDTO productmanagement servisinden bir ödeme isteği alırken
//  hangi bilgileri beklediğimizi tanımlayacak.

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequestDTO {
    private Long orderId;
    private BigDecimal amount;
}

// productmanagement servisi, paymentmanagement servisinin sadece ve sadece API "sözleşmesini" bilmelidir.
// Bu sözleşme de bizim oluşturduğumuz DTO'lardır (PaymentRequestDTO,PaymentResponseDTO)
// yani productmanagement servisi paymentmanagement servisinin iç yapısını ya da işte entitysini bilmemelidir.