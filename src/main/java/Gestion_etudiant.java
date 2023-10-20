import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class Gestion_etudiant{
    private static Connection connection = null;
    private static DefaultTableModel tableModel;

    JFrame frame = new JFrame("Gestion des Projets Étudiants");
    Gestion_etudiant(){
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_projets", "root", "root");
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 600));
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Création du tableau pour afficher la liste des étudiants
        String[] columnNames = {"ID", "Nom", "Prénom", "Formation", "Promotion"};
        tableModel = new DefaultTableModel(columnNames, 0); // Utilisation du modèle de données

        // Création du panneau principal

        JTable studentTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(studentTable);

        // Création des boutons pour ajouter et supprimer des étudiants
        JButton addStudentButton = new JButton("Ajouter Étudiant");
        JButton deleteStudentButton = new JButton("Supprimer Étudiant");


        // Création du formulaire pour ajouter un étudiant
        JTextField studentNameField = new JTextField(10);
        JTextField studentFirstNameField = new JTextField(10);
        JTextField studentFormationField = new JTextField(10);
        JTextField studentPromotionField = new JTextField(10);

        // Ajout des composants au panneau principal
        JPanel studentFormPanel = new JPanel(new GridLayout(6, 2));
        studentFormPanel.add(new JLabel("Nom :"));
        studentFormPanel.add(studentNameField);
        studentFormPanel.add(new JLabel("Prénom :"));
        studentFormPanel.add(studentFirstNameField);
        studentFormPanel.add(new JLabel("Formation :"));
        studentFormPanel.add(studentFormationField);
        studentFormPanel.add(new JLabel("Promotion :"));
        studentFormPanel.add(studentPromotionField);

        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(studentFormPanel, BorderLayout.EAST);
        mainPanel.add(addStudentButton, BorderLayout.SOUTH);
        mainPanel.add(deleteStudentButton, BorderLayout.NORTH);

        frame.add(mainPanel);
        frame.pack();
        frame.setVisible(true);
        loadStudentsFromDatabase();


// Gestionnaire d'événements pour le bouton "Ajouter Étudiant"
        addStudentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Récupérer les données du formulaire
                String nom = studentNameField.getText();
                String prenom = studentFirstNameField.getText();
                String formation = studentFormationField.getText();
                String promotion = studentPromotionField.getText();
                int formationNumero = -1; // Par défaut, en cas de correspondance non trouvée

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
                        studentFormationField.setText("");
                        studentPromotionField.setText("");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });


        // Gestionnaire d'événements pour le bouton "Supprimer Étudiant"
        deleteStudentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = studentTable.getSelectedRow();
                if (selectedRow != -1) {
                    // Récupérez l'ID de l'étudiant sélectionné dans le tableau
                    int studentIdToDelete = (int) tableModel.getValueAt(selectedRow, 0);

                    try {
                        // Créez une requête SQL de suppression de l'étudiant par ID
                        String deleteSql = "DELETE FROM Etudiants WHERE numero = ?";

                        PreparedStatement preparedStatement = connection.prepareStatement(deleteSql);
                        preparedStatement.setInt(1, studentIdToDelete);

                        // Exécutez la requête de suppression
                        preparedStatement.executeUpdate();

                        // Supprimez également la ligne du tableau
                        tableModel.removeRow(selectedRow);

                        // Effacez les champs du formulaire
                        studentNameField.setText("");
                        studentFirstNameField.setText("");
                        studentFormationField.setText("");
                        studentPromotionField.setText("");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });



    }


    private static void loadStudentsFromDatabase() {
        try {
            // Créez une requête SQL pour récupérer les étudiants
            String sql = "SELECT numero, nom, prenom, formation_id FROM Etudiants";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            // Ajoutez les étudiants à la table du modèle
            while (resultSet.next()) {
                int id = resultSet.getInt("numero");
                String nom = resultSet.getString("nom");
                String prenom = resultSet.getString("prenom");
                int formationId = resultSet.getInt("formation_id");

                // Maintenant, vous devrez récupérer le nom de la formation en fonction de l'ID de la formation
                String formation = getFormationName(formationId); // Utilisez une méthode séparée

                // Vous devez également récupérer la promotion en fonction de l'ID de la formation, si elle existe dans une table distincte.
                String promotion = getPromotion(formationId); // Utilisez une autre méthode pour récupérer la promotion

                // Ajoutez l'étudiant au tableau
                tableModel.addRow(new Object[]{id, nom, prenom, formation, promotion});
            }

            // Fermez les ressources JDBC
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Méthode pour récupérer le nom de la formation en fonction de l'ID
    private static String getFormationName(int formationId) {
        try {
            String query = "SELECT nom FROM Formations WHERE numero = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, formationId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("nom");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return "";
    }

    // Méthode pour récupérer la promotion en fonction de l'ID de la formation
    private static String getPromotion(int formationId) {
        try {
            String query = "SELECT promotion FROM Formations WHERE numero = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, formationId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("promotion");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return "";
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
