package hospital_management;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;

public class ReceptionistDashboard extends JFrame {
    private JTable appointmentsTable;
    private JTable doctorsTable;
    private JTable patientsTable;

    public ReceptionistDashboard() {
        initializeUI();
        loadData();
    }

    private void initializeUI() {
        setTitle("Receptionist Dashboard - City Hospital");
        setSize(1280, 720);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(35, 133, 119));
        
        JLabel titleLabel = new JLabel("Receptionist Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(Color.WHITE);
        btnLogout.addActionListener(this::handleLogout);
        headerPanel.add(btnLogout, BorderLayout.EAST);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Main Tabbed Interface
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Appointments Tab
        tabbedPane.addTab("Appointments", createAppointmentsPanel());
        
        // Doctors Tab
        tabbedPane.addTab("Doctors", createDoctorsPanel());
        
        // Patients Tab
        tabbedPane.addTab("Patients", createPatientsPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel);
    }

   private JPanel createAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Create table model with hidden ID column
        String[] columns = {"ID", "Time", "Patient", "Doctor", "Status", "Action"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Integer.class : Object.class;
            }
        };

        appointmentsTable = new JTable(model);

        // Hide the ID column
        appointmentsTable.removeColumn(appointmentsTable.getColumnModel().getColumn(0));

        JScrollPane scrollPane = new JScrollPane(appointmentsTable);

        // Control Panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnCheckIn = new JButton("Check-In Patient");
        JButton btnMarkNoShow = new JButton("Mark as No-Show");
        JButton btnNewAppointment = new JButton("New Appointment");
        JTextField searchField = new JTextField(20);

        btnCheckIn.addActionListener(e -> {
            try {
                handleCheckIn(e);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Database Error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        btnMarkNoShow.addActionListener(e -> {
            try {
                handleMarkNoShow(e);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Database Error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        btnNewAppointment.addActionListener(this::handleNewAppointment);

        controlPanel.add(new JLabel("Search:"));
        controlPanel.add(searchField);
        controlPanel.add(btnCheckIn);
        controlPanel.add(btnMarkNoShow);
        controlPanel.add(btnNewAppointment);

        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDoctorsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Doctors Table
        String[] columns = {"ID", "Name", "Specialization", "Contact", "Availability"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        doctorsTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(doctorsTable);
        
        // Add Doctor Form
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        JTextField txtName = new JTextField();
        JTextField txtSpecialization = new JTextField();
        JTextField txtContact = new JTextField();
        JComboBox<String> cmbAvailability = new JComboBox<>(
            new String[]{"Available", "On Leave", "Busy"}
        );
        
        formPanel.add(new JLabel("Name:"));
        formPanel.add(txtName);
        formPanel.add(new JLabel("Specialization:"));
        formPanel.add(txtSpecialization);
        formPanel.add(new JLabel("Contact:"));
        formPanel.add(txtContact);
        formPanel.add(new JLabel("Availability:"));
        formPanel.add(cmbAvailability);
        
        JButton btnAddDoctor = new JButton("Add New Doctor");
        btnAddDoctor.addActionListener(e -> addNewDoctor(
            txtName.getText(),
            txtSpecialization.getText(),
            txtContact.getText(),
            (String) cmbAvailability.getSelectedItem()
        ));
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(formPanel, BorderLayout.NORTH);
        bottomPanel.add(btnAddDoctor, BorderLayout.SOUTH);
        
        panel.add(bottomPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPatientsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Patients Table
        String[] columns = {"ID", "Name", "Age", "Gender", "Contact", "Email"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        patientsTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(patientsTable);
        
        // Search Panel
        JPanel searchPanel = new JPanel();
        JTextField searchField = new JTextField(20);
        JButton btnSearch = new JButton("Search");
        
        searchPanel.add(new JLabel("Search Patient:"));
        searchPanel.add(searchField);
        searchPanel.add(btnSearch);
        
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadData() {
        refreshAppointments();
        refreshDoctors();
        refreshPatients();
    }

    private void refreshAppointments() {
        try {
            List<Appointment> appointments = DatabaseManager.getTodaysAppointments();
            DefaultTableModel model = (DefaultTableModel) appointmentsTable.getModel();
            model.setRowCount(0);

            for (Appointment appt : appointments) {
                model.addRow(new Object[]{
                    appt.getAppointmentId(),  // ID (hidden)
                    appt.getFormattedTime(),
                    appt.getPatientName(),
                    appt.getDoctorName(),
                    appt.getStatus(),
                    appt.getNotes(),
                    "Action"
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error loading appointments: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshDoctors() {
        try {
            List<Doctor> doctors = DatabaseManager.getAllDoctors();
            DefaultTableModel model = (DefaultTableModel) doctorsTable.getModel();
            model.setRowCount(0);

            for (Doctor doctor : doctors) {
                model.addRow(new Object[]{
                    doctor.getId(),
                    doctor.getFullName(),
                    doctor.getSpecialization(),
                    doctor.getContact(),
                    doctor.getAvailability()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading doctors: " + ex.getMessage());
        }
    }

    private void refreshPatients() {
        try {
            List<Patient> patients = DatabaseManager.getAllPatients();
            DefaultTableModel model = (DefaultTableModel) patientsTable.getModel();
            model.setRowCount(0);

            for (Patient patient : patients) {
                model.addRow(new Object[]{
                    patient.getId(),
                    patient.getFullName(),
                    patient.getAge(),
                    patient.getGender(),
                    patient.getContact(),
                    patient.getEmail()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading patients: " + ex.getMessage());
        }
    }

    

    private void handleCheckIn(ActionEvent e) throws SQLException {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select an appointment to check-in", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Get ID from model instead of table view
        DefaultTableModel model = (DefaultTableModel) appointmentsTable.getModel();
        int appointmentId = (int) model.getValueAt(selectedRow, 0);

        if (DatabaseManager.updateAppointmentStatus(appointmentId, "Completed")) {
            refreshAppointments();
        }
    }

    private void handleMarkNoShow(ActionEvent e) throws SQLException {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select an appointment to mark as no-show", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Get ID from model instead of table view
        DefaultTableModel model = (DefaultTableModel) appointmentsTable.getModel();
        int appointmentId = (int) model.getValueAt(selectedRow, 0);

        if (DatabaseManager.updateAppointmentStatus(appointmentId, "No-Show")) {
            refreshAppointments();
        }
    }

    private void handleNewAppointment(ActionEvent e) {
        // Create dialog components
        JDialog appointmentDialog = new JDialog(this, "Schedule New Appointment", true);
        appointmentDialog.setLayout(new BorderLayout());
        appointmentDialog.setSize(500, 400);
        appointmentDialog.setLocationRelativeTo(this);

        // Form components
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));

        // Patient selection
        JComboBox<Patient> patientCombo = new JComboBox<>();
        try {
            List<Patient> patients = DatabaseManager.getAllPatients();
            patients.forEach(patientCombo::addItem);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading patients: " + ex.getMessage());
        }

        // Doctor selection
        JComboBox<Doctor> doctorCombo = new JComboBox<>();
        try {
            List<Doctor> doctors = DatabaseManager.getAllDoctors();
            doctors.forEach(doctorCombo::addItem);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading doctors: " + ex.getMessage());
        }

        // Date and time pickers
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        dateSpinner.setEditor(dateEditor);
        timeSpinner.setEditor(timeEditor);

        // Notes field
        JTextArea notesField = new JTextArea(3, 20);
        notesField.setLineWrap(true);
        JScrollPane notesScroll = new JScrollPane(notesField);

        // Add components to form
        formPanel.add(new JLabel("Patient:"));
        formPanel.add(patientCombo);
        formPanel.add(new JLabel("Doctor:"));
        formPanel.add(doctorCombo);
        formPanel.add(new JLabel("Date:"));
        formPanel.add(dateSpinner);
        formPanel.add(new JLabel("Time:"));
        formPanel.add(timeSpinner);
        formPanel.add(new JLabel("Notes:"));
        formPanel.add(notesScroll);

        // Control buttons
        JButton btnSchedule = new JButton("Schedule");
        JButton btnCancel = new JButton("Cancel");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnSchedule);
        buttonPanel.add(btnCancel);

        appointmentDialog.add(formPanel, BorderLayout.CENTER);
        appointmentDialog.add(buttonPanel, BorderLayout.SOUTH);

        // Event handlers
        btnSchedule.addActionListener(ev -> {
            try {
                Patient selectedPatient = (Patient) patientCombo.getSelectedItem();
                Doctor selectedDoctor = (Doctor) doctorCombo.getSelectedItem();

                // Get date/time values as java.util.Date
                java.util.Date utilDate = ((SpinnerDateModel) dateSpinner.getModel()).getDate();
                java.util.Date utilTime = ((SpinnerDateModel) timeSpinner.getModel()).getDate();

                // Validate selections
                if (selectedPatient == null || selectedDoctor == null) {
                    JOptionPane.showMessageDialog(appointmentDialog,
                        "Please select both patient and doctor",
                        "Missing Information",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Combine date and time using Calendar
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(utilDate);

                Calendar timeCalendar = Calendar.getInstance();
                timeCalendar.setTime(utilTime);

                calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
                calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));

                Timestamp appointmentTime = new Timestamp(calendar.getTimeInMillis());

                // Save to database
                boolean success = DatabaseManager.addAppointment(
                    selectedPatient.getId(),
                    selectedDoctor.getId(),
                    appointmentTime,
                    notesField.getText()
                );

                if (success) {
                    JOptionPane.showMessageDialog(appointmentDialog,
                        "Appointment scheduled successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    refreshAppointments();
                    appointmentDialog.dispose();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(appointmentDialog,
                    "Invalid Date/Time Selection: " + ex.getMessage(),
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancel.addActionListener(ev -> appointmentDialog.dispose());
        appointmentDialog.setVisible(true);
    }

    private void addNewDoctor(String name, String specialization, 
                            String contact, String availability) {
        if (name.isEmpty() || specialization.isEmpty() || contact.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please fill all required fields",
                "Input Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        try {
            int userId = DatabaseManager.addUser(
                name.replaceAll(" ", "").toLowerCase(),
                "tempPassword",
                "doctor"
            );
            
            if (userId != -1 && DatabaseManager.addDoctor(
                userId, 
                name, 
                specialization, 
                contact, 
                availability)) {
                refreshDoctors();
                JOptionPane.showMessageDialog(this,
                    "Doctor added successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error adding doctor: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void handleLogout(ActionEvent e) {
        dispose();
        new ChooseUser().setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ReceptionistDashboard().setVisible(true);
        });
    }
}