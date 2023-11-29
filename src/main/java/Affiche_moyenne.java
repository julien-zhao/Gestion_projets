import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicOptionPaneUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Affiche_moyenne {
    private static Connection connection;
    public static DefaultTableModel tableModel;
    private JFrame frame;


    public Affiche_moyenne() {
        establishDatabaseConnection();
        initializeTableModel();
        createAndShowFrame();
        setIcons();
        addGeneratePdfAndExportButtons();
        afficherTableauMoyennes();

    }










    private void establishDatabaseConnection() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_projets", "root", "root");
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
    }

    private void initializeTableModel() {
        tableModel = new DefaultTableModel();
    }

    private void createAndShowFrame() {
        frame = new JFrame("Tableau des moyennes");
        frame.setLayout(new BorderLayout());

        JTable studentTable = createAndConfigureStudentTable();  // Use the method to create and configure the JTable
        JScrollPane tableScrollPane = new JScrollPane(studentTable);
        tableScrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(15, 20, 0, 20),
                BorderFactory.createLineBorder(new Color(108, 190, 213), 2, true)
        ));
        frame.add(tableScrollPane, BorderLayout.CENTER);

        frame.setSize(1000, 700);

        // Center the frame on the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(x, y);

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }



    private JTable createAndConfigureStudentTable() {

        JTable studentTable = new JTable(tableModel);
        studentTable.setShowGrid(false);

        JTableHeader header = studentTable.getTableHeader();
        header.setBackground(new Color(108, 190, 213));
        header.setForeground(Color.WHITE); // 设置列头前景颜色
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 26));

        studentTable.setFont(new Font("Arial", Font.PLAIN, 12));
        studentTable.setRowHeight(23);

        studentTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (row % 2 == 0) {
                    c.setBackground(new Color(240, 240, 240));
                } else {
                    c.setBackground(Color.WHITE);
                }
                if (isSelected) {
                    c.setBackground(new Color(173, 216, 230));
                }
                return c;
            }
        });

        return studentTable;
    }


    public void afficherTableauMoyennes() {
        // Ajouter les colonnes nécessaires
        tableModel.addColumn("Nom Étudiant");

        // Récupérer la liste des projets
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSetProjets = statement.executeQuery("SELECT numero, nom_matiere FROM Projets");

            List<Integer> projectNumbers = new ArrayList<>();

            while (resultSetProjets.next()) {
                int numeroProjet = resultSetProjets.getInt("numero");
                String nomProjet = resultSetProjets.getString("nom_matiere");
                tableModel.addColumn(nomProjet + " Moyenne");
                projectNumbers.add(numeroProjet);
            }

            tableModel.addColumn("Moyenne Finale");
            // Récupérer la liste des étudiants
            ResultSet resultSetEtudiants = statement.executeQuery("SELECT numero FROM Etudiants");

            while (resultSetEtudiants.next()) {
                int etudiantId = resultSetEtudiants.getInt("numero");
                String nomEtudiant = getNomPrenomEtudiant(etudiantId);

                // Ajouter les données des notes pour chaque projet
                Object[] rowData = new Object[]{nomEtudiant};

                double totalMoyenne = 0;

                for (int projectNumber : projectNumbers) {
                    double noteRapport = getNoteRapport(etudiantId, projectNumber);
                    double noteSoutenance = getNoteSoutenance(etudiantId, projectNumber);
                    Date dateEffective = getDateRemiseEffective(etudiantId,projectNumber);
                    Date dateRemise = getDateRemiseProjet(projectNumber);

                    int joursDeRetard = calculateDaysOfDelay(dateRemise, dateEffective);

                    double moyenne = (noteRapport + noteSoutenance) / 2;

                    if (joursDeRetard > 0) {
                        moyenne -= (joursDeRetard * 0.5);
                    }
                    moyenne = Math.max(0, moyenne);

                    // Ajouter la moyenne au tableau
                    rowData = addElementToArray(rowData, moyenne);

                    // Ajouter la moyenne au total
                    totalMoyenne += moyenne;
                }

                // Calculer et ajouter la moyenne finale au tableau
                double moyenneFinale = totalMoyenne / projectNumbers.size();
                moyenneFinale = roundToThreeDecimals(moyenneFinale);
                rowData = addElementToArray(rowData, moyenneFinale);

                // Ajouter la ligne de données au modèle du tableau
                tableModel.addRow(rowData);



            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }





    private void setIcons() {
        ImageIcon customIcon = new ImageIcon("src/Picture/logo_D.jpg");
        frame.setIconImage(customIcon.getImage());
    }

    private void addGeneratePdfAndExportButtons() {
        JButton generatePdfButton = new JButton("Générer PDF");
        generatePdfButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generatePDF(tableModel);
            }
        });

        JButton exportExcelButton = new JButton("Exporter vers Excel");
        exportExcelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportToExcel(tableModel);
            }
        });

        JButton generateGraphButton = new JButton("Générer un graphique");
        generateGraphButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Appeler la méthode pour générer le graphique
                generateGraph();
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(generatePdfButton);
        buttonPanel.add(exportExcelButton);
        buttonPanel.add(generateGraphButton);  // Ajouter le nouveau bouton
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        frame.add(buttonPanel, BorderLayout.SOUTH);

    }

    private double getNoteRapport(int etudiantId, int projectNumber) throws SQLException {
        String tableName = "project_" + projectNumber;
        String query = "SELECT note_rapport FROM " + tableName + " WHERE etudiant1_numero = ? OR etudiant2_numero = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, etudiantId);
            preparedStatement.setInt(2, etudiantId);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next() ? resultSet.getDouble("note_rapport") : 0;
        }
    }

    private double getNoteSoutenance(int etudiantId, int projectNumber) throws SQLException {
        String tableName = "project_" + projectNumber;
        String query = "SELECT CASE WHEN etudiant1_numero = ? THEN note_soutenance_etu1 ELSE note_soutenance_etu2 END AS note_soutenance FROM " + tableName + " WHERE etudiant1_numero = ? OR etudiant2_numero = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, etudiantId);
            preparedStatement.setInt(2, etudiantId);
            preparedStatement.setInt(3, etudiantId);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next() ? resultSet.getDouble("note_soutenance") : 0;
        }
    }

    private Date getDateRemiseEffective(int etudiantId, int projectNumber) throws SQLException {
        String tableName = "project_" + projectNumber;
        String query = "SELECT date_remise_effective FROM " + tableName + " WHERE etudiant1_numero = ? OR etudiant2_numero = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, etudiantId);
            preparedStatement.setInt(2, etudiantId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Date dateRemiseEffective = resultSet.getDate("date_remise_effective");
                return dateRemiseEffective != null ? dateRemiseEffective : parseDate("0000-00-00");
            } else {
                return parseDate("0000-00-00");
            }
        }
    }

    private double roundToThreeDecimals(double value) {
        DecimalFormat decimalFormat = new DecimalFormat("#.###");
        return Double.parseDouble(decimalFormat.format(value));
    }

    private Date parseDate(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return new Date(dateFormat.parse(dateString).getTime());
        } catch (ParseException e) {
            e.printStackTrace();  // Gérer l'erreur de format de date si nécessaire
            return null;
        }
    }

    private Date getDateRemiseProjet(int projectNumber) throws SQLException {
        String query = "SELECT date_remise FROM Projets WHERE numero = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, projectNumber);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next() ? resultSet.getDate("date_remise") : null;
        }
    }

    // Méthode pour calculer le nombre de jours de retard
    private int calculateDaysOfDelay(Date dateRemise, Date dateRemiseEffective) {
        try {
            long diff = dateRemiseEffective.getTime() - dateRemise.getTime();
            return Math.max(0, (int) (diff / (1000 * 60 * 60 * 24)));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private Object[] addElementToArray(Object[] array, Object element) {
        Object[] newArray = new Object[array.length + 1];
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = element;
        return newArray;
    }

    // Méthode pour récupérer les noms et prénoms des étudiants
    private String getNomPrenomEtudiant(int etudiantId) {
        try {
            String query = "SELECT nom, prenom FROM Etudiants WHERE numero = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, etudiantId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String nom = resultSet.getString("nom");
                String prenom = resultSet.getString("prenom");
                return nom + " " + prenom;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // generer un pdf
    private void generatePDF(DefaultTableModel noteTableModel) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Enregistrer le PDF");

        FileNameExtensionFilter filter = new FileNameExtensionFilter("Fichiers PDF (*.pdf)", "pdf");
        fileChooser.setFileFilter(filter);

        int userSelection = fileChooser.showSaveDialog(frame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                File fileToSave = fileChooser.getSelectedFile();

                if (!fileToSave.getName().toLowerCase().endsWith(".pdf")) {
                    fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".pdf");
                }

                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(fileToSave));
                document.open();

                document.add(new Paragraph("Liste des moyennes des étudiants" ));
                document.add(new Paragraph("\n"));

                PdfPTable pdfTable = new PdfPTable(noteTableModel.getColumnCount());

                for (int col = 0; col < noteTableModel.getColumnCount(); col++) {
                    PdfPCell cell = new PdfPCell(new Phrase(noteTableModel.getColumnName(col)));
                    pdfTable.addCell(cell);
                }

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

    private void exportToExcel(DefaultTableModel model) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Enregistrer le fichier Excel");

        FileNameExtensionFilter filter = new FileNameExtensionFilter("Fichiers Excel (*.xlsx)", "xlsx");
        fileChooser.setFileFilter(filter);

        int userSelection = fileChooser.showSaveDialog(frame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            if (!fileToSave.getName().toLowerCase().endsWith(".xlsx")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".xlsx");
            }

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Données");

            // En-têtes
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < model.getColumnCount(); col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(model.getColumnName(col));
            }

            // Données
            for (int row = 0; row < model.getRowCount(); row++) {
                Row dataRow = sheet.createRow(row + 1);
                for (int col = 0; col < model.getColumnCount(); col++) {
                    Cell cell = dataRow.createCell(col);
                    cell.setCellValue(model.getValueAt(row, col).toString());
                }
            }

            // Écrire le fichier Excel
            try (FileOutputStream fileOut = new FileOutputStream(fileToSave)) {
                workbook.write(fileOut);
                fileOut.flush();
                fileOut.close();
                workbook.close();
                JOptionPane.showMessageDialog(frame, "Les données ont été exportées avec succès vers " + fileToSave.getAbsolutePath(), "Export Excel réussi", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Une erreur s'est produite lors de l'exportation vers Excel.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }





    private void generateGraph() {
        // Créer un ensemble de données de catégorie
        CategoryDataset dataset = createDataset();

        // Créer un graphique à barres
        JFreeChart barChart = ChartFactory.createBarChart(
                "Moyennes des étudiants",
                "Étudiants",
                "Moyennes",
                dataset
        );

        // Afficher le graphique dans une fenêtre
        JFrame chartFrame = new JFrame("Graphique des moyennes");
        chartFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(560, 370));
        chartFrame.setContentPane(chartPanel);
        chartFrame.pack();
        chartFrame.setVisible(true);
    }



    private CategoryDataset createDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Ajouter les moyennes des étudiants au jeu de données
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            String etudiant = tableModel.getValueAt(row, 0).toString();
            double moyenne = Double.parseDouble(tableModel.getValueAt(row, tableModel.getColumnCount() - 1).toString());
            dataset.addValue(moyenne, "Étudiants", etudiant);
        }

        return dataset;
    }


}