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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.text.Collator;
import java.util.Comparator;

public class Gestion_binome {
    private static Connection connection = null;
    private static DefaultTableModel tableModel;
    JFrame frame;
    int projectNumber;

    public Gestion_binome(int projectNumber) {
        this.projectNumber= projectNumber;

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_projets", "root", "root");
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        frame = new JFrame("Gestion des Binômes du projet : " + getProjectName(projectNumber));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1200, 600));
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Création du tableau pour afficher la liste des binômes
        String[] columnNames = {"ID", "Projet", "Étudiant 1", "Étudiant 2", "Note Rapport", "Note Soutenance Étudiant 1", "Note Soutenance Étudiant 2", "Date de Remise Effective"};
        tableModel = new DefaultTableModel(columnNames, 0);

        // Création du panneau principal
        JTable binomeTable = new JTable(tableModel);
        // Activez le tri sur le tableau
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        binomeTable.setRowSorter(sorter);

        // Pour trier les colonnes en ignorant la casse (insensible à la casse)
        TableRowSorter<DefaultTableModel> caseInsensitiveSorter = new TableRowSorter<>(tableModel) {
            @Override
            public Comparator<?> getComparator(int column) {
                if (column == 1 || column == 2 || column == 3 || column == 4 || column == 5 || column == 6 || column == 7 || column == 8) {
                    return Collator.getInstance();
                }
                return super.getComparator(column);
            }
        };
        binomeTable.setRowSorter(caseInsensitiveSorter);
        JScrollPane tableScrollPane = new JScrollPane(binomeTable);

        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Recherche rapide"); // Astuce pour afficher un texte d'infobulle


        // Création des boutons pour ajouter et supprimer des binômes
        JButton addBinomeButton = new JButton("Ajouter Binôme");
        JButton deleteBinomeButton = new JButton("Supprimer Binôme");
        JButton afficheNoteButton = new JButton("Affiche note");
        JButton generatePDFButton = new JButton("Générer en PDF"); // Bouton pour générer le PDF
        JButton retourProjectButton = new JButton("Retour au Project");// Bouton "Retour au Project"

        // On masque la colonne ID
        TableColumnModel tableColumnModel = binomeTable.getColumnModel();
        tableColumnModel.getColumn(0).setMaxWidth(0);
        tableColumnModel.getColumn(0).setMinWidth(0);
        tableColumnModel.getColumn(0).setPreferredWidth(0);
        tableColumnModel.getColumn(0).setResizable(false);
        tableColumnModel.getColumn(1).setMaxWidth(0);
        tableColumnModel.getColumn(1).setMinWidth(0);
        tableColumnModel.getColumn(1).setPreferredWidth(0);
        tableColumnModel.getColumn(1).setResizable(false);


        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterTable(searchField.getText(), binomeTable);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTable(searchField.getText(), binomeTable);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Les mises à jour d'attributs ne sont pas traitées ici
            }
        });


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
        buttonPanel.add(afficheNoteButton);
        buttonPanel.add(generatePDFButton); // Ajoutez le bouton "Générer en PDF"
        buttonPanel.add(retourProjectButton);


        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Recherche : "));
        searchPanel.add(searchField);

        mainPanel.add(searchPanel, BorderLayout.NORTH);
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

                    int confirmation = JOptionPane.showConfirmDialog(frame, "Êtes-vous sûr de vouloir supprimer ce binôme ?", "Confirmation de suppression", JOptionPane.YES_NO_OPTION);
                    if (confirmation == JOptionPane.YES_OPTION) {
                        deleteBinome(binomeIdToDelete);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Veuillez sélectionner un binôme à supprimer.", "Avertissement", JOptionPane.WARNING_MESSAGE);
                }
            }
        });



        afficheNoteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //JFrame frame;
                new Affiche_note(tableModel,projectNumber);
            }
        });

        // Ajoutez des gestionnaires d'événements de modification de cellule personnalisés pour les colonnes de notes
        binomeTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(new JTextField()) {
            @Override
            public boolean stopCellEditing() {
                try {
                    String value = (String) getCellEditorValue();
                    // Vérifiez si la valeur est un nombre valide en utilisant Double.parseDouble
                    Double.parseDouble(value);
                    return super.stopCellEditing();
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(frame, "Veuillez saisir un nombre valide.", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
                    return false; // Empêche la fin de l'édition
                }
            }
        });
        binomeTable.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(new JTextField()) {
            @Override
            public boolean stopCellEditing() {
                try {
                    String value = (String) getCellEditorValue();
                    // Vérifiez si la valeur est un nombre valide en utilisant Double.parseDouble
                    Double.parseDouble(value);
                    return super.stopCellEditing();
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(frame, "Veuillez saisir un nombre valide.", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
                    return false; // Empêche la fin de l'édition
                }
            }
        });
        binomeTable.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(new JTextField()) {
            @Override
            public boolean stopCellEditing() {
                try {
                    String value = (String) getCellEditorValue();
                    // Vérifiez si la valeur est un nombre valide en utilisant Double.parseDouble
                    Double.parseDouble(value);
                    return super.stopCellEditing();
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(frame, "Veuillez saisir un nombre valide.", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
                    return false; // Empêche la fin de l'édition
                }
            }
        });



// Ajoutez des gestionnaires d'événements de modification de cellule personnalisés pour les colonnes de notes
        DefaultCellEditor numberCellEditor = new DefaultCellEditor(new JTextField()) {
            @Override
            public boolean stopCellEditing() {
                try {
                    String value = (String) getCellEditorValue();
                    double parsedValue = Double.parseDouble(value);
                    if (parsedValue >= 0 && parsedValue <= 20) {
                        return super.stopCellEditing();
                    } else {
                        JOptionPane.showMessageDialog(frame, "La note doit être entre 0 et 20.", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
                        return false; // Empêche la fin de l'édition
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(frame, "Veuillez saisir un nombre valide.", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
                    return false; // Empêche la fin de l'édition
                }
            }
        };
        binomeTable.getColumnModel().getColumn(4).setCellEditor(numberCellEditor); // Colonne "Note Rapport"
        binomeTable.getColumnModel().getColumn(5).setCellEditor(numberCellEditor); // Colonne "Note Soutenance Étudiant 1"
        binomeTable.getColumnModel().getColumn(6).setCellEditor(numberCellEditor); // Colonne "Note Soutenance Étudiant 2

        binomeTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int column = e.getColumn();
                    DefaultTableModel model = (DefaultTableModel) e.getSource();
                    int binomeId = (int) model.getValueAt(row, 0);
                    String columnName = binomeTable.getColumnName(column);
                    Object updatedValue = model.getValueAt(row, column);



                    // Ensuite, mettez à jour la base de données en fonction de la colonne modifiée
                    if (columnName.equals("Note Rapport")) {
                        // Mettez à jour la note de rapport dans la base de données
                        updateNoteRapport(binomeId, updatedValue);
                    } else if (columnName.equals("Note Soutenance Étudiant 1")) {
                        // Mettez à jour la note de soutenance de l'étudiant 1 dans la base de données
                        updateNoteSoutenanceEtu1(binomeId, updatedValue);
                    } else if (columnName.equals("Note Soutenance Étudiant 2")) {
                        // Mettez à jour la note de soutenance de l'étudiant 2 dans la base de données
                        updateNoteSoutenanceEtu2(binomeId, updatedValue);
                    }
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

    }


    // Méthodes pour mettre à jour les colonnes de notes
    private void updateNoteRapport(int binomeId, Object updatedValue) {
        // Mettez à jour la note de rapport dans la base de données
        try {
            String tableName = "Project_" + projectNumber;
            String updateSql = "UPDATE " + tableName + " SET note_rapport = ? WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(updateSql);
            preparedStatement.setObject(1, updatedValue);
            preparedStatement.setInt(2, binomeId);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Erreur lors de la mise à jour des données : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateNoteSoutenanceEtu1(int binomeId, Object updatedValue) {
        // Mettez à jour la note de soutenance de l'étudiant 1 dans la base de données
        try {
            String tableName = "Project_" + projectNumber;
            String updateSql = "UPDATE " + tableName + " SET note_soutenance_etu1 = ? WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(updateSql);
            preparedStatement.setObject(1, updatedValue);
            preparedStatement.setInt(2, binomeId);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Erreur lors de la mise à jour des données : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateNoteSoutenanceEtu2(int binomeId, Object updatedValue) {
        // Mettez à jour la note de soutenance de l'étudiant 2 dans la base de données
        try {
            String tableName = "Project_" + projectNumber;
            String updateSql = "UPDATE " + tableName + " SET note_soutenance_etu2 = ? WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(updateSql);
            preparedStatement.setObject(1, updatedValue);
            preparedStatement.setInt(2, binomeId);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Erreur lors de la mise à jour des données : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void deleteBinome(int binomeId) {
        try {
            String tableName = "Project_" + projectNumber;
            String deleteSql = "DELETE FROM " + tableName + " WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(deleteSql);
            preparedStatement.setInt(1, binomeId);
            preparedStatement.executeUpdate();

            tableModel.removeRow(getRowIndexById(binomeId));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Erreur lors de la suppression du binôme : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getRowIndexById(int binomeId) {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            int id = (int) tableModel.getValueAt(row, 0);
            if (id == binomeId) {
                return row;
            }
        }
        return -1; // Si l'ID du binôme n'est pas trouvé
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
            String query = "SELECT nom, prenom FROM Etudiants WHERE numero = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, studentId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String nom = resultSet.getString("nom");
                String prenom = resultSet.getString("prenom");
                return nom + " " + prenom;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return "";
    }


    private void filterTable(String searchText, JTable binomeTable) {
        RowFilter<DefaultTableModel, Object> rowFilter = RowFilter.regexFilter("(?i)" + searchText, 1,2, 3,4,5,6,7,8);
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) binomeTable.getRowSorter();
        sorter.setRowFilter(rowFilter);
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
                document.add(new Paragraph("Liste des étudiants"));
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
