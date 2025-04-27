package hospital_management;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PatientDashboard extends JFrame {
    private final Patient patient;
    private JTable appointmentsTable;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    public PatientDashboard(Patient patient) {
        this.patient = patient;
        try {
            initializeUI();
            loadPatientData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null,
                "Error initializing dashboard: " + ex.getMessage(),
                "Initialization Error",
                JOptionPane.ERROR_MESSAGE
            );
            dispose();
        }
    }

    private void initializeUI() {
        setTitle("Patient Dashboard - City Hospital");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Navigation Panel
        JPanel navPanel = createNavigationPanel();
        mainPanel.add(navPanel, BorderLayout.WEST);

        // Main Content Panel
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        
        contentPanel.add(createAppointmentsPanel(), "APPOINTMENTS");
        contentPanel.add(createPlaceholderPanel("New Appointment"), "NEW_APPOINTMENT");
        contentPanel.add(createMedicalHistoryPanel(), "MEDICAL_HISTORY");
        contentPanel.add(createPlaceholderPanel("Coming soon!!!"), "PROFILE_SETTINGS");
        contentPanel.add(createPlaceholderPanel("Coming soon!!!"), "HELP");

        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(35, 133, 119));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel welcomeLabel = new JLabel("Welcome, " + patient.getFullName());
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(Color.WHITE);
        btnLogout.addActionListener(this::handleLogout);
        headerPanel.add(btnLogout, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createNavigationPanel() {
        JPanel navPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        navPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        navPanel.setBackground(new Color(240, 245, 240));

        String[] navButtons = {
            "View Appointments", "New Appointment", 
            "Medical History", "Profile Settings", "Help"
        };

        for (String title : navButtons) {
            JButton btn = new JButton(title);
            btn.setBackground(Color.WHITE);
            btn.addActionListener(this::handleNavigation);
            navPanel.add(btn);
        }

        return navPanel;
    }

    private JPanel createAppointmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Table setup with hidden ID column
        String[] columns = {"ID", "Date", "Doctor", "Specialization", "Status", "Actions"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Integer.class : Object.class;
            }
        };
        
        appointmentsTable = new JTable(model);
        
        // Hide the ID column (first column)
        appointmentsTable.removeColumn(appointmentsTable.getColumnModel().getColumn(0));
        
        JScrollPane scrollPane = new JScrollPane(appointmentsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Control buttons
        JPanel controlPanel = new JPanel();
        JButton btnRefresh = new JButton("Refresh");
        JButton btnCancel = new JButton("Cancel Appointment");
        
        btnRefresh.addActionListener(e -> {
            try {
                refreshAppointments();
            } catch (SQLException ex) {
                Logger.getLogger(PatientDashboard.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        btnCancel.addActionListener(e -> cancelAppointment());
        
        controlPanel.add(btnRefresh);
        controlPanel.add(btnCancel);
        panel.add(controlPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createPlaceholderPanel(String text) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private void loadPatientData() throws SQLException {
        refreshAppointments();
    }
    
    

    private void refreshAppointments() throws SQLException {
        List<Appointment> appointments = DatabaseManager.getPatientAppointments(patient.getId());
        DefaultTableModel model = (DefaultTableModel) appointmentsTable.getModel();
        model.setRowCount(0);

        for (Appointment app : appointments) {
            model.addRow(new Object[]{
                app.getAppointmentId(),  // Hidden ID (column 0)
                app.getFormattedTime(),  // Date (column 1)
                app.getDoctorName(),     // Doctor (column 2)
                app.getSpecialization(), // Specialization (column 3)
                app.getStatus(),         // Status (column 4)
                "View Details"           // Actions (column 5)
            });
        }
    }

    private void cancelAppointment() {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select an appointment to cancel", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Get ID from model (column 0 is hidden ID)
        int appointmentId = (int) appointmentsTable.getModel().getValueAt(selectedRow, 0);
        try {
            if (DatabaseManager.cancelAppointment(appointmentId)) {
                JOptionPane.showMessageDialog(this,
                    "Appointment cancelled successfully",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );
                refreshAppointments();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error cancelling appointment: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void handleNavigation(ActionEvent e) {
        String command = ((JButton) e.getSource()).getText();
        switch (command) {
            case "View Appointments":
                cardLayout.show(contentPanel, "APPOINTMENTS");
                break;
            case "New Appointment":
                cardLayout.show(contentPanel, "NEW_APPOINTMENT");
                openNewAppointmentWindow();
                break;
             case "Medical History":
                cardLayout.show(contentPanel, "MEDICAL_HISTORY");
                // Refresh when accessing the tab
                Component[] comps = contentPanel.getComponents();
                for (Component comp : comps) {
                    if (comp.getName() != null && comp.getName().equals("MEDICAL_HISTORY")) {
                        ((JPanel) comp).revalidate();
                    }
                }
                break;
            case "Profile Settings":
                cardLayout.show(contentPanel, "PROFILE_SETTINGS");
                break;
            case "Help":
                cardLayout.show(contentPanel, "HELP");
                break;
        }
    }

    private void openNewAppointmentWindow() {
        new BookAppointmentFrame(patient).setVisible(true);
    }

    private void handleLogout(ActionEvent e) {
        dispose();
        new ChooseUser().setVisible(true);
    }

    private JPanel createMedicalHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Table setup
        String[] columns = {"Date", "Doctor", "Specialization", "Status", "Medical Notes"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable historyTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(historyTable);

        // Load data
        try {
            List<Appointment> history = DatabaseManager.getPatientMedicalHistory(patient.getId());
            for (Appointment app : history) {
                model.addRow(new Object[]{
                    app.getFormattedTime(),
                    app.getDoctorName(),
                    app.getSpecialization(),
                    app.getStatus(),
                    app.getNotes()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading medical history: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }

        panel.add(scrollPane, BorderLayout.CENTER);

        // Add refresh button
        JButton btnRefresh = new JButton("Refresh History");
        btnRefresh.addActionListener(e -> refreshMedicalHistory(model));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnRefresh);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }
    
    private void refreshMedicalHistory(DefaultTableModel model) {
        try {
            List<Appointment> history = DatabaseManager.getPatientMedicalHistory(patient.getId());
            model.setRowCount(0);
            for (Appointment app : history) {
                model.addRow(new Object[]{
                    app.getFormattedTime(),
                    app.getDoctorName(),
                    app.getSpecialization(),
                    app.getStatus(),
                    app.getNotes()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error refreshing history: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}