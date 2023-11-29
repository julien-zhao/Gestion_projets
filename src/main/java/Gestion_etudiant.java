import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;
import javax.swing.table.*;



public class Gestion_etudiant{
    private static Connection connection = null;
    private static DefaultTableModel tableModel;
    JPanel mainPanel = new JPanel(new BorderLayout());
    JFrame frame = new JFrame("Gestion des étudiants");
    JTextField searchField = new JTextField(20);
    private JButton addStudentButton;
    private JButton deleteStudentButton;
    private JButton generatePDFButton;
    private JButton afficheMoyenneButton;
    private JButton retourMenuButton;



    Gestion_etudiant(){

        establishDatabaseConnection();

        setupMainFrame();

        setIcons();

        createAndConfigureStudentTable();

        configureButtons();

        configureSearchField();

        JPanel northPanel = configureNorthPanel();

        JPanel southPanel = configureSouthPanel();

        addReturnMenuListener();


        JTable studentTable = createAndConfigureStudentTable();
        JScrollPane tableScrollPane = new JScrollPane(studentTable);
        tableScrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(15, 20, 0, 20), // 设置边框
                BorderFactory.createLineBorder(new Color(108, 190, 213), 2,true) // 使用RGB颜色设置柔和的边框
        ));


        // Ajout des composants au panneau principal
        LogoDauphine logoDauphine = new LogoDauphine();
        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Recherche : "));
        searchPanel.add(searchField);


        TableColumnModel tableColumnModel = studentTable.getColumnModel();
        tableColumnModel.getColumn(0).setMaxWidth(0);
        tableColumnModel.getColumn(0).setMinWidth(0);
        tableColumnModel.getColumn(0).setPreferredWidth(0);
        tableColumnModel.getColumn(0).setResizable(false);

        northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.X_AXIS));
        northPanel.add(logoDauphine);
        northPanel.add(searchPanel);
        northPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 20));  // 调整边缘的距离


        southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        southPanel.add(addStudentButton);
        southPanel.add(deleteStudentButton);
        southPanel.add(generatePDFButton);
        southPanel.add(afficheMoyenneButton);
        southPanel.add(retourMenuButton);
        southPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));  // 调整边缘的距离

        mainPanel.add(northPanel, BorderLayout.NORTH);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.pack();
        frame.setVisible(true);
        loadStudentsFromDatabase();


        JComboBox<String> formationFilter = new JComboBox<>();
        formationFilter.addItem("All");
        formationFilter.addItem("ID");
        formationFilter.addItem("SITN");
        formationFilter.addItem("IF");
        formationFilter.setPreferredSize(new Dimension(10, 25));
        formationFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedFormation = (String) formationFilter.getSelectedItem();
                if (selectedFormation.equals("All")) {
                    selectedFormation = "";
                }
                JTable studentTable = (JTable) ((JScrollPane) mainPanel.getComponent(1)).getViewport().getView();
                filterTable(selectedFormation, studentTable);
            }
        });
        northPanel.add(formationFilter);



        JComboBox<String> promotionFilter = new JComboBox<>();
        promotionFilter.addItem("All");
        promotionFilter.addItem("Initial");
        promotionFilter.addItem("Alternance");
        promotionFilter.addItem("Continue");
        promotionFilter.setPreferredSize(new Dimension(10, 25));
        promotionFilter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedPromotion = (String) promotionFilter.getSelectedItem();
                if (selectedPromotion.equals("All")) {
                    selectedPromotion = "";
                }
                JTable studentTable = (JTable) ((JScrollPane) mainPanel.getComponent(1)).getViewport().getView();
                filterTable(selectedPromotion, studentTable);
            }
        });
        northPanel.add(promotionFilter);



        MouseListener mouseListener = new MouseAdapter() {
            private JDialog dialog;

            @Override
            public void mouseEntered(MouseEvent e) {
                dialog = new JDialog((JFrame) null, "Aide", false);
                int xOffset = 10;
                int yOffset = 150;
                Point componentPosition = e.getComponent().getLocationOnScreen();
                int xPosition = componentPosition.x + xOffset;
                int yPosition = componentPosition.y - dialog.getHeight() - yOffset;
                dialog.setLocation(xPosition, yPosition);
                JLabel label = new JLabel("<html> - Lorsque vous souhaitez supprimer les informations relatives à un élève, vous devez d'abord sélectionner l'élève en cliquant dessus, puis appuyer sur le bouton de suppression.  <br><br> - Si vous avez d'autres questions, veuillez contacter : info@dauphine.eu</html>");
                label.setPreferredSize(new Dimension(300, 130));
                label.setMaximumSize(new Dimension(500, 130));
                dialog.add(label);
                dialog.pack();
                dialog.setVisible(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (dialog != null) {
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            }



        };

        if ("student".equals(LoginPage.getCurrentUserRole())) {
            // Si le rôle est étudiant, le bouton est caché
            addStudentButton.setVisible(false);
            deleteStudentButton.setVisible(false);
            generatePDFButton.setVisible(false);
            afficheMoyenneButton.setVisible(false);
        }

        ImageIcon imageIcon = new ImageIcon("src/Picture/wenhao.jpeg");
        Image image = imageIcon.getImage(); // 转换为Image对象
        Image newImage = image.getScaledInstance(15, 15,  java.awt.Image.SCALE_SMOOTH); // 调整图像大小
        imageIcon = new ImageIcon(newImage); // 重新生成ImageIcon

        JLabel reminderLabel = new JLabel(imageIcon);
        reminderLabel.addMouseListener(mouseListener);
        southPanel.add(reminderLabel, BorderLayout.EAST);

        // recherche rapide
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterTable(searchField.getText(), studentTable);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTable(searchField.getText(), studentTable);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        addStudentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                new StudentAddDialog(connection, frame, tableModel);
            }
        });


        deleteStudentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = studentTable.getSelectedRow();
                if (selectedRow != -1) {
                    int modelRow = studentTable.convertRowIndexToModel(selectedRow);
                    int studentIdToDelete = (int) tableModel.getValueAt(modelRow, 0);

                    // Check if the student is part of any binome
                    boolean isStudentInBinome = isStudentInBinome(studentIdToDelete);

                    if (isStudentInBinome) {
                        // Display a warning that the student is part of a binome and cannot be deleted
                        JOptionPane.showMessageDialog(frame, "L'étudiant fait partie d'un binôme et ne peut pas être supprimé.", "Avertissement", JOptionPane.WARNING_MESSAGE);
                    } else {
                        int choice = JOptionPane.showConfirmDialog(frame, "Êtes-vous sûr de vouloir supprimer cet étudiant ?", "Confirmation", JOptionPane.YES_NO_OPTION);

                        if (choice == JOptionPane.YES_OPTION) {
                            try {
                                String deleteSql = "DELETE FROM Etudiants WHERE numero = ?";
                                PreparedStatement preparedStatement = connection.prepareStatement(deleteSql);
                                preparedStatement.setInt(1, studentIdToDelete);
                                preparedStatement.executeUpdate();
                                tableModel.removeRow(modelRow);

                                // Show a success message
                                JOptionPane.showMessageDialog(frame, "Étudiant supprimé avec succès.", "Succès", JOptionPane.INFORMATION_MESSAGE);

                            } catch (SQLException ex) {
                                ex.printStackTrace();
                                // Show an error message in case of deletion failure
                                JOptionPane.showMessageDialog(frame, "Erreur lors de la suppression de l'étudiant.", "Erreur", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Veuillez sélectionner un étudiant à supprimer.", "Avertissement", JOptionPane.WARNING_MESSAGE);
                }
            }
        });


        afficheMoyenneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Affiche_moyenne();
            }
        });

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
                String formation = getFormation(formationId);
                String promotion = getPromotion(formationId);
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
    private static String getFormation(int formationId) {
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


    private void filterTable(String searchText,JTable studentTable) {
        RowFilter<DefaultTableModel, Object> rowFilter = RowFilter.regexFilter("(?i)" + searchText, 1, 2, 3, 4);
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) studentTable.getRowSorter();
        sorter.setRowFilter(rowFilter);
    }

    private void generatePDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Enregistrer le PDF");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Fichiers PDF (*.pdf)", "pdf");
        fileChooser.setFileFilter(filter);

        int userSelection = fileChooser.showSaveDialog(frame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                // Obtenez le fichier sélectionné par l'utilisateur
                File fileToSave = fileChooser.getSelectedFile();

                if (!fileToSave.getName().toLowerCase().endsWith(".pdf")) {
                    fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".pdf");
                }

                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(fileToSave));
                document.open();

                // Titre du document
                document.add(new Paragraph("Liste des étudiants"));
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
    private JTable createAndConfigureStudentTable() {
        String[] columnNames = {"ID", "Nom", "Prénom", "Formation", "Promotion"};
        tableModel = new DefaultTableModel(columnNames, 0); // Utilisation du modèle de données

        JTable studentTable = new JTable(tableModel);

        studentTable.setShowGrid(false);

        studentTable.setOpaque(false);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        studentTable.setRowSorter(sorter);

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



    public static void establishDatabaseConnection() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_projets", "root", "root");
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
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


    private void setIcons() {
        ImageIcon customIcon = new ImageIcon("src/Picture/logo_D.jpg");
        frame.setIconImage(customIcon.getImage());
    }


    private JPanel configureNorthPanel() {
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.X_AXIS));
        return northPanel;
    }



    private JPanel configureSouthPanel() {
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        southPanel.add(addStudentButton);
        southPanel.add(deleteStudentButton);
        southPanel.add(generatePDFButton);
        southPanel.add(afficheMoyenneButton);
        southPanel.add(retourMenuButton);
        return southPanel;
    }



    private void configureSearchField() {
        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Recherche rapide");
        searchField.setPreferredSize(new Dimension(150, 25));
    }



    private void configureButtons() {
        addStudentButton = new JButton("Ajouter Étudiant");
        deleteStudentButton = new JButton("Supprimer Étudiant");
        generatePDFButton = new JButton("Générer en PDF");
        afficheMoyenneButton = new JButton("Affiche Moyenne");
        retourMenuButton = new JButton("Retour au Menu");
    }


    private void addReturnMenuListener() {
        retourMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                new Menu();
            }
        });
    }


    private int getMaxProjectNumber() {
        try {
            String query = "SELECT MAX(CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(table_name, '_', -1), '_', 1) AS SIGNED)) FROM information_schema.tables WHERE table_name LIKE 'project_%'";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                ResultSet resultSet = preparedStatement.executeQuery();
                resultSet.next();
                return resultSet.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    private boolean isStudentInBinome(int studentId) {
        try {
            int maxProjectNumber = getMaxProjectNumber();
            if (maxProjectNumber > 0) {
                for (int projectNumber = 1; projectNumber <= maxProjectNumber; projectNumber++) {
                    String tableName = "project_" + projectNumber;
                    if (isTableExists(tableName)) {
                        String query = "SELECT COUNT(*) FROM " + tableName + " WHERE (etudiant1_numero = ? OR etudiant2_numero = ?)";
                        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                            preparedStatement.setInt(1, studentId);
                            preparedStatement.setInt(2, studentId);

                            ResultSet resultSet = preparedStatement.executeQuery();
                            resultSet.next();
                            int count = resultSet.getInt(1);

                            if (count > 0) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private boolean isTableExists(String tableName) throws SQLException {
        String query = "SELECT 1 FROM information_schema.tables WHERE table_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, tableName);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }
    }

}
