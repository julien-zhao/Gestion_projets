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

        // Activez le tri sur le tableau
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        studentTable.setRowSorter(sorter);

        // Pour trier les colonnes en ignorant la casse (insensible à la casse)
        TableRowSorter<DefaultTableModel> caseInsensitiveSorter = new TableRowSorter<>(tableModel) {
            @Override
            public Comparator<?> getComparator(int column) {
                if (column == 1 || column == 2 || column == 3 || column == 4) { // Les colonnes 1, 2, 3 et 4 sont celles qui doivent être triées de manière insensible à la casse
                    return Collator.getInstance();
                }
                return super.getComparator(column);
            }
        };
        studentTable.setRowSorter(caseInsensitiveSorter);

        // Création du champ de recherche rapide
        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Recherche rapide"); // Astuce pour afficher un texte d'infobulle


        JScrollPane tableScrollPane = new JScrollPane(studentTable);

        // Création des boutons pour ajouter et supprimer des étudiants
        JButton addStudentButton = new JButton("Ajouter Étudiant");
        JButton deleteStudentButton = new JButton("Supprimer Étudiant");
        JButton generatePDFButton = new JButton("Générer en PDF"); // Bouton pour générer le PDF
        JButton retourMenuButton = new JButton("Retour au Menu");

        TableColumnModel tableColumnModel = studentTable.getColumnModel();
        tableColumnModel.getColumn(0).setMaxWidth(0);
        tableColumnModel.getColumn(0).setMinWidth(0);
        tableColumnModel.getColumn(0).setPreferredWidth(0);
        tableColumnModel.getColumn(0).setResizable(false);

        retourMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Fermez l'interface de gestion des binômes et affichez l'interface de menu
                frame.dispose(); // Ferme l'interface de gestion des binômes
                new Menu(); // Crée une nouvelle instance de l'interface de menu
            }
        });

        // Ajout des composants au panneau principal
        JPanel studentFormPanel = new JPanel(new GridLayout(6, 2));
        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Recherche : "));
        searchPanel.add(searchField);

        // Ajout du panneau de recherche au panneau principal
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(studentFormPanel, BorderLayout.EAST);

        // Créez un panneau pour les boutons "Ajouter Etudiant" et "Retour au Menu"
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addStudentButton);
        buttonPanel.add(deleteStudentButton);
        buttonPanel.add(generatePDFButton); // Ajoutez le bouton "Générer en PDF"
        buttonPanel.add(retourMenuButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Gestionnaire d'événements pour le champ de recherche rapide
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterTable(searchField.getText(),studentTable);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTable(searchField.getText(),studentTable);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Les mises à jour d'attributs ne sont pas traitées ici
            }
        });

        JTabbedPane projectTabs = new JTabbedPane();
        frame.add(projectTabs, BorderLayout.CENTER);

        frame.add(mainPanel);
        frame.pack();
        frame.setVisible(true);
        loadStudentsFromDatabase();



// Gestionnaire d'événements pour le bouton "Ajouter Étudiant"
        addStudentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new StudentAddDialog(connection,frame,tableModel);
            }
        });


        // Gestionnaire d'événements pour le bouton "Supprimer Étudiant"
        // Gestionnaire d'événements pour le bouton "Supprimer Étudiant"
        deleteStudentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = studentTable.getSelectedRow();
                if (selectedRow != -1) {
                    int studentIdToDelete = (int) tableModel.getValueAt(selectedRow, 0);

                    // Demandez une confirmation avant de supprimer l'étudiant
                    int choice = JOptionPane.showConfirmDialog(frame, "Êtes-vous sûr de vouloir supprimer cet étudiant ?", "Confirmation", JOptionPane.YES_NO_OPTION);

                    if (choice == JOptionPane.YES_OPTION) {
                        try {
                            String deleteSql = "DELETE FROM Etudiants WHERE numero = ?";
                            PreparedStatement preparedStatement = connection.prepareStatement(deleteSql);
                            preparedStatement.setInt(1, studentIdToDelete);
                            preparedStatement.executeUpdate();
                            tableModel.removeRow(selectedRow);
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Veuillez sélectionner un étudiant à supprimer.", "Avertissement", JOptionPane.WARNING_MESSAGE);
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

    // Fonction pour filtrer le tableau en fonction du texte de recherche
    private void filterTable(String searchText,JTable studentTable) {
        RowFilter<DefaultTableModel, Object> rowFilter = RowFilter.regexFilter("(?i)" + searchText, 1, 2, 3, 4);
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) studentTable.getRowSorter();
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
