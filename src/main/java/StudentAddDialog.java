import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.sql.*;

// 学生添加对话框类
public class StudentAddDialog extends JFrame {

    private Connection connection;
    private JFrame frame = new JFrame("Formulaire étudiant");
    private static DefaultTableModel tableModel;

    public StudentAddDialog(Connection connection, JFrame frame, DefaultTableModel tableModel) {
        this.connection = connection;
        this.frame = frame;
        this.tableModel = tableModel;

        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        setTitle("Ajouter Étudiant");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(new Dimension(400, 200));

        JPanel mainPanel = new JPanel(new GridLayout(6, 2));

        JTextField studentNameField = new JTextField(10);
        JTextField studentFirstNameField = new JTextField(10);

        JComboBox<String> formationComboBox = new JComboBox<>(getFormationOptions());
        JComboBox<String> promotionComboBox = new JComboBox<>(getPromotionOptions());

        mainPanel.add(new JLabel("Nom :"));
        mainPanel.add(studentNameField);
        mainPanel.add(new JLabel("Prénom :"));
        mainPanel.add(studentFirstNameField);
        mainPanel.add(new JLabel("Formation :"));
        mainPanel.add(formationComboBox);
        mainPanel.add(new JLabel("Promotion :"));
        mainPanel.add(promotionComboBox);

        JButton validerButton = new JButton("Valider");
        JButton effacerButton = new JButton("Effacer");

        mainPanel.add(validerButton);
        mainPanel.add(effacerButton);
        add(mainPanel);

        ImageIcon icon = new ImageIcon("src/Picture/logo_D.jpg");
        setIconImage(icon.getImage());

        validerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nom = studentNameField.getText();
                String prenom = studentFirstNameField.getText();
                String formation = (String) formationComboBox.getSelectedItem();
                String promotion = (String) promotionComboBox.getSelectedItem();
                int formationNumero = -1;

                nom = capitalizeFirstLetter(nom);
                prenom = capitalizeFirstLetter(prenom);

                if (nom.isEmpty() || prenom.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Les champs 'Nom' et 'Prénom' ne peuvent pas être vides.", "Erreur", JOptionPane.ERROR_MESSAGE);
                } else {

                    switch (formation.toLowerCase() + promotion.toLowerCase()) {
                        case "idinitial":
                            formationNumero = 1;
                            break;
                        case "idalternance":
                            formationNumero = 2;
                            break;
                        case "idcontinue":
                            formationNumero = 3;
                            break;
                        case "sitninitial":
                            formationNumero = 4;
                            break;
                        case "sitnalternance":
                            formationNumero = 5;
                            break;
                        case "sitncontinue":
                            formationNumero = 6;
                            break;
                        case "ifinitial":
                            formationNumero = 7;
                            break;
                        case "ifalternance":
                            formationNumero = 8;
                            break;
                        case "ifcontinue":
                            formationNumero = 9;
                            break;
                        default:
                            JOptionPane.showMessageDialog(frame, "Formation ou promotion non valides", "Erreur", JOptionPane.ERROR_MESSAGE);
                            break;
                    }

                    if (formationNumero != -1) {
                        try {
                            String sql = "INSERT INTO Etudiants (nom, prenom, formation_id) VALUES (?, ?, ?)";
                            PreparedStatement preparedStatement = connection.prepareStatement(sql);
                            preparedStatement.setString(1, nom);
                            preparedStatement.setString(2, prenom);
                            preparedStatement.setInt(3, formationNumero);


                            preparedStatement.executeUpdate();

                            tableModel.addRow(new Object[]{getLastInsertedStudentId(), nom, prenom, formation, promotion});

                            studentNameField.setText("");
                            studentFirstNameField.setText("");
                            formationComboBox.setSelectedIndex(0);
                            promotionComboBox.setSelectedIndex(0);

                            JOptionPane.showMessageDialog(frame, "Étudiant ajouté avec succès", "Confirmation", JOptionPane.INFORMATION_MESSAGE);

                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });

        effacerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                studentNameField.setText("");
                studentFirstNameField.setText("");
                formationComboBox.setSelectedIndex(0);
                promotionComboBox.setSelectedIndex(0);
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private DefaultComboBoxModel<String> getFormationOptions() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        try {
            String query = "SELECT DISTINCT nom FROM Formations";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String formation = resultSet.getString("nom");
                model.addElement(formation);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return model;
    }

    private DefaultComboBoxModel<String> getPromotionOptions() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        try {
            String query = "SELECT DISTINCT promotion FROM Formations";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String promotion = resultSet.getString("promotion");
                model.addElement(promotion);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return model;
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    private int getLastInsertedStudentId() {
        int lastInsertedId = -1;
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT LAST_INSERT_ID() as last_id");
            if (resultSet.next()) {
                lastInsertedId = resultSet.getInt("last_id");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return lastInsertedId;
    }
}
