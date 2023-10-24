import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class StudentAddDialog extends JFrame {
    private Connection connection;
    private JFrame frame = new JFrame("Formulaire étudiant");
    private static DefaultTableModel tableModel;

    public StudentAddDialog(Connection connection,JFrame frame, DefaultTableModel tableModel) {
        this.connection = connection;
        this.frame=frame;
        this.tableModel = tableModel;

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

        validerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Récupérer les données du formulaire
                String nom = studentNameField.getText();
                String prenom = studentFirstNameField.getText();
                String formation = (String) formationComboBox.getSelectedItem();
                String promotion = (String) promotionComboBox.getSelectedItem();
                int formationNumero = -1; // Par défaut, en cas de correspondance non trouvée

                nom = capitalizeFirstLetter(nom);
                prenom = capitalizeFirstLetter(prenom);

                if (nom.isEmpty() || prenom.isEmpty()) {
                    // Vérifier que les champs "Nom" et "Prénom" ne sont pas vides
                    JOptionPane.showMessageDialog(frame, "Les champs 'Nom' et 'Prénom' ne peuvent pas être vides.", "Erreur", JOptionPane.ERROR_MESSAGE);
                } else {
                    // Les champs "Nom" et "Prénom" ne sont pas vides, continuez avec le traitement

                    // Utilisez une structure switch pour déterminer le numéro de formation en fonction de formation et promotion
                    switch (formation.toLowerCase() + promotion.toLowerCase()) { // Convertir en minuscules pour ignorer la casse
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
                            // Créer une requête SQL d'insertion en utilisant le numéro de formation déterminé
                            String sql = "INSERT INTO Etudiants (nom, prenom, formation_id) VALUES (?, ?, ?)";

                            PreparedStatement preparedStatement = connection.prepareStatement(sql);
                            preparedStatement.setString(1, nom);
                            preparedStatement.setString(2, prenom);
                            preparedStatement.setInt(3, formationNumero);

                            // Exécutez la requête d'insertion
                            preparedStatement.executeUpdate();

                            // Ajouter les données à la table
                            tableModel.addRow(new Object[]{getLastInsertedStudentId(), nom, prenom, formation, promotion});

                            // Effacer les champs du formulaire
                            studentNameField.setText("");
                            studentFirstNameField.setText("");

                            formationComboBox.setSelectedIndex(0); // Réinitialisez la sélection de la formation
                            promotionComboBox.setSelectedIndex(0); // Réinitialisez la sélection de la promotion

                            // Afficher une fenêtre contextuelle de confirmation
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
                // Effacez les champs du formulaire lorsque l'utilisateur clique sur "Effacer"
                studentNameField.setText("");
                studentFirstNameField.setText("");
                formationComboBox.setSelectedIndex(0); // Réinitialisez la sélection de la formation
                promotionComboBox.setSelectedIndex(0); // Réinitialisez la sélection de la promotion
            }
        });

        setLocationRelativeTo(null); // Centrer la fenêtre
        setVisible(true);
    }

    // Les méthodes getFormationOptions() et getPromotionOptions() peuvent être similaires à celles de Gestion_etudiant.
    // Assurez-vous que ces méthodes ont accès à la connexion à la base de données.


    // Méthode pour récupérer les options de formation depuis la base de données
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

    // Méthode pour récupérer les options de promotion depuis la base de données
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

    // Méthode pour mettre en majuscule la première lettre d'une chaîne
    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input; // Retourne la chaîne d'origine si elle est vide ou nulle
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    // Méthode pour obtenir le dernier numéro d'étudiant inséré
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
