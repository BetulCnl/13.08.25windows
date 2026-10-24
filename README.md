# E-Ticaret Projesi (ara sürüm, 13.08.2025)

[mini-eticaret-sitesi](https://github.com/BetulCnl/mini-eticaret-sitesi) projesinin 13 Ağustos 2025 tarihli ara sürümü. Güncel hali o repoda; bu repo geçmiş kaydı olarak duruyor.

## Bu sürümde olanlar

İki Spring Boot servisi:

| Servis | Port | Görevi |
|---|---|---|
| `productmanagement` | 8080 | kullanıcı, ürün, kategori, sepet, sipariş |
| `paymentmanagement` | 8081 | ödeme simülasyonu |

- JWT ile giriş, USER / ADMIN rolleri
- Ürün ve kategori yönetimi, sepet, sipariş oluşturma
- Ödeme servisine OpenFeign ile istek; ödeme kayıtları `/api/payment-transactions` altında listelenebiliyor
- `static/` altında düz HTML sayfalar: `login.html`, `products.html`, `cart.html`

## Güncel sürümden farkları

- Veritabanı PostgreSQL değil, dosya tabanlı H2 (`./pmdb`). Ayrı bir veritabanı kurmaya gerek yok.
- Docker dosyaları yok.
- Sipariş ödeme sayfası, "Siparişlerim" sayfası ve iade akışı yok.

## Çalıştırma

Java 21 gerekiyor.

```bash
cd paymentmanagement
./mvnw spring-boot:run

# başka bir terminalde
cd productmanagement
./mvnw spring-boot:run
```

- Arayüz: `http://localhost:8080/login.html`
- H2 konsolu: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./pmdb`, kullanıcı `sa`, şifre boş)
