package com.betul.productmanagement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {//FeignClient

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //many orders to one users
    @ManyToOne(optional = false)  //her sipariş illaki bir kullanıcıya bağlı olacak. ne hiç ne fazla sadece 1
    @JoinColumn(name = "user_id") //userın idsi column ekledik db ye
    private User user;

    private Instant createdAt; //siparişin oluşturulduğu zamanı belirtir

    private BigDecimal totalAmount; //siperiş toplam tutarı. bigdecial -> büyük mat işlemleri için


    //tek bir siparişte birden fazla item sipariş edebiliriz one to many tek oturum to many sipariş item vermek
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    //TODO: orderStatus ekle: PENDING, CANCELLED, COMPLETED, FAILED
    //TODO: İhtiyaçlar: purchase sayfası olacak. purchase sayfasında iki buton, biri success diğeri fail ödeme geçmek.
    //TODO: myOrders sayfası olacak. orda siparişlerin listelenecek.
    //TODO: PaymentTransaction entitysi ekle, bu entity siparişin durumunu tutacak. status: Success, Failed, Pending
    //TODO: payment işlemini ayrı bir payment management microserviceinde yapacağız feign client ile.
}

//cascade = CascadeType.ALL (order üzerinde yaptığımız değişiklikler bağlantılı olduğu orderitema da yansır
// orphanRemoval = true    Bir parent entity (order) ona bağlı olan child entitym var (OrderItem)
// aralarında onetomany onetoone ilişkisi kullanılır.-------------------
//orphanRemoval = true dediğinde, parent içinden child liste veya referansı kaldırılırsa,
// o child entity otomatik olarak veritabanından silinir.
//cascade parent üzerinden childeda değişiklik olursa bunu yansıtır.
// orphanRemovalsa parent üzerinden dierkt childeın silinmesidir.

//fetch = FetchType.EAGER = parent entity yüklendiğinde childe entity de hemen yüklenir
//.LAZY neden kullanmadık anlamadım, EAGER belleği mahveder gibi
//productla orderitemı karıştırdım, orderitem zaten sseçtiğim şeyler