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
        // 建立数据库连接
        establishDatabaseConnection();

        // 设置主窗口属性
        setupMainFrame();

        // 设置窗体图标
        setIcons();

        // 创建项目表格
        String[] columnNames = {"Numero", "Nom Matière", "Sujet", "Date de Remise"};
        tableModel = new DefaultTableModel(columnNames, 0);
        projectTable = new JTable(tableModel);


        // 隐藏表格的网格线
        projectTable.setShowGrid(false);

        // 启用表格排序
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        projectTable.setRowSorter(sorter);

        // 为表格的指定列启用不区分大小写的排序
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

        // 创建搜索字段
        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Recherche rapide");

        // 创建表格滚动窗格
        JScrollPane tableScrollPane = new JScrollPane(projectTable);

        // 创建添加和删除项目的按钮
        JButton addProjectButton = new JButton("Ajouter Projet");
        JButton deleteProjectButton = new JButton("Supprimer Projet");
        JButton gestionBinomeButton = new JButton("Gestion binôme");
        JButton generatePDFButton = new JButton("Générer en PDF");
        JButton retourMenuButton = new JButton("Retour au Menu");

        // 隐藏ID列
        TableColumnModel tableColumnModel = projectTable.getColumnModel();
        tableColumnModel.getColumn(0).setMaxWidth(0);
        tableColumnModel.getColumn(0).setMinWidth(0);
        tableColumnModel.getColumn(0).setPreferredWidth(0);
        tableColumnModel.getColumn(0).setResizable(false);



        // 创建主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // 添加 Dauphine 校徽 Logo
        LogoDauphine logoDauphine = new LogoDauphine();
        mainPanel.add(logoDauphine);

        // 创建搜索面板
        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Recherche : "));
        searchPanel.add(searchField);
        mainPanel.add(searchPanel);

        // 创建按钮面板，包括"添加项目"、"删除项目"、"管理项目组"、"生成 PDF"和"返回菜单"按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addProjectButton);
        buttonPanel.add(deleteProjectButton);
        buttonPanel.add(gestionBinomeButton);
        buttonPanel.add(generatePDFButton);
        buttonPanel.add(retourMenuButton);

        // 添加表格滚动面板
        mainPanel.add(tableScrollPane);

        // 添加按钮面板
        mainPanel.add(buttonPanel);

        // 设置主面板的边框
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 20));





//        // 创建搜索面板
//        JPanel searchPanel = new JPanel();
//        searchPanel.add(new JLabel("Recherche : "));
//        searchPanel.add(searchField);
//
//        // 创建按钮面板，包括"添加项目"和"返回菜单"按钮
//        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//        buttonPanel.add(addProjectButton);
//        buttonPanel.add(deleteProjectButton);
//        buttonPanel.add(gestionBinomeButton);
//        buttonPanel.add(generatePDFButton);
//        buttonPanel.add(retourMenuButton);
//
//        // 创建主面板
//        JPanel mainPanel = new JPanel(new BorderLayout());
//        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
//        mainPanel.add(searchPanel, BorderLayout.NORTH);
//        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
//        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 20));  // 调整边缘的距离

        // 为搜索字段添加文档监听器
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
                // 不处理属性更改
            }
        });

        // 将主面板添加到窗口
        frame.add(mainPanel);
        frame.pack();
        frame.setVisible(true);
        loadProjectsFromDatabase();

        // 为"添加项目"按钮添加事件监听器
        addProjectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ProjectAddDialog(tableModel, connection);
            }
        });

        // 为"删除项目"按钮添加事件监听器
        deleteProjectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = projectTable.getSelectedRow();
                if (selectedRow != -1) {
                    int projectNumber = getPrimaryKeyValueFromSelectedRow();

                    if (projectNumber != -1) {
                        // 检查是否有与该项目关联的binôme
                        if (areBinomesAssociated(projectNumber)) {
                            // 获取项目名称以在消息中显示
                            String projectName = (String) tableModel.getValueAt(selectedRow, getColumnIndex("Nom Matière"));

                            // 根据是否有binôme关联显示警告对话框
                            if (confirmBinomeDeletion(projectName)) {
                                // 删除与项目关联的binôme
                                if (deleteBinomesForProject(projectNumber)) {
                                    // binôme成功删除，删除项目
                                    deleteProject(selectedRow);
                                } else {
                                    // 如果删除binôme失败，显示错误消息
                                    JOptionPane.showMessageDialog(frame, "La suppression des binômes a échoué.", "Erreur", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        } else {
                            // 如果没有与项目关联的binôme，只需要求用户确认删除
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

        // 为"生成PDF"按钮添加事件监听器
        generatePDFButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generatePDF();
            }
        });

        // 为"管理binome"按钮添加事件监听器
        gestionBinomeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 步骤3：检查是否选择了项目
                int selectedRow = projectTable.getSelectedRow();
                if (selectedRow != -1) {
                    // 获取所选项目的项目编号
                    int selectedProjectNumber = getPrimaryKeyValueFromSelectedRow();
                    // 创建"管理binome"窗口的实例，将项目名称传递给窗口
                    new Gestion_binome(selectedProjectNumber);
                } else {
                    // 显示错误消息或对话框，指示未选择任何内容
                    JOptionPane.showMessageDialog(frame, "Sélectionnez un projet avant de gérer les binômes.", "Avertissement", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // 为"返回菜单"按钮添加事件监听器
        retourMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                new Menu();
            }
        });


        // 使得列头更具可读性
        JTableHeader header = projectTable.getTableHeader();
        header.setBackground(new Color(108, 190, 213)); // 设置列头背景颜色
        header.setForeground(Color.WHITE); // 设置列头前景颜色
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 26));// 增加列头的行高


        // 设置表格的字体和行高
        projectTable.setFont(new Font("Arial", Font.PLAIN, 12));
        projectTable.setRowHeight(23);


        // 创建一个表格渲染器以使表格更加美观
        projectTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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
        frame = new JFrame();
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


    // 从数据库加载项目
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


    // 过滤表格内容
    private void filterTable(String searchText, JTable projectTable) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchText));
        projectTable.setRowSorter(sorter);
    }


    // 获取指定列名的列索引
    private int getColumnIndex(String columnName) {
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            if (columnName.equals(tableModel.getColumnName(i))) {
                return i;
            }
        }
        return -1;
    }


    // 从选定行获取项目的主键值
    private int getPrimaryKeyValueFromSelectedRow() {
        int projectNumber = -1;
        int selectedRow = projectTable.getSelectedRow();
        if (selectedRow != -1) {
            // 访问列值
            String matiere = (String) tableModel.getValueAt(selectedRow, getColumnIndex("Nom Matière"));
            String sujet = (String) tableModel.getValueAt(selectedRow, getColumnIndex("Sujet"));
            String dateRemise = (String) tableModel.getValueAt(selectedRow, getColumnIndex("Date de Remise"));

            // 执行必要的操作
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


    // 删除项目
    private void deleteProject(int selectedRow) {
        // 从选定行获取项目编号
        int projectNumber = getPrimaryKeyValueFromSelectedRow();
        if (projectNumber != -1) {
            // 删除与项目关联的表中的记录
            deleteNotesForProject(projectNumber);
            // 删除项目
            String matiere = (String) tableModel.getValueAt(selectedRow, getColumnIndex("Nom Matière"));
            String sujet = (String) tableModel.getValueAt(selectedRow, getColumnIndex("Sujet"));
            String dateRemise = (String) tableModel.getValueAt(selectedRow, getColumnIndex("Date de Remise"));

            try {
                // 从Projets表中删除项目
                String deleteSql = "DELETE FROM Projets WHERE numero = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(deleteSql);
                preparedStatement.setInt(1, projectNumber);
                preparedStatement.executeUpdate();
                // 从表格中删除行
                tableModel.removeRow(selectedRow);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            // 如果有关联的binômes，删除它
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


    // 删除项目相关的Note
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



    // 删除与项目相关的binômes
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


    // 检查项目是否关联了binômes
    boolean areBinomesAssociated(int projectNumber) {
        String tableName = "Project_" + projectNumber;
        try {
            DatabaseMetaData metadata = connection.getMetaData();
            ResultSet resultSet = metadata.getTables(null, null, tableName, null);

            if (resultSet.next()) {
                // 查询binômes的记录数
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


    // 确认是否删除与项目相关的binômes
    private boolean confirmBinomeDeletion(String projectName) {
        int option;
        if (areBinomesAssociated(getPrimaryKeyValueFromSelectedRow())) {
            // 如果与项目关联了binômes，显示警告
            option = JOptionPane.showConfirmDialog(frame, "Il y a des binômes associés au projet '" + projectName + "'. La suppression de ce projet entraînera également la suppression de tous les binômes associés. Voulez-vous continuer ?", "Avertissement", JOptionPane.YES_NO_OPTION);
        } else {
            // 如果没有与项目关联的binômes，显示普通确认
            option = JOptionPane.showConfirmDialog(frame, "Voulez-vous vraiment supprimer ce projet ?", "Confirmation", JOptionPane.YES_NO_OPTION);
        }
        return option == JOptionPane.YES_OPTION;
    }


    // 确认是否删除没有与项目相关的binômes的项目
    private boolean confirmProjectDeletion() {
        int option = JOptionPane.showConfirmDialog(frame, "Voulez-vous vraiment supprimer ce projet ? (Aucun binôme n'est associé à ce projet)", "Confirmation", JOptionPane.YES_NO_OPTION);
        return option == JOptionPane.YES_OPTION;
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
                document.add(new Paragraph("Liste des projets"));
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
