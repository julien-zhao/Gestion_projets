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
import java.util.Comparator;



public class Gestion_etudiant{
    private static Connection connection = null;
    private static DefaultTableModel tableModel;

    JPanel mainPanel = new JPanel(new BorderLayout());
    private JPanel northPanel;
    private JPanel southPanel;
    JFrame frame = new JFrame("Gestion des Projets Étudiants");
    JTextField searchField = new JTextField(20);
    private JButton addStudentButton;
    private JButton deleteStudentButton;
    private JButton generatePDFButton;
    private JButton retourMenuButton;



    Gestion_etudiant(){

        // 建立数据库连接
        establishDatabaseConnection();

        // 设置主窗口属性
        setupMainFrame();

        // 设置窗体图标
        setIcons();

        // 创建并配置学生表格
        createAndConfigureStudentTable();

        // 创建并配置按钮
        configureButtons();

        // 创建并配置搜索字段
        configureSearchField();

        // 创建并配置北部面板
        JPanel northPanel = configureNorthPanel();

        // 创建并配置南部面板
        JPanel southPanel = configureSouthPanel();

        // 为返回菜单按钮添加动作监听器
        addReturnMenuListener();


        // 创建并配置表格滚动面板，同时设置边框和柔和的边框
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


        // 设置表格列的最大宽度、最小宽度、首选宽度和不可调整大小
        TableColumnModel tableColumnModel = studentTable.getColumnModel();
        tableColumnModel.getColumn(0).setMaxWidth(0);
        tableColumnModel.getColumn(0).setMinWidth(0);
        tableColumnModel.getColumn(0).setPreferredWidth(0);
        tableColumnModel.getColumn(0).setResizable(false);


        //页面的NORTH部分
        northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.X_AXIS));
        northPanel.add(logoDauphine);
        northPanel.add(searchPanel);
        northPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 0, 20));  // 调整边缘的距离

        //页面的SOUTH部分
        southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        southPanel.add(addStudentButton);
        southPanel.add(deleteStudentButton);
        southPanel.add(generatePDFButton);
        southPanel.add(retourMenuButton);
        southPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));  // 调整边缘的距离

        //页面的MAIN部分
        mainPanel.add(northPanel, BorderLayout.NORTH);
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);


        frame.add(mainPanel); // 将主面板添加到窗体，并设置窗体属性
        frame.pack(); // 自动调整窗体大小以适应内容
        frame.setVisible(true); // 设置窗体可见
        loadStudentsFromDatabase(); // 从数据库加载学生数据


        // 创建并配置formation下拉列表筛选器
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
                    // 如果选择了 "All"，则传入空字符串
                    selectedFormation = "";
                }
                // 直接获取已经创建的表格对象，并在该表格上应用过滤器
                JTable studentTable = (JTable) ((JScrollPane) mainPanel.getComponent(1)).getViewport().getView();
                filterTable(selectedFormation, studentTable);
            }
        });
        northPanel.add(formationFilter);


        // 创建并配置promotion下拉列表筛选器
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
                    // 如果选择了 "All"，则传入空字符串
                    selectedPromotion = "";
                }
                // 直接获取已经创建的表格对象，并在该表格上应用过滤器
                JTable studentTable = (JTable) ((JScrollPane) mainPanel.getComponent(1)).getViewport().getView();
                filterTable(selectedPromotion, studentTable);
            }
        });
        northPanel.add(promotionFilter);


        // 创建鼠标事件监听器
        MouseListener mouseListener = new MouseAdapter() {
            private JDialog dialog;

            @Override
            public void mouseEntered(MouseEvent e) {
                // 当鼠标进入标签时，显示提示框
                dialog = new JDialog((JFrame) null, "Aide", false);

                ImageIcon icon = new ImageIcon("C:\\Users\\MATEBOOK14\\Desktop\\Gestion_projets\\logo_D.jpg"); // 替换为实际图标文件的路径
                dialog.setIconImage(icon.getImage());

                int xOffset = 10;
                int yOffset = 150;
                Point componentPosition = e.getComponent().getLocationOnScreen();
                int xPosition = componentPosition.x + xOffset;
                int yPosition = componentPosition.y - dialog.getHeight() - yOffset;
                dialog.setLocation(xPosition, yPosition);

                JLabel label = new JLabel("<html> - Lorsque vous souhaitez supprimer les informations relatives à un élève, vous devez d'abord sélectionner l'élève en cliquant dessus, puis appuyer sur le bouton de suppression.<br><br> - Si vous avez d'autres questions, veuillez contacter : info@dauphine.eu</html>");
                label.setPreferredSize(new Dimension(380, 100)); // 设置首选大小
                label.setMaximumSize(new Dimension(500, 100)); // 设置最大大小以确保不会扩展
                dialog.add(label);
                dialog.pack();
                dialog.setVisible(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // 当鼠标离开标签时，隐藏提示框
                if (dialog != null) {
                    dialog.setVisible(false);
                    dialog.dispose();
                }
            }
        };


        //Aide功能实现
        ImageIcon imageIcon = new ImageIcon("C:\\Users\\MATEBOOK14\\Desktop\\Gestion_projets\\wenhao.jpeg");
        Image image = imageIcon.getImage(); // 转换为Image对象
        Image newImage = image.getScaledInstance(15, 15,  java.awt.Image.SCALE_SMOOTH); // 调整图像大小
        imageIcon = new ImageIcon(newImage); // 重新生成ImageIcon

        JLabel reminderLabel = new JLabel(imageIcon);
        reminderLabel.addMouseListener(mouseListener); // 添加鼠标事件监听器
        southPanel.add(reminderLabel, BorderLayout.EAST);


        // 设置搜索字段的文档监听器，用于实时过滤表格数据
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                // 当插入文本时触发，调用filterTable方法过滤表格数据
                filterTable(searchField.getText(), studentTable);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                // 当删除文本时触发，同样调用filterTable方法过滤表格数据
                filterTable(searchField.getText(), studentTable);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // 属性更改事件，不在此处理
            }
        });


        // 为"添加学生"按钮添加事件监听器
        addStudentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 当用户点击"添加学生"按钮时，打开一个学生添加对话框
                new StudentAddDialog(connection, frame, tableModel);
            }
        });


        // 为"删除学生"按钮添加事件监听器
        deleteStudentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 获取选中行的学生数据，并进行删除确认
                int selectedRow = studentTable.getSelectedRow();
                if (selectedRow != -1) {
                    int studentIdToDelete = (int) tableModel.getValueAt(selectedRow, 0);

                    // 弹出确认对话框
                    int choice = JOptionPane.showConfirmDialog(frame, "Êtes-vous sûr de vouloir supprimer cet étudiant ?", "Confirmation", JOptionPane.YES_NO_OPTION);

                    if (choice == JOptionPane.YES_OPTION) {
                        try {
                            // 执行数据库删除操作，并从表格模型中移除相应行
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
                    // 如果没有选中行，则提示用户选择一个学生
                    JOptionPane.showMessageDialog(frame, "Veuillez sélectionner un étudiant à supprimer.", "Avertissement", JOptionPane.WARNING_MESSAGE);
                }
            }
        });


        // "生成 PDF "按钮的事件管理器
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
                String formation = getFormation(formationId); // Utilisez une méthode séparée

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



    // 根据搜索文本过滤表格的函数
    private void filterTable(String searchText,JTable studentTable) {
        RowFilter<DefaultTableModel, Object> rowFilter = RowFilter.regexFilter("(?i)" + searchText, 1, 2, 3, 4);
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) studentTable.getRowSorter();
        sorter.setRowFilter(rowFilter);
    }



    // 生成PDF文件的函数
    private void generatePDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Enregistrer le PDF");
        // 设置文件过滤器，仅显示PDF文件
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



    private JTable createAndConfigureStudentTable() {
        // 创建表格用于显示学生列表
        String[] columnNames = {"ID", "Nom", "Prénom", "Formation", "Promotion"};
        tableModel = new DefaultTableModel(columnNames, 0); // Utilisation du modèle de données

        // 创建主表格
        JTable studentTable = new JTable(tableModel);

        // 隐藏表格的网格线
        studentTable.setShowGrid(false);

        // 设置表格为透明
        studentTable.setOpaque(false);

        // 启用表格排序
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        studentTable.setRowSorter(sorter);

        // 以不区分大小写的方式对列进行排序
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

        // 使得列头更具可读性
        JTableHeader header = studentTable.getTableHeader();
        header.setBackground(new Color(108, 190, 213)); // 设置列头背景颜色
        header.setForeground(Color.WHITE); // 设置列头前景颜色
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 26));// 增加列头的行高

        // 设置表格的字体和行高
        studentTable.setFont(new Font("Arial", Font.PLAIN, 12));
        studentTable.setRowHeight(23);

        // 创建一个表格渲染器以使表格更加美观
        studentTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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
                return c;
            }
        });

        return studentTable;

    }



    private void establishDatabaseConnection() {
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
        // 添加这一行确保窗口的大小已经被正确设置
        frame.pack();
        // 将窗口设置为屏幕中央
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(x, y);
    }


    private void setIcons() {
        ImageIcon customIcon = new ImageIcon("C:\\Users\\MATEBOOK14\\Desktop\\Gestion_projets\\logo_D.jpg");
        frame.setIconImage(customIcon.getImage());
    }



    private JPanel configureNorthPanel() {
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.X_AXIS));
        // 其他北部面板的配置...
        return northPanel;
    }



    private JPanel configureSouthPanel() {
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        southPanel.add(addStudentButton);
        southPanel.add(deleteStudentButton);
        southPanel.add(generatePDFButton);
        southPanel.add(retourMenuButton);
        // 其他南部面板的配置...
        return southPanel;
    }



    private void configureSearchField() {
        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Recherche rapide");
        searchField.setPreferredSize(new Dimension(150, 25));
    }



    private void configureButtons() {
        // 创建并配置按钮
        addStudentButton = new JButton("Ajouter Étudiant");
        deleteStudentButton = new JButton("Supprimer Étudiant");
        generatePDFButton = new JButton("Générer en PDF");
        retourMenuButton = new JButton("Retour au Menu");
        // 其他按钮的配置...
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

}