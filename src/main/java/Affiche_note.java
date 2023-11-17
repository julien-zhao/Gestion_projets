import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
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
        // 初始化表格模型和项目编号
        this.tableModel = tableModel;
        this.projectNumber = projectNumber;

        // 建立数据库连接
        establishDatabaseConnection();

        // 从数据库获取项目名称
        projectName = getProjectName(projectNumber);

        // 创建用于显示成绩的表格模型
        DefaultTableModel noteTableModel = new DefaultTableModel();
        noteTableModel.addColumn("Étudiant"); // 学生
        noteTableModel.addColumn("Note rapport"); // 报告分数
        noteTableModel.addColumn("Note soutenance"); // 论文答辩分数
        noteTableModel.addColumn("Jours de retard"); // 延迟天数
        noteTableModel.addColumn("Note finale"); // 最终分数

        // 计算并将成绩添加到表格模型
        calculateAndDisplayNotes(noteTableModel);

        // 创建带有成绩表格模型的表格
        JTable noteTable = new JTable(noteTableModel);

        // 创建带有滚动条的面板以显示表格
        JScrollPane scrollPane = new JScrollPane(noteTable);

        // 隐藏表格的网格线
        noteTable.setShowGrid(false);

        // 创建生成PDF按钮
        JButton generatePDFButton = new JButton("Générer en PDF");

        // 创建按钮面板并添加生成PDF按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(generatePDFButton);

        // 为生成PDF按钮添加事件处理
        generatePDFButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generatePDF(noteTableModel);
            }
        });

        // 设置主窗口属性
        frame = new JFrame("Liste des notes des étudiants dans le projet " + projectName);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setPreferredSize(new Dimension(600, 400));
        frame.add(scrollPane);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);


        // 将窗口居中显示
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(x, y);


        // 使得列头更具可读性
        JTableHeader header = noteTable.getTableHeader();
        header.setBackground(new Color(108, 190, 213)); // 设置列头背景颜色
        header.setForeground(Color.WHITE); // 设置列头前景颜色
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 26));// 增加列头的行高


        // 设置表格的字体和行高
        noteTable.setFont(new Font("Arial", Font.PLAIN, 12));
        noteTable.setRowHeight(23);


        // 创建一个表格渲染器以使表格更加美观
        noteTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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

        // 设置窗体图标
        setIcons();
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


    // 计算并显示学生项目成绩的方法
    private void calculateAndDisplayNotes(DefaultTableModel noteTableModel) {
        // 遍历表格的每一行，并根据延迟计算最终成绩
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            // 获取学生姓名
            String etudiant1 = tableModel.getValueAt(row, 2).toString();
            String etudiant2 = tableModel.getValueAt(row, 3).toString();

            // 获取评分信息
            double soutenance1 = Double.parseDouble(tableModel.getValueAt(row, 5).toString());
            double soutenance2 = Double.parseDouble(tableModel.getValueAt(row, 6).toString());
            double rapport = Double.parseDouble(tableModel.getValueAt(row, 4).toString());
            String dateRemiseEffective = tableModel.getValueAt(row, 7).toString();
            Date dateRemise = Date.valueOf(getDateRemise(projectNumber));

            // 计算两个最终成绩
            double noteFinale1 = (rapport + soutenance1) / 2;
            double noteFinale2 = (rapport + soutenance2) / 2;

            // 计算延迟天数
            int joursDeRetard = calculateDaysOfDelay(dateRemise, Date.valueOf(dateRemiseEffective));

            // 如果存在延迟，降低最终成绩0.5
            if (joursDeRetard > 0) {
                noteFinale1 -= (joursDeRetard * 0.5);
                noteFinale2 -= (joursDeRetard * 0.5);
            }

            // 确保最终成绩不低于0
            noteFinale1 = Math.max(0, noteFinale1);
            noteFinale2 = Math.max(0, noteFinale2);

            // 将成绩添加到成绩表模型
            noteTableModel.addRow(new Object[]{etudiant1, rapport,soutenance1, joursDeRetard, noteFinale1});
            noteTableModel.addRow(new Object[]{etudiant2,rapport, soutenance2, joursDeRetard, noteFinale2});
        }
    }


    // 获取项目名称的方法
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


    // 获取项目交付日期的方法
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


    // 计算两个日期之间的延迟天数的方法
    private int calculateDaysOfDelay(java.sql.Date dateRemise, java.sql.Date dateRemiseEffective) {
        try {
            long diff = dateRemiseEffective.getTime() - dateRemise.getTime();
            return (int) (diff / (1000 * 60 * 60 * 24));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    // 生成PDF报告的方法
    private void generatePDF(DefaultTableModel noteTableModel) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Enregistrer le PDF");

        // 设置文件过滤器以仅显示PDF文件
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Fichiers PDF (*.pdf)", "pdf");
        fileChooser.setFileFilter(filter);

        int userSelection = fileChooser.showSaveDialog(frame);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                // 获取用户选择的文件
                File fileToSave = fileChooser.getSelectedFile();

                if (!fileToSave.getName().toLowerCase().endsWith(".pdf")) {
                    // 确保文件扩展名为.pdf
                    fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".pdf");
                }

                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(fileToSave));
                document.open();

                // 文档标题
                document.add(new Paragraph("Liste des notes des étudiants dans le projet " + projectName));
                document.add(new Paragraph("\n")); // 添加一个空段落（换行）

                // 创建一个比表格模型少一列的PDF表格
                PdfPTable pdfTable = new PdfPTable(noteTableModel.getColumnCount());

                // 列标题
                for (int col = 0; col < noteTableModel.getColumnCount(); col++) {
                    PdfPCell cell = new PdfPCell(new Phrase(noteTableModel.getColumnName(col)));
                    pdfTable.addCell(cell);
                }

                // 表格内容
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


}
