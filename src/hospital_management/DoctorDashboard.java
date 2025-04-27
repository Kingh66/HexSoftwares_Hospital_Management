package hospital_management;

import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;

public class DoctorDashboard extends JFrame {
    private final int doctorId;
    private String doctorName;
    private JTable appointmentsTable;
    private JTextArea patientInfoArea;
    private JTextArea prescriptionArea;

    public DoctorDashboard(int doctorId, String doctorName) throws SQLException {
        this.doctorId = doctorId;
        this.doctorName = doctorName; // Add this field to the class
        initializeUI();
        loadDoctorData();
    }

    private void initializeUI() {
        setTitle("Doctor Dashboard - City Hospital");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(35, 133, 119));
        
        JLabel welcomeLabel = new JLabel("Dr. " + doctorName);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        
        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(Color.WHITE);
        btnLogout.addActionListener(this::handleLogout);
        headerPanel.add(btnLogout, BorderLayout.EAST);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Main Content Panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(600);

        // Left Panel - Appointments
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Today's Appointments"));
        
        String[] appointmentColumns = {"ID", "Time", "Patient", "Status", "Actions"};
        DefaultTableModel model = new DefaultTableModel(appointmentColumns, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Integer.class : Object.class;
            }
        };
        appointmentsTable = new JTable(model);
        
        // Hide the ID column
        appointmentsTable.removeColumn(appointmentsTable.getColumnModel().getColumn(0));
    
        JScrollPane tableScroll = new JScrollPane(appointmentsTable);
        leftPanel.add(tableScroll, BorderLayout.CENTER);
        
        // Appointment Controls
        JPanel appointmentControls = new JPanel();
        JButton btnComplete = new JButton("Complete Appointment");
        JButton btnReschedule = new JButton("Reschedule");
        JButton btnRefresh = new JButton("Refresh");
        
        btnComplete.addActionListener(e -> {
            try {
                this.completeAppointment(e);
            } catch (SQLException ex) {
                Logger.getLogger(DoctorDashboard.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        btnReschedule.addActionListener(this::rescheduleAppointment);
        btnRefresh.addActionListener(e -> {
            try {
                refreshAppointments();
            } catch (SQLException ex) {
                Logger.getLogger(DoctorDashboard.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        appointmentControls.add(btnComplete);
        appointmentControls.add(btnReschedule);
        appointmentControls.add(btnRefresh);
        leftPanel.add(appointmentControls, BorderLayout.SOUTH);

        // Right Panel - Patient Information
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Patient Information"));
        
        // Patient Info
        patientInfoArea = new JTextArea();
        patientInfoArea.setEditable(false);
        JScrollPane patientScroll = new JScrollPane(patientInfoArea);
        patientScroll.setPreferredSize(new Dimension(400, 300));
        
        // Add this in initializeUI() after creating the appointmentsTable
        appointmentsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = appointmentsTable.getSelectedRow();
                if (selectedRow >= 0) {
                    try {
                        int appointmentId = (int) appointmentsTable.getModel().getValueAt(selectedRow, 0);
                        updatePatientInfo(appointmentId);
                    } catch (SQLException ex) {
                        showWarning("Error loading patient details: " + ex.getMessage());
                    }
                }
            }
        });
        
        // Prescription Panel
        JPanel prescriptionPanel = new JPanel(new BorderLayout());
        prescriptionPanel.setBorder(BorderFactory.createTitledBorder("Write Prescription"));
        
        prescriptionArea = new JTextArea();
        JScrollPane prescriptionScroll = new JScrollPane(prescriptionArea);
        
        JButton btnSavePrescription = new JButton("Save Prescription");
        btnSavePrescription.addActionListener(this::savePrescription);
        
        prescriptionPanel.add(prescriptionScroll, BorderLayout.CENTER);
        prescriptionPanel.add(btnSavePrescription, BorderLayout.SOUTH);

        rightPanel.add(patientScroll, BorderLayout.NORTH);
        rightPanel.add(prescriptionPanel, BorderLayout.CENTER);

        splitPane.add(leftPanel);
        splitPane.add(rightPanel);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void loadDoctorData() throws SQLException {
        refreshAppointments();
    }

    void refreshAppointments() throws SQLException {
        List<Appointment> appointments = DatabaseManager.getDoctorAppointments(doctorId);
        DefaultTableModel model = (DefaultTableModel) appointmentsTable.getModel();
        model.setRowCount(0);

        for (Appointment app : appointments) {
            model.addRow(new Object[]{
                app.getAppointmentId(), // Hidden ID
                app.getFormattedTime(),
                app.getPatientName(),
                app.getStatus(),
                "View Details"
            });
        }
    }

    private void completeAppointment(ActionEvent e) throws SQLException {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Please select an appointment to complete");
            return;
        }
        
        // Get ID from hidden column
        int appointmentId = (int) appointmentsTable.getModel().getValueAt(selectedRow, 0);
        
        if (DatabaseManager.updateAppointmentStatus(appointmentId, "Completed")) {
            refreshAppointments();
            showSuccess("Appointment marked as completed");
        } else {
            showWarning("Failed to update appointment status");
        }
    }
    
    private void updatePatientInfo(int appointmentId) throws SQLException {
        Appointment appointment = DatabaseManager.getAppointmentById(appointmentId);
        Patient patient = DatabaseManager.getPatientByAppointmentId(appointmentId);

        StringBuilder info = new StringBuilder();

        if (patient == null) {
            info.append("Patient record not found");
        } else {
            info.append("=== Patient Details ===\n")
                .append(String.format("%-15s: %s\n", "Name", patient.getFullName()))
                .append(String.format("%-15s: %s\n", "Birth Date", patient.getFormattedDob()))
                .append(String.format("%-15s: %s\n", "Gender", patient.getGender()))
                .append(String.format("%-15s: %s\n", "Contact", patient.getContact()))
                .append(String.format("%-15s: %s\n\n", "Email", patient.getEmail()));

            info.append("=== Medical History ===\n");
            if (appointment != null && appointment.getNotes() != null && !appointment.getNotes().isEmpty()) {
                info.append(appointment.getNotes().replaceAll("\n", "\n• "));
            } else {
                info.append("No medical history recorded");
            }
        }

        patientInfoArea.setText(info.toString());
    }

    private void rescheduleAppointment(ActionEvent e) {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Please select an appointment to reschedule");
            return;
        }

        // Get the appointment ID from the hidden column (column 0)
        int appointmentId = (int) appointmentsTable.getModel().getValueAt(selectedRow, 0);

        // Pass the ID to the dialog
        new RescheduleDialog(this, appointmentId).setVisible(true);
    }

    private void savePrescription(ActionEvent e) {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            showWarning("Please select an appointment");
            return;
        }

        String prescriptionText = prescriptionArea.getText();
        if (prescriptionText.isEmpty()) {
            showWarning("Please write a prescription before saving");
            return;
        }

        try {
            int appointmentId = (int) appointmentsTable.getModel().getValueAt(selectedRow, 0);
            if (DatabaseManager.savePrescription(appointmentId, prescriptionText)) {
                showSuccess("Prescription saved successfully");
                prescriptionArea.setText("");
            } else {
                showWarning("Failed to save prescription");
            }
        } catch (SQLException ex) {
            showWarning("Database error: " + ex.getMessage());
        }
    }

    private void handleLogout(ActionEvent e) {
        dispose();
        new ChooseUser().setVisible(true);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private void showSuccess(String message) {
            JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
        }

    }

    // Reschedule Dialog
    class RescheduleDialog extends JDialog {
        private JDateChooser dateChooser;
        private JComboBox<String> timeCombo;

        private final int appointmentId; // Add field

        public RescheduleDialog(JFrame parent, int appointmentId) { // Updated constructor
            super(parent, "Reschedule Appointment", true);
            this.appointmentId = appointmentId; // Store the ID
            initializeUI();
        }

        private void initializeUI() {
            setSize(300, 200);
            setLocationRelativeTo(getParent());

            JPanel mainPanel = new JPanel(new GridLayout(3, 2, 10, 10));

            mainPanel.add(new JLabel("New Date:"));
            dateChooser = new JDateChooser();
            mainPanel.add(dateChooser);

            mainPanel.add(new JLabel("New Time:"));
            timeCombo = new JComboBox<>(new String[]{"09:00", "10:00", "11:00", "14:00", "15:00"});
            mainPanel.add(timeCombo);

            JButton btnConfirm = new JButton("Confirm");
            btnConfirm.addActionListener(e -> confirmReschedule());

            mainPanel.add(btnConfirm);

            add(mainPanel);
        }

        private void confirmReschedule() {
            try {
                Date newDate = dateChooser.getDate();
                String newTime = (String) timeCombo.getSelectedItem();

                if (newDate == null || newTime == null) {
                    JOptionPane.showMessageDialog(this, 
                        "Please select both date and time",
                        "Input Error",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Combine date and time
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                Date time = timeFormat.parse(newTime);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(newDate);
                Calendar timeCalendar = Calendar.getInstance();
                timeCalendar.setTime(time);

                calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
                calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));

                Timestamp newDateTime = new Timestamp(calendar.getTimeInMillis());

                // Update appointment in database using the stored appointmentId
                if (DatabaseManager.rescheduleAppointment(this.appointmentId, newDateTime)) {
                    JOptionPane.showMessageDialog(this,
                        "Appointment rescheduled successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                    // Refresh the parent dashboard's appointments
                    ((DoctorDashboard) getParent()).refreshAppointments();
                    dispose();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Error rescheduling: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }