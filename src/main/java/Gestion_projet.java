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
import javax.swing.table.*;
import javax.swing.RowFilter;
import java.awt.*;
import java.awt.event.*;
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
        establishDatabaseConnection();

        setupMainFrame();

        setIcons();

        String[] columnNames = {"Numero", "Nom Matière", "Sujet", "Date de Remise"};
        tableModel = new DefaultTableModel(columnNames, 0);
        projectTable = new JTable(tableModel);

        projectTable.setShowGrid(false);

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


        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Recherche rapide");


        JScrollPane tableScrollPane = new JScrollPane(projectTable);

        JButton addProjectButton = new JButton("Ajouter Projet");
        JButton deleteProjectButton = new JButton("Supprimer Projet");
        JButton gestionBinomeButton = new JButton("Gestion binôme");
        JButton generatePDFButton = new JButton("Générer en PDF");
        JButton retourMenuButton = new JButton("Retour au Menu");

        TableColumnModel tableColumnModel = projectTable.getColumnModel();
        tableColumnModel.getColumn(0).setMaxWidth(0);
        tableColumnModel.getColumn(0).setMinWidth(0);
        tableColumnModel.getColumn(0).setPreferredWidth(0);
        tableColumnModel.getColumn(0).setResizable(false);


        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        LogoDauphine logoDauphine = new LogoDauphine();
        logoDauphine.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 20));
//        mainPanel.add(logoDauphine);



        JPanel searchPanel = new JPanel();
        searchPanel.add(logoDauphine);
        searchPanel.add(new JLabel("Recherche : "));
        searchPanel.add(searchField);
        mainPanel.add(searchPanel);

//        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        buttonPanel.add(addProjectButton);
//        buttonPanel.add(deleteProjectButton);
//        buttonPanel.add(gestionBinomeButton);
//        buttonPanel.add(generatePDFButton);
////        buttonPanel.add(new JLabel("            ")); //添加占位的空标签
//        buttonPanel.add(retourMenuButton);
//        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 20));

        // 创建面板并设置布局
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // 添加按钮到面板
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        buttonPanel.add(addProjectButton, gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 0, 0, 20);
        buttonPanel.add(deleteProjectButton, gbc);

        gbc.gridx = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        buttonPanel.add(gestionBinomeButton, gbc);

        gbc.gridx = 3;
        gbc.insets = new Insets(0, 0, 0, 20);
        buttonPanel.add(generatePDFButton, gbc);

        gbc.gridx = 4;
        gbc.insets = new Insets(0, 0, 0, 0);
        buttonPanel.add(retourMenuButton, gbc);

        mainPanel.add(tableScrollPane);
        mainPanel.add(buttonPanel);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 20));


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
            }
        });

        frame.add(mainPanel);
        frame.pack();
        frame.setVisible(true);
        loadProjectsFromDatabase();


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
                    int modelRow = projectTable.convertRowIndexToModel(selectedRow);
                    int projectNumber = getPrimaryKeyValueFromSelectedRow();

                    if (projectNumber != -1) {

                        if (areBinomesAssociated(projectNumber)) {

                            String projectName = (String) tableModel.getValueAt(modelRow, getColumnIndex("Nom Matière"));


                            if (confirmBinomeDeletion(projectName)) {

                                if (deleteBinomesForProject(projectNumber)) {

                                    deleteProject(modelRow);
                                    // Afficher le message de confirmation
                                    JOptionPane.showMessageDialog(frame, "Le projet a été supprimé avec succès.", "Confirmation", JOptionPane.INFORMATION_MESSAGE);
                                } else {

                                    JOptionPane.showMessageDialog(frame, "La suppression des binômes a échoué.", "Erreur", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        } else {

                            if (confirmProjectDeletion() && deleteBinomesForProject(projectNumber)) {
                                deleteProject(selectedRow);
                                // Afficher le message de confirmation
                                JOptionPane.showMessageDialog(frame, "Le projet a été supprimé avec succès.", "Confirmation", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Veuillez sélectionner un projet à supprimer.", "Avertissement", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        generatePDFButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generatePDF();
            }
        });


        gestionBinomeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int selectedRow = projectTable.getSelectedRow();
                if (selectedRow != -1) {

                    int selectedProjectNumber = getPrimaryKeyValueFromSelectedRow();
                    new Gestion_binome(selectedProjectNumber);
                } else {

                    JOptionPane.showMessageDialog(frame, "Sélectionnez un projet avant de gérer les binômes.", "Avertissement", JOptionPane.WARNING_MESSAGE);
                }
            }
        });


        retourMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                new Menu();
            }
        });


        JTableHeader header = projectTable.getTableHeader();
        header.setBackground(new Color(108, 190, 213));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 26));

        projectTable.setFont(new Font("Arial", Font.PLAIN, 12));
        projectTable.setRowHeight(23);

        projectTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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

                ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);

                return c;
            }
        });

    }


    private void establishDatabaseConnection() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_projets", "root", "root");
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
    }

    private void setIcons() {
        ImageIcon customIcon = new ImageIcon("src/Picture/logo_D.jpg");
        frame.setIconImage(customIcon.getImage());
    }


    private void setupMainFrame() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1000, 700));
        frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(x, y);
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

    private int getPrimaryKeyValueFromSelectedRow() {
        int projectNumber = -1;

        int selectedRow = projectTable.getSelectedRow();
        if (selectedRow != -1) {
            int modelRow = projectTable.convertRowIndexToModel(selectedRow);
            String matiere = (String) tableModel.getValueAt(modelRow, getColumnIndex("Nom Matière"));
            String sujet = (String) tableModel.getValueAt(modelRow, getColumnIndex("Sujet"));
            String dateRemise = (String) tableModel.getValueAt(modelRow, getColumnIndex("Date de Remise"));

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

        int modelRow = projectTable.convertRowIndexToModel(selectedRow);
        int projectNumber = getPrimaryKeyValueFromSelectedRow();
        if (projectNumber != -1) {

            deleteNotesForProject(projectNumber);

            String matiere = (String) tableModel.getValueAt(modelRow, getColumnIndex("Nom Matière"));
            String sujet = (String) tableModel.getValueAt(modelRow, getColumnIndex("Sujet"));
            String dateRemise = (String) tableModel.getValueAt(modelRow, getColumnIndex("Date de Remise"));

            try {

                String deleteSql = "DELETE FROM Projets WHERE numero = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(deleteSql);
                preparedStatement.setInt(1, projectNumber);
                preparedStatement.executeUpdate();

                tableModel.removeRow(modelRow);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

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

    private boolean confirmBinomeDeletion(String projectName) {
        int option;
        if (areBinomesAssociated(getPrimaryKeyValueFromSelectedRow())) {

            option = JOptionPane.showConfirmDialog(frame, "Il y a des binômes associés au projet '" + projectName + "'. La suppression de ce projet entraînera également la suppression de tous les binômes associés. Voulez-vous continuer ?", "Avertissement", JOptionPane.YES_NO_OPTION);
        } else {

            option = JOptionPane.showConfirmDialog(frame, "Voulez-vous vraiment supprimer ce projet ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        }
        return option == JOptionPane.YES_OPTION;
    }

    private boolean confirmProjectDeletion() {
        int option = JOptionPane.showConfirmDialog(frame, "Voulez-vous vraiment supprimer ce projet ? (Aucun binôme n'est associé à ce projet)", "Confirmation", JOptionPane.YES_NO_OPTION);
        return option == JOptionPane.YES_OPTION;
    }


    private void generatePDF() {
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


                document.add(new Paragraph("Liste des projets"));
                document.add(new Paragraph("\n"));


                PdfPTable pdfTable = new PdfPTable(tableModel.getColumnCount() - 1);
                pdfTable.setWidthPercentage(100);


                for (int col = 1; col < tableModel.getColumnCount(); col++) {
                    PdfPCell cell = new PdfPCell(new Phrase(tableModel.getColumnName(col)));
                    pdfTable.addCell(cell);
                }

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
