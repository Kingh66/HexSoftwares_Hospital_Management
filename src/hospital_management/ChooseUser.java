package hospital_management;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;

public class ChooseUser extends JFrame {

    public ChooseUser() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Hospital Management System");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel with background color
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 245, 240));

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(35, 133, 119));
        JLabel titleLabel = new JLabel("Welcome to City Hospital");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // User Selection Panel
        JPanel userPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        userPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        userPanel.setBackground(new Color(240, 245, 240));

        // Create user buttons with proper image loading
        createUserButton(userPanel, "Patient", "patient_icon.png");
        createUserButton(userPanel, "Doctor", "doctor_icon.png");
        createUserButton(userPanel, "Receptionist", "receptionist_icon.png");

        mainPanel.add(userPanel, BorderLayout.CENTER);

        // Footer Panel
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(35, 133, 119));
        JLabel footerLabel = new JLabel("© 2025 City Hospital. All rights reserved.");
        footerLabel.setForeground(Color.WHITE);
        footerPanel.add(footerLabel);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void createUserButton(JPanel panel, String userType, String iconFileName) {
        JButton button = new JButton(userType);
        button.setFont(new Font("Arial", Font.PLAIN, 20));
        button.setFocusPainted(false);
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Load image using resource stream
        try {
            // Get image from resources folder
            URL imgUrl = getClass().getResource("/hospital_management/icons/" + iconFileName);
            if (imgUrl != null) {
                ImageIcon originalIcon = new ImageIcon(imgUrl);
                Image scaledImage = originalIcon.getImage()
                        .getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                button.setIcon(new ImageIcon(scaledImage));
            } else {
                // Fallback to file system path
                File imageFile = new File("src/hospital_management/icons/" + iconFileName);
                if (imageFile.exists()) {
                    ImageIcon originalIcon = new ImageIcon(imageFile.getAbsolutePath());
                    Image scaledImage = originalIcon.getImage()
                            .getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                    button.setIcon(new ImageIcon(scaledImage));
                } else {
                    throw new Exception("Image file not found: " + iconFileName);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading icon: " + e.getMessage());
            button.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
        }

        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setHorizontalTextPosition(SwingConstants.CENTER);

        button.addActionListener(e -> handleUserSelection(userType));
        panel.add(button);
    }

    private void handleUserSelection(String userType) {
        dispose();
        switch (userType) {
            case "Patient":
                new PatientLogin().setVisible(true);
                break;
            case "Doctor":
                new DoctorLogin().setVisible(true);
                break;
            case "Receptionist":
                new ReceptionistLogin().setVisible(true);
                break;
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new ChooseUser().setVisible(true);
        });
    }

}