# 🏥 Hospital Management System

A comprehensive desktop application for managing hospital operations, including patient appointments, medical records, doctor schedules, and receptionist tasks.

---

## 🚀 Features

### 👨‍⚕️ Patient Features
- 🏥 User registration and login
- 📅 Book/Cancel appointments
- 📋 View medical history and prescriptions
- 👤 Profile management
- 🔍 Search for available doctors

### 👨‍⚕️ Doctor Features
- 📅 View daily appointments
- 💊 Write and save prescriptions
- 📝 Update patient medical records
- 🔄 Reschedule appointments
- 📊 View patient medical history

### 👩‍💼 Receptionist Features
- 👥 Manage patient records
- 🩺 Manage doctor schedules
- 📆 Schedule/Cancel appointments
- 📊 Generate daily reports
- 🔍 Search patient/doctor information

---

## 🧰 Technology Stack

- **Frontend**: Java Swing
- **Backend**: MySQL
- **Database**: Relational Database Management System (RDBMS)
- **Dependencies**:
  - MySQL Connector/J
  - JCalendar Library
  - JGoodies Forms

---

## ⚙️ Installation

### 1. Prerequisites
- Java JDK 8+
- MySQL Server 5.7+
- MySQL Workbench

### 2. Database Setup
```sql
CREATE DATABASE Hospital_Management_System;
USE Hospital_Management_System;

-- Users Table (Common for all roles)
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL, -- Hashed passwords
    role ENUM('patient', 'doctor', 'receptionist') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Patients Table
CREATE TABLE patients (
    patient_id INT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    dob DATE NOT NULL,
    gender ENUM('Male', 'Female', 'Other'),
    contact VARCHAR(20) NOT NULL,
    FOREIGN KEY (patient_id) REFERENCES users(user_id)
);

-- Doctors Table
CREATE TABLE doctors (
    doctor_id INT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    contact VARCHAR(20) NOT NULL,
    availability ENUM('Available', 'On Leave', 'Busy') DEFAULT 'Available',
    FOREIGN KEY (doctor_id) REFERENCES users(user_id)
);

-- Appointments Table
CREATE TABLE appointments (
    appointment_id INT PRIMARY KEY AUTO_INCREMENT,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_date DATETIME NOT NULL,
    status ENUM('Scheduled', 'Completed', 'Cancelled', 'No-Show') DEFAULT 'Scheduled',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id)
);

-- Specializations Table
CREATE TABLE specializations (
    specialization_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) UNIQUE NOT NULL
);

-- Receptionists Table
CREATE TABLE receptionists (
    receptionist_id INT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    contact VARCHAR(20) NOT NULL,
    FOREIGN KEY (receptionist_id) REFERENCES users(user_id)
);

-- Update patients table
ALTER TABLE patients ADD COLUMN (
    email VARCHAR(100) UNIQUE NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    registration_date DATE
);

-- Remove password_hash column (if needed)
ALTER TABLE patients DROP COLUMN password_hash;

-- Insert default receptionist user
INSERT INTO users (username, password, role)
VALUES ('reception', SHA2('hospital123', 256), 'receptionist');

-- Insert receptionist details
INSERT INTO receptionists (receptionist_id, full_name, contact)
VALUES (LAST_INSERT_ID(), 'John Doe', '555-1234');
```

### 3. Configuration
Update your database credentials in `DatabaseManager.java`:
```java
private static final String DB_USER = "your_username";
private static final String DB_PASSWORD = "your_password";
```

### 4. Add Dependencies
- Download **MySQL Connector/J**
- Download **JCalendar**

### 5. Run the Application
```bash
javac -cp .;mysql-connector-java-8.0.23.jar;jcalendar-1.4.jar hospital_management/*.java
java -cp .;mysql-connector-java-8.0.23.jar;jcalendar-1.4.jar hospital_management.ChooseUser
```

---

## 📸 Usage

### Main Interface
- Dashboard for choosing Patient, Doctor, or Receptionist login.

### Patient Dashboard
- Book and manage appointments, view medical history.

### Doctor Dashboard
- View daily appointments, prescribe medications, update records.

### Receptionist Dashboard
- Manage appointments, patient records, and doctor schedules.

---

## 🗂️ Key Components

### Database Design
- **Users** (Patients, Doctors, Receptionists)
- **Appointments**
- **Medical Records**
- **Specializations**

### Core Classes
- `DatabaseManager.java` — Handles all database operations
- `PatientDashboard.java` — Patient interface
- `DoctorDashboard.java` — Doctor interface
- `ReceptionistDashboard.java` — Receptionist interface
- `Appointment.java` — Appointment management
- `Patient.java` — Patient entity model

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

---

## 🙏 Acknowledgments

- Java Swing Community
- MySQL Documentation
- Open Source Healthcare Projects
