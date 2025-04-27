package hospital_management;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.awt.event.ActionEvent;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BookAppointmentFrame extends JFrame {
    private final Patient patient;
    private JComboBox<Doctor> doctorCombo;
    private JSpinner dateSpinner;
    private JSpinner timeSpinner;
    private JTextArea notesArea;

    public BookAppointmentFrame(Patient patient) {
        this.patient = patient;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Book New Appointment");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        
        // Doctor Selection
        doctorCombo = new JComboBox<>();
        loadDoctors();
        formPanel.add(new JLabel("Select Doctor:"));
        formPanel.add(doctorCombo);

        // Date Picker
        dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        formPanel.add(new JLabel("Appointment Date:"));
        formPanel.add(dateSpinner);

        // Time Picker
        timeSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        formPanel.add(new JLabel("Appointment Time:"));
        formPanel.add(timeSpinner);

        // Notes
        notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        formPanel.add(new JLabel("Notes:"));
        formPanel.add(notesScroll);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Control Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSubmit = new JButton("Book Appointment");
        JButton btnCancel = new JButton("Cancel");

        btnSubmit.addActionListener(this::handleSubmit);
        btnCancel.addActionListener(e -> dispose());

        buttonPanel.add(btnSubmit);
        buttonPanel.add(btnCancel);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

    private void loadDoctors() {
        try {
            List<Doctor> doctors = DatabaseManager.getAllDoctors();
            doctors.forEach(doctorCombo::addItem);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading doctors: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void handleSubmit(ActionEvent e) {
        Doctor selectedDoctor = (Doctor) doctorCombo.getSelectedItem();
        Date date = ((SpinnerDateModel) dateSpinner.getModel()).getDate();
        Date time = ((SpinnerDateModel) timeSpinner.getModel()).getDate();

        if (selectedDoctor == null) {
            JOptionPane.showMessageDialog(this,
                "Please select a doctor",
                "Input Error",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (date == null || time == null) {
            JOptionPane.showMessageDialog(this,
                "Please select both date and time",
                "Input Error",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            // Combine date and time
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            Calendar timeCalendar = Calendar.getInstance();
            timeCalendar.setTime(time);
            
            calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
            
            Timestamp appointmentTime = new Timestamp(calendar.getTimeInMillis());

            boolean success = DatabaseManager.addAppointment(
                patient.getId(),
                selectedDoctor.getId(),
                appointmentTime,
                notesArea.getText()
            );

            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Appointment booked successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );
                dispose();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error booking appointment: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            loadDoctors(); // Refresh doctor list when shown
        }
    }
}