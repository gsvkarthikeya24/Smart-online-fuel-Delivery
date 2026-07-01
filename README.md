# Smart Online Fuel Delivery

A comprehensive Spring Boot web application designed for smart online fuel delivery and demand prediction.

## Features
- **User Roles & Authorization**: Role-based access control for Admins, Customers, Providers, and Delivery Agents.
- **Demand Prediction**: Built-in logic to predict fuel demands.
- **Inventory & Orders Management**: Track fuel inventory, place orders, and manage prices.
- **PDF Invoices**: Automatic generation of order invoices using OpenPDF.
- **Secure Authentication**: Uses Spring Security and custom success handlers for different user dashboards.

## Tech Stack
- **Backend Framework**: Java 17, Spring Boot 3.2.x
- **Database**: H2 (Development) / MySQL (Production) via Spring Data JPA
- **Security**: Spring Security
- **Frontend**: Thymeleaf templates, HTML, CSS, JS
- **Utilities**: Lombok, OpenPDF

## Setup Instructions

### Prerequisites
- **Java 17** (or Java 21) installed (Note: Java 22+ may face compatibility issues with Lombok)
- **Maven** installed

### Running the Application Locally
1. Clone the repository:
   ```bash
   git clone https://github.com/gsvkarthikeya24/Smart-online-fuel-Delivery.git
   ```
2. Navigate to the project directory.
3. Build the project using Maven:
   ```bash
   mvn clean install
   ```
4. Run the application:
   ```bash
   mvn spring-boot:run
   ```
5. Access the application at `http://localhost:8080`.

### Database Configuration
By default, the application runs using the `mysql` profile (`application.properties`). 
To switch to the H2 file-based database for quick testing, change `spring.profiles.active` to `h2` in `src/main/resources/application.properties`.

## License
This project is open-source and available under the [MIT License](LICENSE).
