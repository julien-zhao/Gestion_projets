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

        // 初始化projectNumber
        this.projectNumber = projectNumber;

        // 建立数据库连接
        establishDatabaseConnection();

        // 设置主窗口属性
        setupMainFrame();

        // 设置窗体图标
        setIcons();

        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 创建binome列表的表格
        String[] columnNames = {"ID", "Projet", "Étudiant 1", "Étudiant 2", "Note Rapport", "Note Soutenance Étudiant 1", "Note Soutenance Étudiant 2", "Date de Remise Effective"};
        tableModel = new DefaultTableModel(columnNames, 0);

        // 创建表格并启用排序
        JTable binomeTable = new JTable(tableModel);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        binomeTable.setRowSorter(sorter);

        // 隐藏表格的网格线
        binomeTable.setShowGrid(false);

        // 为表格启用不区分大小写的排序
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

        // 添加搜索字段的文本变更监听器
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
                // 不处理属性更改
            }
        });


        // 添加返回项目按钮的事件监听器
        retourProjectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 关闭配对管理界面，返回 Gestion_projet 的现有实例
                frame.dispose(); // Ferme l'interface de gestion des binômes
                if (Gestion_projet.currentInstance != null) {
                    Gestion_projet.currentInstance.frame.setVisible(true); // Affichez à nouveau l'instance existante de Gestion_projet
                }
            }
        });


        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addBinomeButton);
        buttonPanel.add(deleteBinomeButton);
        buttonPanel.add(afficheNoteButton);
        buttonPanel.add(generatePDFButton);
        buttonPanel.add(retourProjectButton);


        // 创建搜索面板
        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Recherche : "));
        searchPanel.add(searchField);


        // 将组件添加到主面板
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 20));  // 调整边缘的距离

        // 将主面板添加到窗口
        frame.add(mainPanel);
        frame.pack();
        frame.setVisible(true);

        // 检查binome是否存在
        boolean binomeTableExists = checkTableExistence(projectNumber);

        if (!binomeTableExists) {
            // 如果不存在，则创建binome表
            createBinomeTable(projectNumber);
        }
        // 从数据库加载binome信息
        loadBinomesFromDatabase(projectNumber);
        // 添加"添加binome"按钮的事件监听器
        addBinomeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new BinomeAddDialog(connection, frame, tableModel,projectNumber);
            }
        });
        // 添加"删除binome"按钮的事件监听器
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
        // 添加"显示成绩"按钮的事件监听器
        afficheNoteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //JFrame frame;
                new Affiche_note(tableModel,projectNumber);
            }
        });


        // 为成绩列添加自定义单元格编辑事件处理器
        binomeTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(new JTextField()) {
            @Override
            public boolean stopCellEditing() {
                try {
                    String value = (String) getCellEditorValue();
                    // 检查值是否为有效数字，使用 Double.parseDouble
                    Double.parseDouble(value);
                    return super.stopCellEditing();
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(frame, "Veuillez saisir un nombre valide.", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
                    return false;// 防止编辑结束
                }
            }
        });
        binomeTable.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(new JTextField()) {
            @Override
            public boolean stopCellEditing() {
                try {
                    String value = (String) getCellEditorValue();
                    // 检查值是否为有效数字，使用 Double.parseDouble
                    Double.parseDouble(value);
                    return super.stopCellEditing();
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(frame, "Veuillez saisir un nombre valide.", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
                    return false; // 防止编辑结束
                }
            }
        });
        binomeTable.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(new JTextField()) {
            @Override
            public boolean stopCellEditing() {
                try {
                    String value = (String) getCellEditorValue();
                    // 检查值是否为有效数字，使用 Double.parseDouble
                    Double.parseDouble(value);
                    return super.stopCellEditing();
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(frame, "Veuillez saisir un nombre valide.", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
                    return false; // 防止编辑结束
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


        // 添加"生成PDF"按钮的事件监听器
        generatePDFButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generatePDF();
            }
        });


        // 使得列头更具可读性
        JTableHeader header = binomeTable.getTableHeader();
        header.setBackground(new Color(108, 190, 213)); // 设置列头背景颜色
        header.setForeground(Color.WHITE); // 设置列头前景颜色
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 26));// 增加列头的行高


        // 设置表格的字体和行高
        binomeTable.setFont(new Font("Arial", Font.PLAIN, 12));
        binomeTable.setRowHeight(23);


        // 创建一个表格渲染器以使表格更加美观
        binomeTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                // 交替颜色
                if (row % 2 == 0) {
                    c.setBackground(new Color(240, 240, 240)); // 浅灰色
                } else {
                    c.setBackground(Color.WHITE);
                }
                // 设置选中行的背景颜色
                if (isSelected) {
                    c.setBackground(new Color(173, 216, 230)); // 淡蓝色
                }

                // 设置文字居中
                ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);

                return c;
            }
        });

    }




    // 建立数据库连接
    private void establishDatabaseConnection() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_projets", "root", "root");
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
    }


    // 设置窗口图标
    private void setIcons() {
        ImageIcon customIcon = new ImageIcon("C:\\Users\\MATEBOOK14\\Desktop\\Gestion_projets\\logo_D.jpg");
        frame.setIconImage(customIcon.getImage());
    }


    // 设置主窗口
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


    // 更新评分列的方法
    private void updateNoteRapport(int binomeId, Object updatedValue) {
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


    // 方法：更新学生1的答辩成绩
    private void updateNoteSoutenanceEtu1(int binomeId, Object updatedValue) {
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


    // 方法：更新学生2的答辩成绩
    private void updateNoteSoutenanceEtu2(int binomeId, Object updatedValue) {
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


    // 方法：删除binome
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


    // 方法：根据ID获取行索引
    private int getRowIndexById(int binomeId) {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            int id = (int) tableModel.getValueAt(row, 0);
            if (id == binomeId) {
                return row;
            }
        }
        return -1; // Si l'ID du binôme n'est pas trouvé
    }


    // 方法：从数据库加载binome信息
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


    // 方法：检查表是否存在
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


    // 方法：创建Binome表
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


    // 方法：获取项目名称
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


    // 方法：获取学生姓名
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


    // 方法：过滤表格数据
    private void filterTable(String searchText, JTable binomeTable) {
        RowFilter<DefaultTableModel, Object> rowFilter = RowFilter.regexFilter("(?i)" + searchText, 1,2, 3,4,5,6,7,8);
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) binomeTable.getRowSorter();
        sorter.setRowFilter(rowFilter);
    }


    // 生成PDF文件
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
