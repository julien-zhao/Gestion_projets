import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.text.MaskFormatter;
import java.text.ParseException;

// 添加项目对话框类
public class ProjectAddDialog {
    private JFrame frame;
    private JTextField matiereField;
    private JTextField sujetField;
    private JFormattedTextField dateRemiseField;
    private DefaultTableModel tableModel;
    private Connection connection;

    // 构造函数
    public ProjectAddDialog(DefaultTableModel tableModel, Connection connection) {
        this.tableModel = tableModel;
        this.connection = connection;

        frame = new JFrame("Ajouter un Projet");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        ImageIcon icon = new ImageIcon("C:\\Users\\MATEBOOK14\\Desktop\\Gestion_projets\\logo_D.jpg"); // 替换为实际图标文件的路径
        frame.setIconImage(icon.getImage()); // 设置 JFrame 的图标

        JPanel mainPanel = new JPanel(new GridLayout(6, 2));

        matiereField = new JTextField(10);
        sujetField = new JTextField(10);

        try {
            MaskFormatter dateMask = new MaskFormatter("####-##-##");
            dateMask.setPlaceholderCharacter('_'); // Remplacement character for dashes
            dateRemiseField = new JFormattedTextField(dateMask);
            dateRemiseField.setColumns(10);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        mainPanel.add(new JLabel("Nom Matière :"));
        mainPanel.add(matiereField);
        mainPanel.add(new JLabel("Sujet :"));
        mainPanel.add(sujetField);
        mainPanel.add(new JLabel("Date de Remise (YYYY-MM-DD) :"));
        mainPanel.add(dateRemiseField);

        JButton addButton = new JButton("Ajouter");
        JButton clearButton = new JButton("Effacer");

        // 添加事件处理器到"Ajouter"按钮
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateFields()) {
                    try {
                        addProject();
                        // Afficher la fenêtre de confirmation
                        JOptionPane.showMessageDialog(frame, "Projet ajouté avec succès!", "Confirmation", JOptionPane.INFORMATION_MESSAGE);
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(frame, "Erreur lors de l'ajout du projet : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Veuillez remplir tous les champs correctement.", "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 添加事件处理器到"Effacer"按钮
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });

        mainPanel.add(addButton);
        mainPanel.add(clearButton);

        frame.add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // 添加项目的方法
    private void addProject() throws SQLException {
        String matiere = matiereField.getText();
        String sujet = sujetField.getText();
        String dateRemise = dateRemiseField.getText();

        if (isValidDateFormat(dateRemise)) {
            String sql = "INSERT INTO Projets (nom_matiere, sujet, date_remise) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, matiere);
            preparedStatement.setString(2, sujet);
            preparedStatement.setString(3, dateRemise);
            preparedStatement.executeUpdate();

            tableModel.addRow(new Object[]{getLastInsertedProjectId(), matiere, sujet, dateRemise});
            frame.dispose();
        } else {
            throw new SQLException("Format de date incorrect. Utilisez le format YYYY-MM-DD.");
        }
    }

    // 验证日期格式的方法
    private boolean isValidDateFormat(String date) {
        String regex = "\\d{4}-\\d{2}-\\d{2}";
        if (date.matches(regex)) {
            String[] dateParts = date.split("-");
            int year = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]);
            int day = Integer.parseInt(dateParts[2]);

            // 验证有效月份 (1-12)
            if (month >= 1 && month <= 12) {
                // 验证每月的有效天数
                int[] daysInMonth = {0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
                if (month == 2 && isLeapYear(year)) {
                    return day >= 1 && day <= 29;
                } else {
                    return day >= 1 && day <= daysInMonth[month];
                }
            }
        }
        return false;
    }

    // 判断是否是闰年的方法
    private boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    // 验证字段的方法
    private boolean validateFields() {
        return !matiereField.getText().isEmpty() && !sujetField.getText().isEmpty() && !dateRemiseField.getText().isEmpty();
    }

    // 清空字段的方法
    private void clearFields() {
        matiereField.setText("");
        sujetField.setText("");
        dateRemiseField.setValue(null);
    }

    // 获取最后插入的项目编号的方法
    private int getLastInsertedProjectId() {
        int lastInsertedId = -1;
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT LAST_INSERT_ID() as last_id");
            if (resultSet.next()) {
                lastInsertedId = resultSet.getInt("last_id");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return lastInsertedId;
    }
}
