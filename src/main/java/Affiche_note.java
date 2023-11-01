import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Affiche_note {
    private static Affiche_note instance = null;

    private static Connection connection;
    private DefaultTableModel tableModel;
    private int projectNumber;
    private String projectName;
    JFrame frame;

    Affiche_note(DefaultTableModel tableModel, int projectNumber) {
        this.tableModel = tableModel;
        this.projectNumber = projectNumber;

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_projets", "root", "root");
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        // Récupérer le nom du projet à partir de la base de données
        projectName = getProjectName(projectNumber);

        // Créer un modèle de table pour afficher les notes
        DefaultTableModel noteTableModel = new DefaultTableModel();
        noteTableModel.addColumn("Étudiant");
        noteTableModel.addColumn("Note rapport");
        noteTableModel.addColumn("Note soutenance");
        noteTableModel.addColumn("Jours de retard");
        noteTableModel.addColumn("Note finale");

        // Calculer et ajouter les notes au modèle de table
        calculateAndDisplayNotes(noteTableModel);

        // Créer une table avec le modèle de table des notes
        JTable noteTable = new JTable(noteTableModel);

        // Créer un panneau avec une barre de défilement pour afficher la table
        JScrollPane scrollPane = new JScrollPane(noteTable);

        JButton generatePDFButton = new JButton("Générer en PDF"); // Bouton pour générer le PDF
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(generatePDFButton); // Ajoutez le bouton "Générer en PDF"

        // Gestionnaire d'événements pour le bouton "Générer en PDF"
        generatePDFButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generatePDF(noteTableModel);
            }
        });

        // Créer une fenêtre pour afficher la table
        frame = new JFrame("Liste des notes des étudiants dans le projet " + projectName);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setPreferredSize(new Dimension(600, 400));
        frame.add(scrollPane);
        frame.add(buttonPanel, BorderLayout.SOUTH); // Ajoutez le bouton "Générer en PDF" en bas
        frame.pack();
        frame.setVisible(true);
    }



    private void calculateAndDisplayNotes(DefaultTableModel noteTableModel) {
        // Parcourez chaque ligne du tableau et calculez la note finale en fonction du retard
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            String etudiant1 = tableModel.getValueAt(row, 2).toString();
            String etudiant2 = tableModel.getValueAt(row, 3).toString();

            double soutenance1 = Double.parseDouble(tableModel.getValueAt(row, 5).toString());
            double soutenance2 = Double.parseDouble(tableModel.getValueAt(row, 6).toString());
            double rapport = Double.parseDouble(tableModel.getValueAt(row, 4).toString());
            String dateRemiseEffective = tableModel.getValueAt(row, 7).toString();
            Date dateRemise = Date.valueOf(getDateRemise(projectNumber));

            double noteFinale1 = (rapport + soutenance1) / 2;
            double noteFinale2 = (rapport + soutenance2) / 2;

            // Calculer la différence entre date_remise_effective et date_remise
            int joursDeRetard = calculateDaysOfDelay(dateRemise, Date.valueOf(dateRemiseEffective));

            // Réduire la note finale de 0.5 si un jour de retard est présent
            if (joursDeRetard > 0) {
                noteFinale1 -= (joursDeRetard * 0.5);
                noteFinale2 -= (joursDeRetard * 0.5);
            }

            // Assurez-vous que la note finale n'est pas inférieure à 0
            noteFinale1 = Math.max(0, noteFinale1);
            noteFinale2 = Math.max(0, noteFinale2);

            // Ajouter les notes au modèle de table des notes
            noteTableModel.addRow(new Object[]{etudiant1, rapport,soutenance1, joursDeRetard, noteFinale1});
            noteTableModel.addRow(new Object[]{etudiant2,rapport, soutenance2, joursDeRetard, noteFinale2});
        }
    }

    private String getProjectName(int projectNumber) {
        try {
            String query = "SELECT nom_matiere FROM Projets WHERE numero = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, projectNumber);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("nom_matiere");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void generatePDF(DefaultTableModel noteTableModel) {
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
                document.add(new Paragraph("Liste des notes des étudiants dans le projet " + projectName));
                document.add(new Paragraph("\n")); // Ajout d'un paragraphe vide (saut de ligne)

                // Créez une table PDF avec une colonne de moins que le modèle de table
                PdfPTable pdfTable = new PdfPTable(noteTableModel.getColumnCount());

                // En-têtes de colonne
                for (int col = 0; col < noteTableModel.getColumnCount(); col++) {
                    PdfPCell cell = new PdfPCell(new Phrase(noteTableModel.getColumnName(col)));
                    pdfTable.addCell(cell);
                }

                // Contenu du tableau
                for (int row = 0; row < noteTableModel.getRowCount(); row++) {
                    for (int col = 0; col < noteTableModel.getColumnCount(); col++) {
                        PdfPCell cell = new PdfPCell(new Phrase(noteTableModel.getValueAt(row, col).toString()));
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


    private static String getDateRemise(int projectNumber) {
        try {
            String query = "SELECT date_remise FROM Projets WHERE numero = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, projectNumber);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("date_remise");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return "";
    }


    // Méthode pour calculer la différence en jours entre deux dates
    private int calculateDaysOfDelay(java.sql.Date dateRemise, java.sql.Date dateRemiseEffective) {
        try {
            long diff = dateRemiseEffective.getTime() - dateRemise.getTime();
            return (int) (diff / (1000 * 60 * 60 * 24));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }



}
