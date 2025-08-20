package com.betul.productmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import com.betul.productmanagement.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;




@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    //@JsonManagedReference *DTO KULLANDIĞIM İÇİN BUNA GEREK YOK SİLDİM
    @JoinColumn(name = "user_id")
    private User user;

    private Instant createdAt;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING) // Enum string olarak DB'ye yazılacak
    private OrderStatus orderStatus;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY) //!!!EAGER yerine LAZY kullanıldı, çünkü order ile ilişkili orderItem'lar çok fazla olabileceğinden ve performans için lazy loading tercih edildi.
    @ToString.Exclude     //<------------- To avoid infinite recursion in toString
    @EqualsAndHashCode.Exclude //<------------- To avoid infinite recursion in equals and hashCode
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL) //burada orphanReomoverlar vardı sildim, neden sildiğini unutursan düşün
    @ToString.Exclude //bu order entitysine ait olan payment transactionları yazdırmamak için
    @EqualsAndHashCode.Exclude //onetomany altında yazdık BİR SİPARİŞİN BİRDEN ÇOK ÖDEME İŞLEMİ OLABİLİYOR DİYE
    private List<PaymentTransaction> transactions = new ArrayList<>();

    //TODO: orderStatus ekle: PENDING, CANCELLED, COMPLETED, FAILED
    //TODO: İhtiyaçlar: purchase sayfası olacak. purchase sayfasında iki buton, biri success diğeri fail ödeme geçmek.
    //TODO: PaymentTransaction entitysi ekle, bu entity siparişin durumunu tutacak. status: Success, Failed, Pending
    //TODO: payment işlemini ayrı bir payment management microserviceinde yapacağız feign client ile.
    //TODO: myOrders sayfası olacak. orda siparişlerin listelenecek.
}