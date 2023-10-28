import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.RowFilter;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.text.Collator;
import java.util.Comparator;

public class Gestion_projet {
    private static Connection connection = null;
    private static DefaultTableModel tableModel;
    private JTable projectTable;
    JFrame frame = new JFrame("Gestion des Projets Étudiants");
    static Gestion_projet currentInstance; // Variable statique pour stocker l'instance actuelle


    Gestion_projet() {
        currentInstance = this; // Affectez l'instance actuelle à la variable statique
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_projets", "root", "root");
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 600));
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Création du tableau pour afficher la liste des projets
        String[] columnNames = {"Numero","Nom Matière", "Sujet", "Date de Remise"};
        tableModel = new DefaultTableModel(columnNames, 0);
        projectTable = new JTable(tableModel);

        // Activez le tri sur le tableau
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        projectTable.setRowSorter(sorter);

        // Pour trier les colonnes en ignorant la casse (insensible à la casse)
        TableRowSorter<DefaultTableModel> caseInsensitiveSorter = new TableRowSorter<>(tableModel) {
            @Override
            public Comparator<?> getComparator(int column) {
                if (column == 1 || column == 2 || column == 3) {
                    return Collator.getInstance();
                }
                return super.getComparator(column);
            }
        };
        projectTable.setRowSorter(caseInsensitiveSorter);

        // Création du champ de recherche rapide
        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Recherche rapide");

        JScrollPane tableScrollPane = new JScrollPane(projectTable);

        // Création des boutons pour ajouter et supprimer des projets
        JButton addProjectButton = new JButton("Ajouter Projet");
        JButton deleteProjectButton = new JButton("Supprimer Projet");
        JButton gestionBinomeButton = new JButton("Gestion binôme");
        JButton generatePDFButton = new JButton("Générer en PDF"); // Bouton pour générer le PDF
        JButton retourMenuButton = new JButton("Retour au Menu");

        //On masque la colonne ID
        TableColumnModel tableColumnModel = projectTable.getColumnModel();
        tableColumnModel.getColumn(0).setMaxWidth(0);
        tableColumnModel.getColumn(0).setMinWidth(0);
        tableColumnModel.getColumn(0).setPreferredWidth(0);
        tableColumnModel.getColumn(0).setResizable(false);


        retourMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                new Menu();
            }
        });

        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Recherche : "));
        searchPanel.add(searchField);

        // Créez un panneau pour les boutons "Ajouter Projet" et "Retour au Menu"
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addProjectButton);
        buttonPanel.add(deleteProjectButton);
        buttonPanel.add(gestionBinomeButton);
        buttonPanel.add(generatePDFButton); // Ajoutez le bouton "Générer en PDF"
        buttonPanel.add(retourMenuButton);

        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterTable(searchField.getText(), projectTable);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTable(searchField.getText(), projectTable);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Les mises à jour d'attributs ne sont pas traitées ici
            }
        });

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
        deleteProjectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = projectTable.getSelectedRow();
                if (selectedRow != -1) {
                    int projectNumber = getPrimaryKeyValueFromSelectedRow();

                    if (projectNumber != -1) {
                        // Vérifier s'il y a des binômes associés à ce projet
                        if (areBinomesAssociated(projectNumber)) {
                            // Obtenir le nom du projet pour afficher dans le message
                            String projectName = (String) tableModel.getValueAt(selectedRow, getColumnIndex("Nom Matière"));

                            // Afficher une boîte de dialogue d'avertissement en fonction de la présence de binômes
                            if (confirmBinomeDeletion(projectName)) {
                                // Supprimer les binômes associés au projet
                                if (deleteBinomesForProject(projectNumber)) {
                                    // Les binômes ont été supprimés avec succès, supprimer le projet
                                    deleteProject(selectedRow);
                                } else {
                                    // Afficher un message d'erreur si la suppression des binômes a échoué
                                    JOptionPane.showMessageDialog(frame, "La suppression des binômes a échoué.", "Erreur", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        } else {
                            // S'il n'y a pas de binômes associés, demander simplement à l'utilisateur de confirmer la suppression
                            if (confirmProjectDeletion() && deleteBinomesForProject(projectNumber)) {
                                deleteProject(selectedRow);
                            }
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Veuillez sélectionner un projet à supprimer.", "Avertissement", JOptionPane.WARNING_MESSAGE);
                }
            }
        });


        // Gestionnaire d'événements pour le bouton "Générer en PDF"
        generatePDFButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generatePDF();
            }
        });


        gestionBinomeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Étape 3 : Vérifiez si un projet est sélectionné
                int selectedRow = projectTable.getSelectedRow();
                if (selectedRow != -1) {
                    // Récupérez le nom du projet sélectionné
                    int selectedProjectNumber = getPrimaryKeyValueFromSelectedRow();
                    // Créez une instance de la fenêtre "Gestion binôme" avec le nom du projet
                    new Gestion_binome(selectedProjectNumber);
                } else {
                    // Affichez un message d'erreur ou une boîte de dialogue indiquant que rien n'a été sélectionné.
                    JOptionPane.showMessageDialog(frame, "Sélectionnez un projet avant de gérer les binômes.", "Avertissement", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

    }

    private static void loadProjectsFromDatabase() {
        try {
            String sql = "SELECT numero,nom_matiere, sujet, date_remise FROM Projets";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int numero = resultSet.getInt("numero");
                String matiere = resultSet.getString("nom_matiere");
                String sujet = resultSet.getString("sujet");
                String dateRemise = resultSet.getString("date_remise");

                tableModel.addRow(new Object[]{numero,matiere, sujet, dateRemise});
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    private void filterTable(String searchText, JTable projectTable) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
        projectTable.setRowSorter(sorter);
    }

    private int getColumnIndex(String columnName) {
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            if (columnName.equals(tableModel.getColumnName(i))) {
                return i;
            }
        }
        return -1;
    }

    //On obtient la clé primaire du project selectionné
    private int getPrimaryKeyValueFromSelectedRow() {
        int projectNumber = -1;
        int selectedRow = projectTable.getSelectedRow();
        if (selectedRow != -1) {
            // Accédez aux valeurs de la colonne ici
            String matiere = (String) tableModel.getValueAt(selectedRow, getColumnIndex("Nom Matière"));
            String sujet = (String) tableModel.getValueAt(selectedRow, getColumnIndex("Sujet"));
            String dateRemise = (String) tableModel.getValueAt(selectedRow, getColumnIndex("Date de Remise"));

            // Effectuez les opérations nécessaires
            try {
                String sql = "SELECT Numero FROM Projets WHERE nom_matiere = ? AND sujet = ? AND date_remise = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, matiere);
                preparedStatement.setString(2, sujet);
                preparedStatement.setString(3, dateRemise);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    projectNumber = resultSet.getInt("Numero");
                }

                resultSet.close();
                preparedStatement.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return projectNumber;
    }

    private void deleteProject(int selectedRow) {
        int projectNumber = getPrimaryKeyValueFromSelectedRow();
        if (projectNumber != -1) {
            // Supprimer les enregistrements liés dans la table Notes
            deleteNotesForProject(projectNumber);

            // Supprimer le projet
            String matiere = (String) tableModel.getValueAt(selectedRow, getColumnIndex("Nom Matière"));
            String sujet = (String) tableModel.getValueAt(selectedRow, getColumnIndex("Sujet"));
            String dateRemise = (String) tableModel.getValueAt(selectedRow, getColumnIndex("Date de Remise"));

            try {
                // Supprimer le projet de la table Projets
                String deleteSql = "DELETE FROM Projets WHERE numero = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(deleteSql);
                preparedStatement.setInt(1, projectNumber);
                preparedStatement.executeUpdate();

                tableModel.removeRow(selectedRow);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            // Supprimer la table des binômes si elle existe
            if (areBinomesAssociated(projectNumber)) {
                if (deleteBinomesForProject(projectNumber)) {
                    System.out.println("Table de binômes supprimée avec succès.");
                } else {
                    System.err.println("La suppression de la table de binômes a échoué.");
                }
            } else {
                System.out.println("Aucune table de binômes associée.");
            }
        }
    }



    private void deleteNotesForProject(int projectNumber) {
        try {
            String deleteSql = "DELETE FROM Notes WHERE projet_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(deleteSql);
            preparedStatement.setInt(1, projectNumber);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Méthode pour supprimer tous les binômes associés à un projet
    private boolean deleteBinomesForProject(int projectNumber) {
        String tableName = "Project_" + projectNumber;
        try {
            // Créez un statement pour exécuter la commande DROP TABLE
            String deleteTableSQL = "DROP TABLE IF EXISTS " + tableName;
            Statement statement = connection.createStatement();
            statement.execute(deleteTableSQL);

            return true; // La table de binômes a été supprimée avec succès
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // La suppression de la table de binômes a échoué
        }
    }


    boolean areBinomesAssociated(int projectNumber) {
        String tableName = "Project_" + projectNumber;
        try {
            DatabaseMetaData metadata = connection.getMetaData();
            ResultSet resultSet = metadata.getTables(null, null, tableName, null);

            if (resultSet.next()) {
                String countQuery = "SELECT COUNT(*) FROM " + tableName;
                PreparedStatement preparedStatement = connection.prepareStatement(countQuery);
                ResultSet countResult = preparedStatement.executeQuery();

                if (countResult.next()) {
                    int rowCount = countResult.getInt(1); // Récupère le nombre d'enregistrements
                    return rowCount > 0; // Retourne true si au moins un enregistrement existe
                }
            }
            return false; // Si la table n'existe pas ou si aucune donnée n'est présente, retourne false
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    // Méthode pour confirmer la suppression du projet avec ou sans binômes
// Méthode pour confirmer la suppression du projet avec ou sans binômes
    private boolean confirmBinomeDeletion(String projectName) {
        int option;
        if (areBinomesAssociated(getPrimaryKeyValueFromSelectedRow())) {
            // Si des binômes sont associés au projet, affichez un avertissement
            option = JOptionPane.showConfirmDialog(frame, "Il y a des binômes associés au projet '" + projectName + "'. La suppression de ce projet entraînera également la suppression de tous les binômes associés. Voulez-vous continuer ?", "Avertissement", JOptionPane.YES_NO_OPTION);
        } else {
            // S'il n'y a pas de binômes associés, affichez une confirmation normale
            option = JOptionPane.showConfirmDialog(frame, "Voulez-vous vraiment supprimer ce projet ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        }
        return option == JOptionPane.YES_OPTION;
    }

    // Méthode pour demander confirmation de la suppression du projet sans binômes
    private boolean confirmProjectDeletion() {
        int option = JOptionPane.showConfirmDialog(frame, "Voulez-vous vraiment supprimer ce projet ? (Aucun binôme n'est associé à ce projet)", "Confirmation", JOptionPane.YES_NO_OPTION);
        return option == JOptionPane.YES_OPTION;
    }



    private void generatePDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Enregistrer le PDF");

        // Définissez le filtre de fichier pour afficher uniquement les fichiers PDF
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Fichiers PDF (*.pdf)", "pdf");
        fileChooser.setFileFilter(filter);

        int userSelection = fileChooser.showSaveDialog(frame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                // Obtenez le fichier sélectionné par l'utilisateur
                File fileToSave = fileChooser.getSelectedFile();

                if (!fileToSave.getName().toLowerCase().endsWith(".pdf")) {
                    // Assurez-vous que l'extension est .pdf
                    fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".pdf");
                }

                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(fileToSave));
                document.open();

                // Titre du document
                document.add(new Paragraph("Liste des projets"));
                document.add(new Paragraph("\n")); // Ajout d'un paragraphe vide (saut de ligne)

                // Créez une table avec une colonne de moins que le modèle de table
                PdfPTable pdfTable = new PdfPTable(tableModel.getColumnCount() - 1); // Moins une colonne (la colonne "ID" n'est pas incluse)
                pdfTable.setWidthPercentage(100);

                // En-têtes de colonne (en excluant la colonne "ID")
                for (int col = 1; col < tableModel.getColumnCount(); col++) {
                    PdfPCell cell = new PdfPCell(new Phrase(tableModel.getColumnName(col)));
                    pdfTable.addCell(cell);
                }

                // Contenu du tableau (en excluant la colonne "ID")
                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    for (int col = 1; col < tableModel.getColumnCount(); col++) {
                        PdfPCell cell = new PdfPCell(new Phrase(tableModel.getValueAt(row, col).toString()));
                        pdfTable.addCell(cell);
                    }
                }

                document.add(pdfTable);
                document.close();

                JOptionPane.showMessageDialog(frame, "Le PDF a été généré avec succès et enregistré dans " + fileToSave.getAbsolutePath(), "PDF généré", JOptionPane.INFORMATION_MESSAGE);
            } catch (DocumentException | IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Une erreur s'est produite lors de la génération du PDF.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }



}
