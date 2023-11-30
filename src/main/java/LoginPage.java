import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LoginPage extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JRadioButton studentRadioButton;
    private JRadioButton teacherRadioButton;
    private static String currentUserRole;

    public LoginPage() {
        initializeUI();
    }


    // Initialise l'interface utilisateur
    private void initializeUI() {
        setTitle("Login Page");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setContentPane(new TransparentPanel(new BorderLayout())); // Use BorderLayout for the main panel

        JPanel panel = createMainPanel();
        add(panel);

        setVisible(true);

        Image iconImage = new ImageIcon("src/Picture/logo_D.jpg").getImage();
        setIconImage(iconImage);
    }


    // Crée le panneau principal
    private JPanel createMainPanel() {
        JPanel panel = new TransparentPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);

        addLogoAndTitle(panel, constraints);
        addLoginForm(panel, constraints);

        return panel;
    }


    // Ajoute le logo et le titre
    private void addLogoAndTitle(JPanel panel, GridBagConstraints constraints) {
        JLabel titleLabel = new JLabel("Université Paris Dauphine - PSL");
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        panel.add(titleLabel, constraints);
    }


    // Ajoute le formulaire de connexion
    private void addLoginForm(JPanel panel, GridBagConstraints constraints) {
        JLabel usernameLabel = new JLabel("Username");
        JLabel passwordLabel = new JLabel("Password");

        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);

        ButtonGroup roleButtonGroup = new ButtonGroup();
        studentRadioButton = new JRadioButton("Student ");
        teacherRadioButton = new JRadioButton("Teacher");

        // Définis la propriété opaque sur false pour la transparence
        studentRadioButton.setOpaque(false);
        teacherRadioButton.setOpaque(false);

        roleButtonGroup.add(studentRadioButton);
        roleButtonGroup.add(teacherRadioButton);

        JButton loginButton = createLoginButton();

        addFormField(panel, constraints, usernameLabel, 0, 2);
        addFormField(panel, constraints, usernameField, 1, 2);
        addFormField(panel, constraints, passwordLabel, 0, 3);
        addFormField(panel, constraints, passwordField, 1, 3);

        // Add radio buttons horizontally
        JPanel rolePanel = new TransparentPanel();
        rolePanel.add(studentRadioButton);
        rolePanel.add(teacherRadioButton);
        addFormField(panel, constraints, rolePanel, 1, 4);

        addFormField(panel, constraints, loginButton, 0, 5, 2);
    }


    // Crée le bouton de connexion
    private JButton createLoginButton() {
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> handleLogin());
        loginButton.setPreferredSize(new Dimension(120, 30));
        loginButton.setBackground(new Color(71, 120, 197));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        return loginButton;
    }


    // Ajoute un champ de formulaire au panneau
    private void addFormField(JPanel panel, GridBagConstraints constraints, JComponent component, int x, int y) {
        constraints.gridx = x;
        constraints.gridy = y;
        panel.add(component, constraints);
    }


    // Ajoute un champ de formulaire avec une largeur spécifiée au panneau
    private void addFormField(JPanel panel, GridBagConstraints constraints, JComponent component, int x, int y, int width) {
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = width;
        panel.add(component, constraints);
    }


    // Gère l'événement de connexion
    private void handleLogin() {
        String username = usernameField.getText();
        char[] passwordChars = passwordField.getPassword();
        String password = new String(passwordChars);
        String role = studentRadioButton.isSelected() ? "student" : "teacher";

        if (authenticate(username, password, role)) {
            currentUserRole = role;
            // 在认证成功时自动弹出 "Login Successful" 消息
            showMessage("Login Successful", "Message", JOptionPane.INFORMATION_MESSAGE, 500);

            // 在一秒后执行下面的任务
            Timer timer = new Timer(500, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(() -> {
                        new Menu();
                        dispose();
                    });
                }
            });
            timer.setRepeats(false); // 仅执行一次
            timer.start();
        } else {
            // 只在认证失败时弹出消息框
            JOptionPane.showMessageDialog(this, "Invalid Username, Password, or Role", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    // Affiche un message avec une durée spécifiée
    private void showMessage(String message, String title, int messageType, int duration) {
        JOptionPane optionPane = new JOptionPane(message, messageType);
        JDialog dialog = optionPane.createDialog(title);

        Timer timer = new Timer(duration, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
            }
        });
        timer.setRepeats(false); // 仅执行一次

        timer.start();
        dialog.setVisible(true);
    }


    // Authentifie l'utilisateur avec la base de données
    private boolean authenticate(String username, String password, String role) {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_projets", "root", "root")) {
            String query = "SELECT * FROM users WHERE username = ? AND password = ? AND role = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                preparedStatement.setString(3, role);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    // Renvoie le rôle actuel de l'utilisateur
    public static String getCurrentUserRole() {
        return currentUserRole;
    }


    // JPanel personnalisé pour l'image de fond avec transparence
    private class TransparentPanel extends JPanel {
        public TransparentPanel() {
            setOpaque(false);
        }

        public TransparentPanel(LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f)); // Adjust the transparency here
            Image bgImage = new ImageIcon("src/Picture/uni_logo.jpeg").getImage();
            g2d.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            g2d.dispose();
        }
    }
}
