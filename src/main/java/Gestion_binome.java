import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class Gestion_binome {
    private static Connection connection = null;
    private static DefaultTableModel tableModel;
    JFrame frame;
    private Gestion_projet gestionProjetInstance; // Ajoutez une référence à l'instance de Gestion_projet

    public Gestion_binome(int projectNumber) {
        frame = new JFrame("Gestion des Binômes ");

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_projets", "root", "root");
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1200, 600));
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Création du tableau pour afficher la liste des binômes
        String[] columnNames = {"ID", "Projet", "Étudiant 1", "Étudiant 2", "Note Rapport", "Note Soutenance Étudiant 1", "Note Soutenance Étudiant 2", "Date de Remise Effective"};
        tableModel = new DefaultTableModel(columnNames, 0);

        // Création du panneau principal
        JTable binomeTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(binomeTable);

        // Création des boutons pour ajouter et supprimer des binômes
        JButton addBinomeButton = new JButton("Ajouter Binôme");
        JButton deleteBinomeButton = new JButton("Supprimer Binôme");
        // Bouton "Retour au Project"
        JButton retourProjectButton = new JButton("Retour au Project");
        // On masque la colonne ID
        TableColumnModel tableColumnModel = binomeTable.getColumnModel();
        tableColumnModel.getColumn(0).setMaxWidth(0);
        tableColumnModel.getColumn(0).setMinWidth(0);
        tableColumnModel.getColumn(0).setPreferredWidth(0);
        tableColumnModel.getColumn(0).setResizable(false);

        retourProjectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Fermez l'interface de gestion des binômes et revenez à l'instance existante de Gestion_projet
                frame.dispose(); // Ferme l'interface de gestion des binômes
                if (Gestion_projet.currentInstance != null) {
                    Gestion_projet.currentInstance.frame.setVisible(true); // Affichez à nouveau l'instance existante de Gestion_projet
                }
            }
        });
        // Ajout des composants au panneau principal
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addBinomeButton);
        buttonPanel.add(deleteBinomeButton);
        buttonPanel.add(retourProjectButton);

        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.pack();
        frame.setVisible(true);

        // Vérifier l'existence de la table de binômes
        boolean binomeTableExists = checkTableExistence(projectNumber);

        if (!binomeTableExists) {
            // Créer la table de binômes
            createBinomeTable(projectNumber);
        }
        loadBinomesFromDatabase(projectNumber);

        // Gestionnaire d'événements pour le bouton "Ajouter Binôme"
        addBinomeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new BinomeAddDialog(connection, frame, tableModel,projectNumber);
            }
        });

        deleteBinomeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = binomeTable.getSelectedRow();
                if (selectedRow != -1) {
                    int binomeIdToDelete = (int) tableModel.getValueAt(selectedRow, 0);

                    int confirmation = JOptionPane.showConfirmDialog(frame, "Êtes-vous sûr de vouloir supprimer ce binôme et la table associée ?", "Confirmation de suppression", JOptionPane.YES_NO_OPTION);
                    if (confirmation == JOptionPane.YES_OPTION) {
                        try {
                            String tableName = "Project_" + projectNumber;
                            String deleteSql = "DELETE FROM " + tableName + " WHERE id = ?";
                            PreparedStatement preparedStatement = connection.prepareStatement(deleteSql);
                            preparedStatement.setInt(1, binomeIdToDelete);
                            preparedStatement.executeUpdate();

                            // Supprimez la table de binômes associée au projet
                            deleteBinomeTable(tableName);

                            tableModel.removeRow(selectedRow);
                            // Vous pouvez également réinitialiser les champs du formulaire ici
                        } catch (SQLException ex) {
                            JOptionPane.showMessageDialog(frame, "Erreur lors de la suppression du binôme : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Veuillez sélectionner un binôme à supprimer.", "Avertissement", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
    }

    // Méthode pour supprimer la table de binômes
    private void deleteBinomeTable(String tableName) {
        try {
            Statement statement = connection.createStatement();
            String deleteTableSQL = "DROP TABLE IF EXISTS " + tableName;
            statement.executeUpdate(deleteTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Erreur lors de la suppression de la table de binômes : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void loadBinomesFromDatabase(int projectNumber) {
        tableModel.setRowCount(0); // Efface les lignes existantes dans le tableau
        try {
            String sql = "SELECT * FROM Project_" + projectNumber;
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int binomeId = resultSet.getInt("id");
                int projectId = resultSet.getInt("projet_numero");
                int student1Id = resultSet.getInt("etudiant1_numero");
                int student2Id = resultSet.getInt("etudiant2_numero");
                String rapport = resultSet.getString("note_rapport");
                String soutenance1 = resultSet.getString("note_soutenance_etu1");
                String soutenance2 = resultSet.getString("note_soutenance_etu2");
                String dateRemiseEffective = resultSet.getString("date_remise_effective");

                String project = getProjectName(projectId);
                String student1 = getStudentName(student1Id);
                String student2 = getStudentName(student2Id);

                tableModel.addRow(new Object[]{binomeId, project, student1, student2, rapport, soutenance1, soutenance2, dateRemiseEffective});
            }
            resultSet.close();
            preparedStatement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }



    private boolean checkTableExistence(int projectNumber) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "Project_" + projectNumber, null);
            return tables.next(); // Retourne vrai si la table existe, sinon faux
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void createBinomeTable(int projectNumber) {
        try {
            Statement statement = connection.createStatement();
            String tableName = "Project_" + projectNumber;
            String createTableSQL = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "projet_numero INT, " +
                    "etudiant1_numero INT, " +
                    "etudiant2_numero INT, " +
                    "note_rapport VARCHAR(255), " +
                    "note_soutenance_etu1 VARCHAR(255), " +
                    "note_soutenance_etu2 VARCHAR(255), " +
                    "date_remise_effective DATE" +
                    ")";
            statement.executeUpdate(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Erreur lors de la création de la table de binômes : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }



    private static String getProjectName(int projectId) {
        try {
            String query = "SELECT nom_matiere FROM Projets WHERE numero = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, projectId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("nom_matiere");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return "";
    }

    private static String getStudentName(int studentId) {
        try {
            String query = "SELECT nom FROM Etudiants WHERE numero = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, studentId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("nom");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return "";
    }
}
