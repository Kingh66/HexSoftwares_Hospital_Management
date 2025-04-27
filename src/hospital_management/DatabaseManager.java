package hospital_management;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;


public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/Hospital_Management_System";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Sizwe22";
    private static final int MAX_RETRIES = 3;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            verifyDatabaseStructure();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", DB_USER);
        props.setProperty("password", DB_PASSWORD);
        props.setProperty("useSSL", "false");
        props.setProperty("serverTimezone", "UTC");
        return DriverManager.getConnection(DB_URL, props);
    }

    private static void verifyDatabaseStructure() {
        String[] tables = {"users", "patients", "doctors", "appointments", "specializations"};
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            DatabaseMetaData meta = conn.getMetaData();
            
            for (String table : tables) {
                ResultSet rs = meta.getTables(null, null, table, null);
                if (!rs.next()) {
                    createTable(stmt, table);
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    private static void createTable(Statement stmt, String table) {
        try {
            switch (table) {
                case "users":
                    stmt.executeUpdate("CREATE TABLE users (" +
                        "user_id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "username VARCHAR(50) UNIQUE NOT NULL, " +
                        "password VARCHAR(255) NOT NULL, " +
                        "role ENUM('patient','doctor','receptionist') NOT NULL, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
                    break;
                case "patients":
                    stmt.executeUpdate("CREATE TABLE patients (" +
                        "patient_id INT PRIMARY KEY, " +
                        "full_name VARCHAR(100) NOT NULL, " +
                        "dob DATE NOT NULL, " +
                        "gender ENUM('Male','Female','Other'), " +
                        "contact VARCHAR(20) NOT NULL, " +
                        "email VARCHAR(100) UNIQUE NOT NULL, " +
                        "registration_date DATE, " +
                        "FOREIGN KEY (patient_id) REFERENCES users(user_id))");
                    break;
                case "doctors":
                    stmt.executeUpdate("CREATE TABLE doctors (" +
                        "doctor_id INT PRIMARY KEY, " +
                        "full_name VARCHAR(100) NOT NULL, " +
                        "specialization VARCHAR(100) NOT NULL, " +
                        "contact VARCHAR(20) NOT NULL, " +
                        "availability ENUM('Available','On Leave','Busy') DEFAULT 'Available', " +
                        "FOREIGN KEY (doctor_id) REFERENCES users(user_id))");
                    break;
                case "appointments":
                    stmt.executeUpdate("CREATE TABLE appointments (" +
                        "appointment_id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "patient_id INT NOT NULL, " +
                        "doctor_id INT NOT NULL, " +
                        "appointment_date DATETIME NOT NULL, " +
                        "status ENUM('Scheduled','Completed','Cancelled','No-Show') DEFAULT 'Scheduled', " +
                        "notes TEXT, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (patient_id) REFERENCES patients(patient_id), " +
                        "FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id))");
                    break;
                case "specializations":
                    stmt.executeUpdate("CREATE TABLE specializations (" +
                        "specialization_id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "name VARCHAR(100) UNIQUE NOT NULL)");
                    break;
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }

    // User Management
    public static int addUser(String username, String password, String role) throws SQLException {
        String hashed = hashPassword(password);
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, hashed);
            pstmt.setString(3, role);
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    // Patient Management
    public static boolean addPatient(int userId, String fullName, Date dob, 
                               String gender, String contact, String email) {
        String sql = "INSERT INTO patients (patient_id, full_name, dob, gender, contact, email, username, registration_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, (SELECT username FROM users WHERE user_id = ?), CURDATE())";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, fullName);
            pstmt.setDate(3, dob);
            pstmt.setString(4, gender);
            pstmt.setString(5, contact);
            pstmt.setString(6, email);
            pstmt.setInt(7, userId);  // For username retrieval

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleSQLException(e);
            return false;
        }
    }
    // In DatabaseManager.java
    // Updated methods in DatabaseManager.java
    public static Patient getPatientByUsername(String username) throws SQLException {
        String sql = "SELECT p.*, u.username FROM patients p " +
                     "JOIN users u ON p.patient_id = u.user_id " +
                     "WHERE u.username = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Patient(
                        rs.getInt("patient_id"),
                        rs.getString("username"),  // Added username
                        rs.getString("full_name"),
                        rs.getDate("dob"),
                        rs.getString("gender"),
                        rs.getString("contact"),
                        rs.getString("email"),
                        rs.getDate("registration_date")
                    );
                }
            }
        }
        return null;
    }
    
    // Replace the old authenticateUser method with this
    public static User authenticateUser(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (storedHash.equals(hashPassword(password))) {
                        return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            storedHash,
                            rs.getString("role"),
                            rs.getTimestamp("created_at")
                        );
                    }
                }
            }
        }
        return null;
    }

    // Updated authentication method to include username in Patient object
    public static boolean authenticateUser(String username, String password, String role) {
        String sql = "SELECT password FROM users WHERE username = ? AND role = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, role);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    return storedHash.equals(hashPassword(password));
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return false;
    }

    // Updated method to get complete patient details
    public static Patient getPatientDetails(int patientId) throws SQLException {
        String sql = "SELECT p.*, u.username FROM patients p " +
                     "JOIN users u ON p.patient_id = u.user_id " +
                     "WHERE p.patient_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Patient(
                        rs.getInt("patient_id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getDate("dob"),
                        rs.getString("gender"),
                        rs.getString("contact"),
                        rs.getString("email"),
                        rs.getDate("registration_date")
                    );
                }
            }
        }
        return null;
    }
    
    // Receptionist Management (new)
    public static boolean addReceptionist(int userId, String fullName, String contact) throws SQLException {
        String sql = "INSERT INTO receptionists (receptionist_id, full_name, contact) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, fullName);
            pstmt.setString(3, contact);
            
            return pstmt.executeUpdate() > 0;
        }
    }

    public static boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // Doctor Management
    public static boolean addDoctor(int userId, String fullName, String specialization, String contact, String availability) {
        String sql = "INSERT INTO doctors (doctor_id, full_name, specialization, contact) " +
                     "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, fullName);
            pstmt.setString(3, specialization);
            pstmt.setString(4, contact);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            handleSQLException(e);
            return false;
        }
    }
    
    
    // In DatabaseManager.java
    public static List<Doctor> getAllDoctors() throws SQLException {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT d.*, u.username FROM doctors d "
                   + "JOIN users u ON d.doctor_id = u.user_id "
                   + "WHERE d.availability = 'Available'";  // Only show available doctors

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                doctors.add(new Doctor(
                    rs.getInt("doctor_id"),
                    rs.getString("username"),
                    rs.getString("full_name"),
                    rs.getString("specialization"),
                    rs.getString("contact"),
                    rs.getString("availability")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching doctors: " + e.getMessage());
            throw e; // Re-throw for proper error handling in UI
        }
        return doctors;
    }
    
    public static Doctor getDoctorByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM doctors WHERE doctor_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Doctor(
                        rs.getInt("doctor_id"),
                        rs.getString("full_name"),
                        rs.getString("specialization"),
                        rs.getString("contact"),
                        rs.getString("availability")
                    );
                }
            }
        }
        return null;
    }
    
    public static Appointment getAppointmentById(int appointmentId) throws SQLException {
        String sql = "SELECT a.*, p.full_name AS patient_name, " +
                     "d.full_name AS doctor_name, d.specialization " + // Add specialization
                     "FROM appointments a " +
                     "JOIN patients p ON a.patient_id = p.patient_id " +
                     "JOIN doctors d ON a.doctor_id = d.doctor_id " +
                     "WHERE a.appointment_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, appointmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Appointment(
                        rs.getInt("appointment_id"),
                        rs.getTimestamp("appointment_date"),
                        rs.getString("patient_name"),
                        rs.getString("doctor_name"),
                        rs.getString("specialization"),
                        rs.getString("status"),
                        rs.getString("notes")  // Add this to your Appointment constructor
                    );
                }
            }
        }
        return null;
    }

    public static Patient getPatientByAppointmentId(int appointmentId) throws SQLException {
        String sql = "SELECT p.patient_id, p.full_name, p.dob, p.gender, "
                   + "p.contact, p.email, p.registration_date "
                   + "FROM patients p "
                   + "JOIN appointments a ON p.patient_id = a.patient_id "
                   + "WHERE a.appointment_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, appointmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Patient(
                        rs.getInt("patient_id"),
                        "", // username not needed here
                        rs.getString("full_name"),
                        rs.getDate("dob"),
                        rs.getString("gender"),
                        rs.getString("contact"),
                        rs.getString("email"),
                        rs.getDate("registration_date")
                    );
                }
            }
        }
        return null;
    }

    public static List<Patient> getAllPatients() throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT p.*, u.username FROM patients p " +
                     "JOIN users u ON p.patient_id = u.user_id";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                patients.add(new Patient(
                    rs.getInt("patient_id"),
                    rs.getString("username"),
                    rs.getString("full_name"),
                    rs.getDate("dob"),
                    rs.getString("gender"),
                    rs.getString("contact"),
                    rs.getString("email"),
                    rs.getDate("registration_date")
                ));
            }
        }
        return patients;
    }

    // Authentication
     
     
     private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            return bytesToHex(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    public static List<Appointment> getPatientAppointments(int patientId) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.appointment_id, a.appointment_date, " +
                     "p.full_name AS patient_name, d.full_name AS doctor_name, " +  // Added patient name
                     "d.specialization, a.status, a.notes " +
                     "FROM appointments a " +
                     "JOIN patients p ON a.patient_id = p.patient_id " +  // Added patients join
                     "JOIN doctors d ON a.doctor_id = d.doctor_id " +
                     "WHERE a.patient_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(new Appointment(
                        rs.getInt("appointment_id"),
                        rs.getTimestamp("appointment_date"),
                        rs.getString("patient_name"),  // Patient name
                        rs.getString("doctor_name"),
                        rs.getString("specialization"),
                        rs.getString("status"),
                        rs.getString("notes")
                    ));
                }
            }
        }
        return appointments;
    }

    public static boolean cancelAppointment(int appointmentId) throws SQLException {
        String sql = "UPDATE appointments SET status = 'Cancelled' WHERE appointment_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, appointmentId);
            return pstmt.executeUpdate() > 0;
        }
    }

    // Appointment Management
    public static boolean addAppointment(int patientId, int doctorId, 
                                        Timestamp dateTime, String notes) throws SQLException {
         String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, notes) " +
                      "VALUES (?, ?, ?, ?)";

         try (Connection conn = getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {

             pstmt.setInt(1, patientId);
             pstmt.setInt(2, doctorId);
             pstmt.setTimestamp(3, dateTime);
             pstmt.setString(4, notes);

             return pstmt.executeUpdate() > 0;
         }
     }
    
    // In DatabaseManager.java
    public static List<Appointment> getTodaysAppointments() throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.appointment_id, a.appointment_date, " +
                     "p.full_name AS patient_name, d.full_name AS doctor_name, " +
                     "d.specialization, a.status, a.notes " +  // Added a.notes
                     "FROM appointments a " +
                     "JOIN patients p ON a.patient_id = p.patient_id " +
                     "JOIN doctors d ON a.doctor_id = d.doctor_id " +
                     "WHERE DATE(a.appointment_date) = CURDATE()";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                appointments.add(new Appointment(
                    rs.getInt("appointment_id"),
                    rs.getTimestamp("appointment_date"),
                    rs.getString("patient_name"),
                    rs.getString("doctor_name"),
                    rs.getString("specialization"),
                    rs.getString("status"),
                    rs.getString("notes")  // Add notes parameter
                ));
            }
        }
        return appointments;
    }
    
    public static List<Appointment> getDoctorAppointments(int doctorId) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.appointment_id, a.appointment_date, " +
                     "p.full_name, d.specialization, a.status,  a.notes " +  // Added specialization and status
                     "FROM appointments a " +
                     "JOIN patients p ON a.patient_id = p.patient_id " +
                     "JOIN doctors d ON a.doctor_id = d.doctor_id " +
                     "WHERE a.doctor_id = ? AND DATE(a.appointment_date) = CURDATE()";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, doctorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Example update in getDoctorAppointments():
                    appointments.add(new Appointment(
                        rs.getInt("appointment_id"),
                        rs.getTimestamp("appointment_date"),
                        rs.getString("full_name"),
                        "", // Doctor name (empty in doctor view)
                        rs.getString("specialization"),  // Specialization from doctor table
                        rs.getString("status"),
                        rs.getString("notes")
                    ));
                }
            }
        }
        return appointments;
    }
    
    public static boolean savePrescription(int appointmentId, String prescription) throws SQLException {
        // Get appointment details including doctor's name
        Appointment appointment = getAppointmentById(appointmentId);
        if (appointment == null) {
            throw new SQLException("Appointment not found with ID: " + appointmentId);
        }

        // Create formatted prescription entry
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String formattedEntry = String.format(
            "\n\n=== Prescription Entry ===\n" +
            "Date: %s\n" +
            "Doctor: Dr. %s\n" +
            "Medication:\n%s\n" +
            "=== End of Prescription ===",
            dateFormat.format(new java.util.Date()), // Explicitly use java.util.Date
            appointment.getDoctorName(),
            prescription.replaceAll("(?m)^", "• ")
        );

        String sql = "UPDATE appointments SET notes = CONCAT(IFNULL(notes, ''), ?) WHERE appointment_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, formattedEntry);
            pstmt.setInt(2, appointmentId);

            return pstmt.executeUpdate() > 0;
        }
    }
    
    public static boolean rescheduleAppointment(int appointmentId, Timestamp newDateTime) throws SQLException {
        String sql = "UPDATE appointments SET appointment_date = ? WHERE appointment_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, newDateTime);
            pstmt.setInt(2, appointmentId);

            return pstmt.executeUpdate() > 0;
        }
    }

    // Utility Methods
    private static void handleSQLException(SQLException e) {
        System.err.println("SQL Error: " + e.getMessage());
        System.err.println("SQL State: " + e.getSQLState());
        System.err.println("Error Code: " + e.getErrorCode());
    }

    public static void testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("Connection successful!");
            System.out.println("Server version: " + conn.getMetaData().getDatabaseProductVersion());
        } catch (SQLException e) {
            System.err.println("Connection failed:");
            e.printStackTrace();
        }
    }
    
    public static List<Appointment> getPatientMedicalHistory(int patientId) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT a.appointment_id, a.appointment_date, " +
                     "d.full_name AS doctor_name, d.specialization, " +
                     "a.status, a.notes " +
                     "FROM appointments a " +
                     "JOIN doctors d ON a.doctor_id = d.doctor_id " +
                     "WHERE a.patient_id = ? AND a.status IN ('Completed', 'No-Show')";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(new Appointment(
                        rs.getInt("appointment_id"),
                        rs.getTimestamp("appointment_date"),
                        "", // Patient name not needed
                        rs.getString("doctor_name"),
                        rs.getString("specialization"),
                        rs.getString("status"),
                        rs.getString("notes")
                    ));
                }
            }
        }
        return appointments;
    }

    // Add additional methods for other operations as needed

    public static boolean updateAppointmentStatus(int appointmentId, String newStatus) throws SQLException {
        String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, appointmentId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            handleSQLException(e);
            throw e; // Re-throw to let caller handle UI notifications
        }
    }
    
    
}