package hospital_management;

import com.sun.jdi.connect.spi.Connection;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class ReceptionistLogin extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public ReceptionistLogin() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Receptionist Login - City Hospital");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Header
        JLabel titleLabel = new JLabel("Receptionist Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(35, 133, 119));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Login Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        txtUsername = new JTextField(20);
        txtPassword = new JPasswordField(20);
        
        // Login Button
        JButton btnLogin = new JButton("Login");
        btnLogin.setBackground(new Color(35, 133, 119));
        btnLogin.setForeground(Color.BLACK);
        btnLogin.setPreferredSize(new Dimension(120, 30));

        // Back Button
        JButton btnBack = new JButton("Back");
        btnBack.setBackground(new Color(200, 200, 200));
        btnBack.setForeground(Color.BLACK);
        btnBack.setPreferredSize(new Dimension(120, 30));

        // Add components to form
        addFormRow(formPanel, gbc, 0, "Username:", txtUsername);
        addFormRow(formPanel, gbc, 1, "Password:", txtPassword);

        // Button Panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JPanel loginButtonPanel = new JPanel();
        loginButtonPanel.add(btnLogin);
        loginButtonPanel.setBackground(Color.WHITE);
        
        JPanel backButtonPanel = new JPanel();
        backButtonPanel.add(btnBack);
        backButtonPanel.setBackground(Color.WHITE);
        
        buttonPanel.add(loginButtonPanel);
        buttonPanel.add(backButtonPanel);

        // Main layout
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(formPanel, BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
        add(mainPanel);

        // Event Listeners
        btnLogin.addActionListener(e -> handleLogin());
        btnBack.addActionListener(e -> {
            new ChooseUser().setVisible(true);
            dispose();
        });
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, Component field) {
        JLabel lbl = new JLabel(label);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(lbl, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(field, gbc);
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill all fields");
            return;
        }

        try {
            User user = DatabaseManager.authenticateUser(username, password);
            if (user != null && user.getRole().equals("receptionist")) {
                new ReceptionistDashboard().setVisible(true);
                dispose();
            } else {
                showError("Invalid username or password");
            }
        } catch (SQLException ex) {
            showError("Database error: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    // Manual setup method (run this once to create initial receptionist)
    public static void createDefaultReceptionist() {
        try {
            int userId = DatabaseManager.addUser(
                "reception", 
                "hospital123", 
                "receptionist"
            );
            
            if (userId != -1) {
                // Add to receptionists table if needed
                String sql = "INSERT INTO receptionists (receptionist_id, full_name, contact) VALUES (?, ?, ?)";
                try (java.sql.Connection conn = DatabaseManager.getConnection();
                     var pstmt = conn.prepareStatement(sql)) {
                    
                    pstmt.setInt(1, userId);
                    pstmt.setString(2, "John Doe");
                    pstmt.setString(3, "555-1234");
                    pstmt.executeUpdate();
                }
                System.out.println("Default receptionist created successfully!");
            }
        } catch (SQLException e) {
            System.err.println("Error creating receptionist: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Uncomment to create default receptionist (run once)
         createDefaultReceptionist();
        
        SwingUtilities.invokeLater(() -> {
            new ReceptionistLogin().setVisible(true);
        });
    }
}