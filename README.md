# TRINITY (Backend)

Bu, Trinity Password Manager'ın arka uç (backend) deposudur. Güvenli kimlik doğrulama ve şifreleme mekanizmaları ile kullanıcı şifrelerini depolamak ve yönetmek için tasarlanmış güvenli bir uygulamadır. Backend, kullanıcı kimlik doğrulama, şifre yönetimi ve yönetici görevlerini gerçekleştiren RESTful API'ler sağlar. Şu anda geliştirme aşamasındadır; en son değişiklikler yerel ortamda çalışmakta fakat henüz bu depoya tamamen yansıtılmamıştır.

---

## 🚀 Özellikler

- **Kullanıcı Kimlik Doğrulama**: JWT (JSON Web Tokens) ve yenileme tokenları ile kayıt, giriş ve token tabanlı kimlik doğrulama.
- **Şifre Yönetimi**: Şifrelerin güvenli şekilde AES ile şifrelenerek depolanması. Parola hashleme için BCrypt.
- **Yönetici Paneli**: Kullanıcı etkinlikleri, parola istatistikleri ve kategori dağılımı.
- **Güvenli Parola Sıfırlama**: E-posta üzerinden doğrulama kodu ile şifre sıfırlama.
- **Kategori Yönetimi**: Yöneticiler için şifre kategorilerini oluşturma, güncelleme ve silme işlemleri.
- **Denetim Günlüğü (Audit Logging)**: Kullanıcı oluşturma, şifre güncelleme gibi önemli işlemlerin izlenmesi.
- **CORS Desteği**: Frontend için `http://localhost:5173` adresinden güvenli iletişim yapılandırması.

---

## 🛠️ Kullanılan Teknolojiler

- **Java 17**
- **Spring Boot 3.2.4**
- **Spring Security**
- **PostgreSQL**
- **JPA / Hibernate**
- **BCrypt & AES**
- **JavaMailSender**
- **Lombok**
- **Maven**
- **SLF4J**

---

## 📁 Proje Yapısı

TRINITY/
├── src/main/java/com/project/Trinity/
│ ├── Config/ # Güvenlik, parola şifreleme, global hata yönetimi
│ ├── Controller/ # Auth, kullanıcı, şifre, kategori ve admin API endpoint'leri
│ ├── DTO/ # API veri taşıma nesneleri
│ ├── Entity/ # JPA varlıkları (User, Password, Category, RefreshToken, vs.)
│ ├── Filter/ # JWT authentication/authorization filtreleri
│ ├── Repository/ # Veritabanı işlemleri için JPA repository'leri
│ ├── Service/ # İş mantığı (user, password, category, email services)
│ ├── Util/ # JWT ve AES şifreleme yardımcı sınıfları
├── src/main/resources/
│ └── application.properties # DB, JWT, şifreleme, email ayarları
├── pom.xml # Maven yapılandırması

## ⚙️ Kurulum Talimatları

### 1. Ön Gereksinimler

- Java 17
- Maven 3.9.9
- PostgreSQL (yerel veya uzak)
- SMTP sunucu bilgileri (örneğin Gmail)

### 2. Depoyu Klonla


git clone https://github.com/BeyzaNurKeskinn/TRINITY.git
cd TRINITY

3. Ortamı Yapılandır
PostgreSQL’de trinity isimli bir veritabanı oluştur.

src/main/resources/application.properties dosyasını şu şekilde düzenle:

properties
Kopyala
Düzenle
spring.datasource.url=jdbc:postgresql://localhost:5432/trinity
spring.datasource.username=your_username
spring.datasource.password=your_password

jwt.secret=your_base64_encoded_jwt_secret
encryption.secret-key=your_base64_encoded_aes_key

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email
spring.mail.password=your_app_password
4. Gizli Anahtarlar Üret (İsteğe Bağlı)
JWT Anahtarı (32 byte):
powershell
Kopyala
Düzenle
$RandomBytes = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$Bytes = [byte[]]::new(32)
$RandomBytes.GetBytes($Bytes)
[Convert]::ToBase64String($Bytes)
AES Anahtarı (16 byte):
powershell
Kopyala
Düzenle
$RandomBytes = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$Bytes = [byte[]]::new(16)
$RandomBytes.GetBytes($Bytes)
[Convert]::ToBase64String($Bytes)
▶️ Uygulamayı Başlat
bash
Kopyala
Düzenle
mvn clean install
mvn spring-boot:run
Uygulama http://localhost:8080 adresinde çalışacaktır.
🔍 API Testleri
API'leri test etmek için Postman veya cURL gibi araçları kullanabilirsiniz:

POST /api/auth/register: Yeni kullanıcı kaydı

POST /api/auth/login: Giriş yap, token al

GET /api/user/passwords: Kullanıcı şifrelerini getir (auth gerekli)

GET /api/admin/dashboard: Admin paneli verileri (ADMIN rolü gerekli)

📌 Geliştirme Durumu
Uygulama yerel ortamda çalışır durumda.

Şifreleme, JWT, kategori yönetimi tamamlandı.

Yeni özellikler (parola sıfırlama, audit log) henüz GitHub'a yansıtılmadı.

🔮 Gelecek Geliştirmeler
Yerel değişikliklerin GitHub'a aktarılması

Birim ve entegrasyon testleri eklenmesi

Rate limiting ve iki faktörlü kimlik doğrulama

Daha gelişmiş hata yönetimi ve loglama

Veritabanı sorgularının optimizasyonu

🤝 Katkıda Bulunma
Proje aktif geliştirme altındadır. Katkılar memnuniyetle karşılanır. Forklayın, yeni bir branch oluşturun ve pull request gönderin.

📄 Lisans
Apache License, Version 2.0
License
Licensed under the Apache License, Version 2.0. See the LICENSE file for details.
