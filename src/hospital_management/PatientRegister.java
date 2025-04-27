package hospital_management;

import javax.swing.*;
import java.awt.*;
import java.sql.Date;
import java.sql.SQLException;
import com.toedter.calendar.JDateChooser;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PatientRegister extends JFrame {
    private JTextField txtFullName;
    private JDateChooser dateChooser;
    private JComboBox<String> genderCombo;
    private JTextField txtContact;
    private JTextField txtEmail;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirm;

    public PatientRegister() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Patient Registration - City Hospital");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Header
        JLabel titleLabel = new JLabel("Patient Registration");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(35, 133, 119));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Registration Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Initialize components
        txtFullName = new JTextField(25);
        dateChooser = new JDateChooser();
        dateChooser.setPreferredSize(new Dimension(200, 30));
        genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        txtContact = new JTextField(25);
        txtEmail = new JTextField(25);
        txtUsername = new JTextField(25);
        txtPassword = new JPasswordField(25);
        txtConfirm = new JPasswordField(25);

        addFormRow(formPanel, gbc, 0, "Full Name:", txtFullName);
        addFormRow(formPanel, gbc, 1, "Date of Birth:", dateChooser);
        addFormRow(formPanel, gbc, 2, "Gender:", genderCombo);
        addFormRow(formPanel, gbc, 3, "Contact:", txtContact);
        addFormRow(formPanel, gbc, 4, "Email:", txtEmail);
        addFormRow(formPanel, gbc, 5, "Username:", txtUsername);
        addFormRow(formPanel, gbc, 6, "Password:", txtPassword);
        addFormRow(formPanel, gbc, 7, "Confirm Password:", txtConfirm);

        JButton btnRegister = new JButton("Register");
        btnRegister.setBackground(new Color(35, 133, 119));
        btnRegister.setForeground(Color.BLACK);

        JButton btnBackToLogin = new JButton("Back to Login");
        btnBackToLogin.setBackground(new Color(200, 200, 200));
        btnBackToLogin.setForeground(Color.BLACK);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        buttonPanel.setBackground(Color.WHITE);

        JPanel registerPanel = new JPanel();
        registerPanel.add(btnRegister);
        registerPanel.setBackground(Color.WHITE);

        JPanel loginPanel = new JPanel();
        loginPanel.add(btnBackToLogin);
        loginPanel.setBackground(Color.WHITE);

        buttonPanel.add(registerPanel);
        buttonPanel.add(loginPanel);

        // Add components to main panel
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Event Listeners
        btnRegister.addActionListener(e -> {
            try {
                handleRegistration();
            } catch (SQLException ex) {
                Logger.getLogger(PatientRegister.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        btnBackToLogin.addActionListener(e -> {
            new PatientLogin().setVisible(true);
            dispose();
        });

        add(mainPanel);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component field) {
        JLabel lbl = new JLabel(label);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(field, gbc);
    }

    private JLabel createLinkLabel(String text) {
        JLabel label = new JLabel("<html><u>" + text + "</u></html>");
        label.setForeground(Color.BLUE);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return label;
    }

    private void handleRegistration() throws SQLException {
            String fullName = txtFullName.getText().trim();
            java.util.Date dobDate = dateChooser.getDate();
            String gender = (String) genderCombo.getSelectedItem();
            String contact = txtContact.getText().trim();
            String email = txtEmail.getText().trim();
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword()).trim();
            String confirmPassword = new String(txtConfirm.getPassword()).trim();

            try {
            if (!validateRegistration(fullName, dobDate, contact, email, username, 
                                     password, confirmPassword)) {
                return;
            }

            Date dob = new Date(dobDate.getTime());
            int userId = DatabaseManager.addUser(username, password, "patient");

            if (userId != -1) {
                if (DatabaseManager.addPatient(userId, fullName, dob, gender, contact, email)) {
                    // Show success message
                    JOptionPane.showMessageDialog(this,
                        "Registration successful! Please login.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    new PatientLogin().setVisible(true);
                    dispose();
                }
            }
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1062) { // MySQL duplicate entry error code
                JOptionPane.showMessageDialog(this,
                    "Username already exists",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            } else {
                throw ex; // Re-throw other SQL exceptions
            }
        }
    }
    

    private boolean validateRegistration(String fullName, java.util.Date dobDate,
                                        String contact, String email, String username,
                                        String password, String confirmPassword) {
        // Check empty fields
        if (fullName.isEmpty() || dobDate == null || contact.isEmpty() ||
                email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill all required fields",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        // Password confirmation check
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                    "Passwords do not match",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        // Email format validation
        if (!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            JOptionPane.showMessageDialog(this,
                    "Invalid email format",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        // Password strength check (minimum 8 characters)
        if (password.length() < 8) {
            JOptionPane.showMessageDialog(this,
                    "Password must be at least 8 characters",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        // Contact number validation (basic check)
        if (!contact.matches("\\d{10,15}")) {
            JOptionPane.showMessageDialog(this,
                    "Invalid contact number (10-15 digits required)",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        // Username validation (alphanumeric with underscores)
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            JOptionPane.showMessageDialog(this,
                    "Username can only contain letters, numbers, and underscores",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }
        return true;
     }
}