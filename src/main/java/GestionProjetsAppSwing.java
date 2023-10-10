import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class GestionProjetsAppSwing {
    private static Connection connection=null;
    public static void main(String[] args) {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_projets", "root", "root");
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
        });
    }


    private static DefaultTableModel tableModel;

    private static void createAndShowGUI() {

        JFrame frame = new JFrame("Gestion des Projets Étudiants");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 600));

        // Création du panneau principal
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Création du tableau pour afficher la liste des étudiants
        String[] columnNames = {"ID", "Nom", "Prénom", "Formation", "Promotion"};
        tableModel = new DefaultTableModel(columnNames, 0); // Utilisation du modèle de données

        JTable studentTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(studentTable);

        // Création des boutons pour ajouter et supprimer des étudiants
        JButton addStudentButton = new JButton("Ajouter Étudiant");
        JButton deleteStudentButton = new JButton("Supprimer Étudiant");

        // Création du formulaire pour ajouter un étudiant
        JTextField studentIdField = new JTextField(10);
        JTextField studentNameField = new JTextField(10);
        JTextField studentFirstNameField = new JTextField(10);
        JTextField studentFormationField = new JTextField(10);
        JTextField studentPromotionField = new JTextField(10);

        JButton saveStudentButton = new JButton("Enregistrer");

        // Ajout des composants au panneau principal
        JPanel studentFormPanel = new JPanel(new GridLayout(6, 2));
        studentFormPanel.add(new JLabel("ID :"));
        studentFormPanel.add(studentIdField);
        studentFormPanel.add(new JLabel("Nom :"));
        studentFormPanel.add(studentNameField);
        studentFormPanel.add(new JLabel("Prénom :"));
        studentFormPanel.add(studentFirstNameField);
        studentFormPanel.add(new JLabel("Formation :"));
        studentFormPanel.add(studentFormationField);
        studentFormPanel.add(new JLabel("Promotion :"));
        studentFormPanel.add(studentPromotionField);
        studentFormPanel.add(saveStudentButton);

        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(studentFormPanel, BorderLayout.EAST);
        mainPanel.add(addStudentButton, BorderLayout.SOUTH);
        mainPanel.add(deleteStudentButton, BorderLayout.NORTH);

        frame.add(mainPanel);
        frame.pack();
        frame.setVisible(true);

        loadStudentsFromDatabase();


        // Ajout de gestionnaires d'événements
        addStudentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Récupérer les données du formulaire
                int id = Integer.parseInt(studentIdField.getText());
                String nom = studentNameField.getText();
                String prenom = studentFirstNameField.getText();
                String formation = studentFormationField.getText();
                String promotion = studentPromotionField.getText();

                try {
                    // Créer une requête SQL d'insertion
                    String sql = "INSERT INTO Etudiants (numero, nom, prenom, formation_id) VALUES (?, ?, ?, ?)";

                    PreparedStatement preparedStatement = connection.prepareStatement(sql);
                    preparedStatement.setInt(1, id);
                    preparedStatement.setString(2, nom);
                    preparedStatement.setString(3, prenom);
                    preparedStatement.setInt(4, 3); // Remplacez par l'ID de la formation appropriée

                    // Exécutez la requête d'insertion
                    preparedStatement.executeUpdate();

                    // Ajouter les données à la table
                    tableModel.addRow(new Object[]{id, nom, prenom, formation, promotion});

                    // Effacer les champs du formulaire
                    studentIdField.setText("");
                    studentNameField.setText("");
                    studentFirstNameField.setText("");
                    studentFormationField.setText("");
                    studentPromotionField.setText("");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

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
                        studentIdField.setText("");
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


        saveStudentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Code pour enregistrer un étudiant (si nécessaire)
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

                // Vous devrez récupérer le nom de la formation en fonction de l'ID de la formation
                // Pour simplifier, nous utilisons une chaîne vide pour le moment.
                String formation = "";

                // Ajoutez l'étudiant au tableau
                tableModel.addRow(new Object[]{id, nom, prenom, formation});
            }

            // Fermez les ressources JDBC
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

    }
}
