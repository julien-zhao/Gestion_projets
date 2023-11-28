import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginPage extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private static String currentUserRole;

    public LoginPage() {
        setTitle("Login Page");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);

        // Logo and Text
        JLabel logoLabel = new JLabel(new ImageIcon("src/Picture/dauphine-psl.png"));

        JLabel titleLabel = new JLabel("Université Paris Dauphine - PSL");
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        panel.add(logoLabel, constraints);

        constraints.gridy = 1;
        panel.add(titleLabel, constraints);

        // Login Form
        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        JLabel roleLabel = new JLabel("");


        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);

        String[] roles = {"student", "teacher"};
        roleComboBox = new JComboBox<>(roles);
        roleComboBox.setSelectedItem(null); // Set initially selected item to null



        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                char[] passwordChars = passwordField.getPassword();
                String password = new String(passwordChars);
                String role = (String) roleComboBox.getSelectedItem();

                if (authenticate(username, password, role)) {
                    // Stocke le rôle de l'utilisateur
                    currentUserRole = role;

                    JOptionPane.showMessageDialog(LoginPage.this, "Login Successful");
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            new Menu();
                            dispose(); // Close the login window
                        }
                    });
                } else {
                    JOptionPane.showMessageDialog(LoginPage.this, "Invalid Username, Password, or Role", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        loginButton.setPreferredSize(new Dimension(120, 30));
        loginButton.setBackground(new Color(71, 120, 197));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));

        constraints.gridx = 0;
        constraints.gridy = 2;
        panel.add(usernameLabel, constraints);

        constraints.gridx = 1;
        panel.add(usernameField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 3;
        panel.add(passwordLabel, constraints);

        constraints.gridx = 1;
        panel.add(passwordField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 4;
        panel.add(roleLabel, constraints);

        constraints.gridx = 1;
        panel.add(roleComboBox, constraints);

        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.gridwidth = 2;
        panel.add(loginButton, constraints);

        add(panel);

        setVisible(true);
    }

    private boolean authenticate(String username, String password, String role) {
        // Replace with your database connection code
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_projets", "root", "root");
            String query = "SELECT * FROM users WHERE username = ? AND password = ? AND role = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, role);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    // Méthode pour obtenir le rôle de l'utilisateur actuel
    public static String getCurrentUserRole() {
        return currentUserRole;
    }

}
