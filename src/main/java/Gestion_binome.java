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
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
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

        this.projectNumber = projectNumber;

        establishDatabaseConnection();

        setupMainFrame();

        setIcons();

        JPanel mainPanel = new JPanel(new BorderLayout());

        String[] columnNames = {"ID", "Projet", "Étudiant 1", "Étudiant 2", "Note Rapport", "Note Soutenance Étudiant 1", "Note Soutenance Étudiant 2", "Date de Remise Effective"};
        tableModel = new DefaultTableModel(columnNames, 0);

        JTable binomeTable = new JTable(tableModel);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        binomeTable.setRowSorter(sorter);

        binomeTable.setShowGrid(false);




        TableRowSorter<DefaultTableModel> caseInsensitiveSorter = new TableRowSorter<>(tableModel) {
            @Override
            public Comparator<?> getComparator(int column) {
                if (column >= 1 && column <= 8) {
                    return Collator.getInstance();
                }
                return super.getComparator(column);
            }
        };
        binomeTable.setRowSorter(caseInsensitiveSorter);
        JScrollPane tableScrollPane = new JScrollPane(binomeTable);

        // 创建搜索字段
        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Recherche rapide"); // 提示用户的工具提示文本

        // 创建按钮
        JButton addBinomeButton = new JButton("Ajouter Binôme");
        JButton deleteBinomeButton = new JButton("Supprimer Binôme");
        JButton afficheNoteButton = new JButton("Affiche note");
        JButton generatePDFButton = new JButton("Générer en PDF");
        JButton retourProjectButton = new JButton("Retour au Project");

        // 隐藏ID和项目列
        TableColumnModel tableColumnModel = binomeTable.getColumnModel();
        tableColumnModel.getColumn(0).setMaxWidth(0);
        tableColumnModel.getColumn(0).setMinWidth(0);
        tableColumnModel.getColumn(0).setPreferredWidth(0);
        tableColumnModel.getColumn(0).setResizable(false);
        tableColumnModel.getColumn(1).setMaxWidth(0);
        tableColumnModel.getColumn(1).setMinWidth(0);
        tableColumnModel.getColumn(1).setPreferredWidth(0);
        tableColumnModel.getColumn(1).setResizable(false);

        // recherche rapide
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
            }
        });


        // button retour
        retourProjectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose(); // Ferme l'interface de gestion des binômes
                if (Gestion_projet.currentInstance != null) {
                    Gestion_projet.currentInstance.frame.setVisible(true); // Affichez à nouveau l'instance existante de Gestion_projet
                }
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addBinomeButton);
        buttonPanel.add(deleteBinomeButton);
        buttonPanel.add(afficheNoteButton);
        buttonPanel.add(generatePDFButton);
        buttonPanel.add(retourProjectButton);


        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Recherche : "));
        searchPanel.add(searchField);


        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 20));  // 调整边缘的距离

        frame.add(mainPanel);
        frame.pack();
        frame.setVisible(true);

        boolean binomeTableExists = checkTableExistence(projectNumber);

        if (!binomeTableExists) {
            createBinomeTable(projectNumber);
        }
        loadBinomesFromDatabase(projectNumber);

        addBinomeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new BinomeAddDialog(connection, frame, tableModel,projectNumber);
            }
        });

        //button supprimer
        deleteBinomeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = binomeTable.getSelectedRow();

                if (selectedRow != -1) {
                    int modelRow = binomeTable.convertRowIndexToModel(selectedRow);
                    int binomeIdToDelete = (int) tableModel.getValueAt(modelRow, 0);
                    int confirmation = JOptionPane.showConfirmDialog(frame, "Êtes-vous sûr de vouloir supprimer ce binôme ?", "Confirmation de suppression", JOptionPane.YES_NO_OPTION);
                    if (confirmation == JOptionPane.YES_OPTION) {
                        deleteBinome(binomeIdToDelete);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Veuillez sélectionner un binôme à supprimer.", "Avertissement", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        // button aficher les notes
        afficheNoteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //JFrame frame;
                new Affiche_note(tableModel,projectNumber);
            }
        });

        binomeTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(new JTextField()) {
            @Override
            public boolean stopCellEditing() {
                try {
                    String value = (String) getCellEditorValue();
                    Double.parseDouble(value);
                    return super.stopCellEditing();
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(frame, "Veuillez saisir un nombre valide.", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        });
        binomeTable.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(new JTextField()) {
            @Override
            public boolean stopCellEditing() {
                try {
                    String value = (String) getCellEditorValue();
                    Double.parseDouble(value);
                    return super.stopCellEditing();
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(frame, "Veuillez saisir un nombre valide.", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        });
        binomeTable.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(new JTextField()) {
            @Override
            public boolean stopCellEditing() {
                try {
                    String value = (String) getCellEditorValue();
                    Double.parseDouble(value);
                    return super.stopCellEditing();
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(frame, "Veuillez saisir un nombre valide.", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        });

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
                        return false;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(frame, "Veuillez saisir un nombre valide.", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
                    return false;
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

                    if (columnName.equals("Note Rapport")) {
                        updateNoteRapport(binomeId, updatedValue);
                    } else if (columnName.equals("Note Soutenance Étudiant 1")) {
                        updateNoteSoutenanceEtu1(binomeId, updatedValue);
                    } else if (columnName.equals("Note Soutenance Étudiant 2")) {
                        updateNoteSoutenanceEtu2(binomeId, updatedValue);
                    }
                }
            }
        });


        // generer un pdf
        generatePDFButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generatePDF();
            }
        });

        JTableHeader header = binomeTable.getTableHeader();
        header.setBackground(new Color(108, 190, 213));
        header.setForeground(Color.WHITE); // 设置列头前景颜色
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 26));


        binomeTable.setFont(new Font("Arial", Font.PLAIN, 12));
        binomeTable.setRowHeight(23);


        binomeTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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

                if (column == 4 || column == 5 || column == 6) {
                    c.setBackground(new Color(255, 200, 200));
                }

                return c;
            }
        });
        MouseListener mouseListener = new MouseAdapter() {
            private JDialog dialog;

            @Override
            public void mouseEntered(MouseEvent e) {
                dialog = new JDialog((JFrame) null, "Aide", false);

                ImageIcon icon = new ImageIcon("src/Picture/ogo_D.jpg");
                dialog.setIconImage(icon.getImage());

                int xOffset = 10;
                int yOffset = 150;
                Point componentPosition = e.getComponent().getLocationOnScreen();
                int xPosition = componentPosition.x + xOffset;
                int yPosition = componentPosition.y - dialog.getHeight() - yOffset;
                dialog.setLocation(xPosition, yPosition);

                JLabel label = new JLabel("<html>Les notes dans la zone rouge peuvent être modifiées directement en cliquant dessus. <br><br> - Si vous avez d'autres questions, veuillez contacter : info@dauphine.eu</html>");
                label.setPreferredSize(new Dimension(380, 100));
                label.setMaximumSize(new Dimension(500, 100));
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


        //Aide功能实现
        ImageIcon imageIcon = new ImageIcon("src/Picture/wenhao.jpeg");
        Image image = imageIcon.getImage();
        Image newImage = image.getScaledInstance(15, 15,  java.awt.Image.SCALE_SMOOTH); // 调整图像大小
        imageIcon = new ImageIcon(newImage);
        JLabel reminderLabel = new JLabel(imageIcon);
        reminderLabel.addMouseListener(mouseListener); // 添加鼠标事件监听器
        buttonPanel.add(reminderLabel, BorderLayout.EAST);


        if ("student".equals(LoginPage.getCurrentUserRole())) {
            // Si le rôle est étudiant, le bouton est caché
            deleteBinomeButton.setVisible(false);
            addBinomeButton.setVisible(false);
            generatePDFButton.setVisible(false);
        }
    }




    // connexion base de donnée
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
        frame = new JFrame("Gestion des Binômes du projet : " + getProjectName(projectNumber));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1000, 700));
        // 添加这一行确保窗口的大小已经被正确设置
        frame.pack();
        // 将窗口设置为屏幕中央
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(x, y);
    }


    private void updateNoteRapport(int binomeId, Object updatedValue) {
        if ("student".equals(LoginPage.getCurrentUserRole())) {
            // Si le rôle est étudiant, ne permettez pas la modification
            JOptionPane.showMessageDialog(frame, "Vous n'avez pas les autorisations pour effectuer cette opération.", "Autorisations insuffisantes", JOptionPane.WARNING_MESSAGE);
            return;
        }
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
        if ("student".equals(LoginPage.getCurrentUserRole())) {
            // Si le rôle est étudiant, ne permettez pas la modification
            JOptionPane.showMessageDialog(frame, "Vous n'avez pas les autorisations pour effectuer cette opération.", "Autorisations insuffisantes", JOptionPane.WARNING_MESSAGE);
            return;
        }
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
        if ("student".equals(LoginPage.getCurrentUserRole())) {
            // Si le rôle est étudiant, ne permettez pas la modification
            JOptionPane.showMessageDialog(frame, "Vous n'avez pas les autorisations pour effectuer cette opération.", "Autorisations insuffisantes", JOptionPane.WARNING_MESSAGE);
            return;
        }
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


    // supprimer un binome
    private void deleteBinome(int binomeId) {
        try {
            String tableName = "Project_" + projectNumber;
            String deleteSql = "DELETE FROM " + tableName + " WHERE id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(deleteSql);
            preparedStatement.setInt(1, binomeId);
            preparedStatement.executeUpdate();

            tableModel.removeRow(getRowIndexById(binomeId));
            // Afficher un message de confirmation
            JOptionPane.showMessageDialog(frame, "Le binôme a été supprimé avec succès.", "Confirmation de suppression", JOptionPane.INFORMATION_MESSAGE);
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


    // chargement base de donnée
    private static void loadBinomesFromDatabase(int projectNumber) {
        tableModel.setRowCount(0); // 清空表中的现有行
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
        // 打开文件选择器
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Enregistrer le PDF");

        // 设置文件过滤器，仅显示PDF文件
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Fichiers PDF (*.pdf)", "pdf");
        fileChooser.setFileFilter(filter);

        int userSelection = fileChooser.showSaveDialog(frame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                // 获取用户选择的文件
                File fileToSave = fileChooser.getSelectedFile();

                if (!fileToSave.getName().toLowerCase().endsWith(".pdf")) {
                    // 确保扩展名是.pdf
                    fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".pdf");
                }

                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(fileToSave));
                document.open();

                // 文档标题
                document.add(new Paragraph("Liste des étudiants"));
                document.add(new Paragraph("\n")); // 添加空段落（换行）

                // 创建一个表格，列数比表格模型少一列
                PdfPTable pdfTable = new PdfPTable(tableModel.getColumnCount() - 1); // 减去一列（不包括“ID”列）
                pdfTable.setWidthPercentage(100);

                // 列标题（不包括“ID”列）
                for (int col = 1; col < tableModel.getColumnCount(); col++) {
                    PdfPCell cell = new PdfPCell(new Phrase(tableModel.getColumnName(col)));
                    pdfTable.addCell(cell);
                }

                // 表格内容（不包括 "ID "列）
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
