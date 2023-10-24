import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class Gestion_projet {
    private static Connection connection = null;
    private static DefaultTableModel tableModel;
    private JTextField searchField;
    private JTable projectTable; // Ajoutez un champ pour le JTable

    JFrame frame = new JFrame("Gestion des Projets Étudiants");

    Gestion_projet() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_projets", "root", "root");
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 600));
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Création du champ de recherche
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Rechercher");
        searchPanel.add(new JLabel("Recherche rapide :"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Ajout d'un gestionnaire d'événements pour le bouton de recherche
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchText = searchField.getText();
                filterTable(searchText);
            }
        });

        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // Création du tableau pour afficher la liste des projets
        String[] columnNames = {"Nom Matière", "Sujet", "Date de Remise"};
        tableModel = new DefaultTableModel(columnNames, 0);

        projectTable = new JTable(tableModel); // Utilisez le champ projectTable

        JScrollPane tableScrollPane = new JScrollPane(projectTable);

        // Création des boutons pour ajouter et supprimer des projets
        JButton addProjectButton = new JButton("Ajouter Projet");
        JButton deleteProjectButton = new JButton("Supprimer Projet");

        JButton retourMenuButton = new JButton("Retour au Menu");

        retourMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                new Menu();
            }
        });

        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Créez un panneau pour les boutons "Ajouter Projet" et "Retour au Menu"
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addProjectButton);
        buttonPanel.add(deleteProjectButton);
        buttonPanel.add(retourMenuButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.pack();
        frame.setVisible(true);
        loadProjectsFromDatabase();

        // Gestionnaire d'événements pour le bouton "Ajouter Projet"
        addProjectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ProjectAddDialog(tableModel, connection);
            }
        });

        // Gestionnaire d'événements pour le bouton "Supprimer Projet"
        deleteProjectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = projectTable.getSelectedRow();
                if (selectedRow != -1) {
                    String matiere = (String) tableModel.getValueAt(selectedRow, 0);
                    String sujet = (String) tableModel.getValueAt(selectedRow, 1);
                    String dateRemise = (String) tableModel.getValueAt(selectedRow, 2);

                    try {
                        String deleteSql = "DELETE FROM Projets WHERE nom_matiere = ? AND sujet = ? AND date_remise = ?";
                        PreparedStatement preparedStatement = connection.prepareStatement(deleteSql);
                        preparedStatement.setString(1, matiere);
                        preparedStatement.setString(2, sujet);
                        preparedStatement.setString(3, dateRemise);
                        preparedStatement.executeUpdate();

                        tableModel.removeRow(selectedRow);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    private static void loadProjectsFromDatabase() {
        try {
            String sql = "SELECT nom_matiere, sujet, date_remise FROM Projets";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String matiere = resultSet.getString("nom_matiere");
                String sujet = resultSet.getString("sujet");
                String dateRemise = resultSet.getString("date_remise");

                tableModel.addRow(new Object[]{matiere, sujet, dateRemise});
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void filterTable(String searchText) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
        projectTable.setRowSorter(sorter); // Mettez à jour le RowSorter du JTable
    }
}
