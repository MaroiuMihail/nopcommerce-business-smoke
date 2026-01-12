# nopCommerce QA Automation – Smoke Test Framework

This repository contains a **QA Automation demo framework** built to demonstrate real-world UI test automation on an eCommerce platform.

The project focuses on **clean architecture**, **stable smoke flows**, and **business-critical scenarios**, not on brittle or over-engineered tests.

Target application: **nopCommerce demo store (Dockerized)**

---

## 🧰 Tech Stack
- Java 17
- Selenium WebDriver
- TestNG
- Maven
- Docker & Docker Compose
- GitHub Actions (CI – in progress)

---

## 🏗️ Framework Architecture
- **Page Object Model (POM)**
- `BaseTest` – browser lifecycle & base URL
- `BasePage` – shared waits & common UI actions
- Explicit waits only (no `Thread.sleep`)
- Page methods represent **business actions**, not raw Selenium calls

---

## ✅ Automated Smoke Flows

The framework currently covers the following **stable, business-critical smoke tests**:

### 1. Open Home Page
- Verifies that the store loads successfully

### 2. Search Product & Add to Cart
- Search for a product
- Open Product Details Page
- Add product to shopping cart

### 3. Remove Product from Cart
- Verify product is present in cart
- Remove item
- Verify cart becomes empty

### 4. Guest Checkout (Happy Path – until Confirm Order)
- Add product to cart
- Accept Terms of Service
- Checkout as Guest
- Fill Billing Address
- Select Shipping Method
- Select Payment Method
- Enter Payment Information
- Verify **Confirm Order** step is reached  
  *(Order is intentionally NOT placed)*

---

## ▶️ How to Run Locally

### 1. Start nopCommerce using Docker

```bash
docker compose up -d
```

The application will be available at:
http://localhost:5000

⚠️ On first run, nopCommerce requires installation via the UI (/install).

### 2. Run the test suite

```bash
mvn test
```
Run headless:
```bash
mvn -Dheadless=true test
```

### 🧪 Project Structure

src
 ├─ main/java
 │   ├─ core        (DriverFactory, BasePage)
 │   └─ pages       (Page Objects)
 └─ test/java
     ├─ core        (BaseTest)
     └─ tests
         └─ smoke   (Smoke test suite)

### 🎯 Project Purpose

This project is designed as a portfolio/demo repository to demonstrate:

Clean and maintainable test architecture

Realistic eCommerce automation flows

Stability-focused smoke testing

Professional QA automation practices

It intentionally avoids flaky or over-engineered scenarios in favor of reliability and clarity.

### 🚧 CI Status

A GitHub Actions pipeline is configured.
Automated nopCommerce installation inside CI is currently being finalized.

### 👤 Author

QA Automation Engineer
(Java • Selenium • TestNG)