
# SalesApp

**SalesApp** is a comprehensive desktop application designed to manage sales data, track customers, employees, items, and their corresponding interactions, all while providing an intuitive user experience. Built using **Spring Boot** for backend services and **JavaFX** for the frontend UI, it integrates with a **MySQL database** for data persistence.

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Technologies Used](#technologies-used)
3. [Features](#features)
4. [Entities](#entities)
5. [Getting Started](#getting-started)
6. [Installation](#installation)
7. [Configuration](#configuration)
8. [Running the Application](#running-the-application)
9. [Project Structure](#project-structure)
10. [CRUD Operations](#crud-operations)
11. [Screenshots](#screenshots)
12. [License](#license)

---

## Project Overview

SalesApp is designed to streamline the management of sales processes, from tracking customer orders to managing employee details and items. The application uses a **Spring Boot** backend to handle the business logic, with **JavaFX** as the front-end UI to provide a seamless user experience.

The app integrates with a **MySQL database** to persist data for various entities such as **Person**, **Employee**, and **Item**, enabling full CRUD operations, filtering, and pagination for each entity.

---

## Technologies Used

- **Java 21**: The application is developed using Java 21, providing the latest features and performance improvements.
- **Spring Boot 3.3.5**: Used for creating the backend services and dependency injection.
- **JavaFX 23.0.1**: Provides the user interface for the desktop application.
- **MySQL**: Stores application data such as sales records, customer information, etc.
- **Hibernate 6.2.8.Final**: ORM framework for database interaction.
- **Maven**: Dependency management and build tool.
- **Scene Builder**: For designing the front-end UI in a drag-and-drop interface.

---

## Features

- **CRUD Operations**: The app supports Create, Read, Update, and Delete operations for the following entities:
  - Person (Customer)
  - Employee
  - Item
- **Sales Tracking**: View and manage sales records.
- **Database Integration**: Utilizes MySQL for storing and retrieving application data.
- **Pagination and Filtering**: Efficient data management with pagination and filtering options for large datasets.
- **JavaFX UI**: A responsive, intuitive user interface built with JavaFX and FXML.

---

## Entities

The application includes the following entities:

### Person
Represents a customer or individual associated with sales. The `Person` entity contains the following fields:
- `ID`: Unique identifier for the person.
- `Name`: Full name of the person.
- `Email`: Email address of the person.
- `Phone Number`: Contact number for the person.

### Employee
Represents an employee of the company. The `Employee` entity contains:
- `ID`: Unique identifier for the employee.
- `Name`: Full name of the employee.
- `Position`: Job title or position of the employee.
- `Salary`: Employee's salary.

### Item
Represents an item sold. The `Item` entity includes:
- `ID`: Unique identifier for the item.
- `Name`: Name of the item.
- `Price`: Price of the item.

---

## Getting Started

Follow the steps below to get a local copy of the project up and running.

### Prerequisites

Before you begin, ensure you have the following installed:

- **Java 21** or higher
- **Maven**: For building and managing project dependencies
- **MySQL**: For database management
- **Scene Builder** (Optional): For designing the JavaFX UI

---

## Installation

1. Clone the repository to your local machine:
   ```bash
   git clone https://github.com/MahmoodWaheed/SalesManagementSystem
   ```

2. Navigate to the project directory:
    ```bash
    cd salesapp
    ```

3. Build the project using Maven:
    ```bash
    mvn clean install
    ```

4. Set up your MySQL database with the following credentials:
    - Database Name: `salesdb`
    - Username: `root`
    - Password: `Ma7moud%Wa7eed&*123`

    Create the `salesdb` database if it doesn't already exist.

---

## Configuration

In your `application.properties` (located in `src/main/resources`), configure your database connection:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/salesdb
spring.datasource.username=root
spring.datasource.password=Ma7moud%Wa7eed&*123
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

---

## Running the Application

To run the application, execute the following command:

```bash
mvn spring-boot:run
```

Alternatively, you can run the JavaFX application by executing:

```bash
mvn javafx:run
```

---

## Project Structure

Here’s a breakdown of the project directory structure:

```
salesapp
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com/
│   │   │   │   ├── mahmoud/
│   │   │   │   │   ├── sales/
│   │   │   │   │   │   ├── controller/           # Controllers for managing UI actions
│   │   │   │   │   │   ├── entity/               # JPA Entities (Person, Employee, Item)
│   │   │   │   │   │   ├── repository/           # Repositories for database access
│   │   │   │   │   │   ├── service/              # Service layer for business logic
│   │   │   │   │   │   └── JavaFxApplication.java # Main class to launch JavaFX with Spring Boot
│   │   ├── resources/
│   │   │   ├── application.properties             # Spring Boot configuration
│   │   │   └── static/                            # Static files (CSS, JS, etc.)
│   │   │   └── fxml/                             # FXML files for JavaFX views
│   ├── test/                                      # Unit and integration tests
└── pom.xml                                         # Maven build file
```

---

## CRUD Operations

The following operations can be performed for each of the entities (Person, Employee, Item):

- **Create**: Add a new record to the database.
- **Read**: View existing records with support for filtering and pagination.
- **Update**: Modify existing records.
- **Delete**: Remove records from the database.

---

## Screenshots

_Screenshots of the application can be added here to showcase the UI and functionality._

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
