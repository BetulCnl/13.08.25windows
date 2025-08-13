package com.betul.productmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //many orderitem to one order
    @ManyToOne(optional = false) //optional = false= her orderitem illaki bir ordera bsğlı olmak zorunda
    @JoinColumn(name = "order_id") //bunu order_items tableında sütun yaptık
    private Order order;

    private String productName;

    private BigDecimal unitPrice; //ürünün birim fiyatı

    private int quantity; // o üründen kaç tane sipariş edilmiş
} 