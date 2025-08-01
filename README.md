# 💳 PayApp – Digital Wallet Application  

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0-green?logo=springboot)  
![Angular](https://img.shields.io/badge/Angular-16-red?logo=angular)  
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql)  
![License](https://img.shields.io/badge/License-MIT-yellow)  
![Build](https://img.shields.io/badge/Build-Passing-brightgreen)

PayApp is a **full‑stack digital wallet and e‑commerce payment solution** built with **Spring Boot (backend)** and **Angular (frontend)**.  
It enables **secure payments, wallet top‑ups, transaction history, and user authentication** for seamless online transactions.

---

## 🚀 Features  
✅ **User Registration & Login (JWT Auth)**  
✅ **Wallet Top‑up & Balance Management**  
✅ **Send & Receive Money**  
✅ **Transaction History Tracking**  
✅ **Secure REST APIs for Payments**  
✅ **Responsive UI with Angular & Bootstrap**

---

## 🛠 Tech Stack  

| Technology          | Purpose                        |
|---------------------|--------------------------------|
| **Java (Spring Boot)** | Backend REST API             |
| **Angular 16**      | Frontend UI                   |
| **MySQL**           | Database for storage          |
| **Spring Security + JWT** | Authentication & Authorization |
| **Maven**           | Build & Dependency Management |

---

## 📂 Project Structure  

```
PayApp/
 ├── backend/
 │   ├── src/main/java/com/payapp/  # Spring Boot backend
 │   ├── controller/                # REST APIs
 │   ├── entity/                    # JPA Entities
 │   ├── repository/                # Database Repositories
 │   ├── service/                   # Business Logic
 │   └── PayAppApplication.java
 ├── frontend/
 │   ├── src/app/                   # Angular Components & Services
 │   └── angular.json
 └── pom.xml
```

---

## ⚙️ Setup & Installation  

### 1️⃣ Clone the Repository  
```bash
git clone https://github.com/CodeByRubenInamdar/PayApp.git
cd PayApp
```

### 2️⃣ Configure MySQL Database (Backend)  
Create a MySQL database named `payapp`  
Update `application.properties` inside backend folder:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/payapp
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
spring.jpa.hibernate.ddl-auto=update
```

### 3️⃣ Build & Run Backend  
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### 4️⃣ Run Frontend  
```bash
cd frontend
npm install
ng serve
```

---

## 📌 API Endpoints  

| Endpoint                  | Method | Description             |
|---------------------------|--------|-------------------------|
| `/auth/register`          | POST   | Register new user       |
| `/auth/login`             | POST   | User login              |
| `/wallet/balance`         | GET    | Get wallet balance      |
| `/wallet/topup`           | POST   | Add money to wallet     |
| `/wallet/transfer`        | POST   | Send money to user      |
| `/transactions`           | GET    | View transaction history|

---

## 📸 Screenshots (Optional)  
_Add frontend screenshots here if available._

---

## 🤝 Contributing  
1. Fork the repository  
2. Create a new branch (`feature-xyz`)  
3. Commit your changes  
4. Push to your branch and create a PR  

---

## 📬 Contact  

👤 **Ruben Inamdar**  
📧 Email: rubeninamdar86@gmail.com  
🔗 [LinkedIn](https://www.linkedin.com/in/ruben-inamdar)  
💻 [GitHub](https://github.com/CodeByRubenInamdar)
