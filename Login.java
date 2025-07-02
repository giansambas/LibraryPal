import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class Login extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public Login() {
        setTitle("LibraryPal Login");
        setSize(400, 280); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel logoLabel = new JLabel();
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            ImageIcon logoIcon = new ImageIcon("C:/Users/Gian Sambas/Desktop/java/LibraryPal/resources/LibPal_Logo.png"); 
            Image scaled = logoIcon.getImage().getScaledInstance(140, 50, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            logoLabel.setText("LibraryPal"); 
            logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        }
        add(logoLabel, BorderLayout.NORTH);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 255, 240));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font uiFont = new Font("Segoe UI", Font.PLAIN, 14);
        UIManager.put("Label.font", uiFont);
        UIManager.put("Button.font", uiFont);
        UIManager.put("TextField.font", uiFont);

        JLabel userLabel = new JLabel("Username:");
        JLabel passLabel = new JLabel("Password:");
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);

        JButton loginButton = new JButton("Login");
        loginButton.setBackground(new Color(0, 153, 76));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setFont(uiFont);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(userLabel, gbc);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(passLabel, gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(loginButton, gbc);

        add(panel, BorderLayout.CENTER);

        loginButton.addActionListener(e -> login());
        getRootPane().setDefaultButton(loginButton);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.equals("admin") && password.equals("123")) {
            dispose();
            try {
                Runtime.getRuntime().exec("cmd /c start \"\" \"C:\\Users\\Gian Sambas\\Desktop\\java\\LibraryPal\\run.bat\"");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to launch the application.", "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
            System.exit(0);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}
