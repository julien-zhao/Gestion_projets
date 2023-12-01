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
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;

public class Affiche_note {
    private static Connection connection;
    private DefaultTableModel tableModel;
    private int projectNumber;
    private String projectName;
    JFrame frame;


    Affiche_note(DefaultTableModel tableModel, int projectNumber) {
        this.tableModel = tableModel;
        this.projectNumber = projectNumber;

        // Établir la connexion à la base de données
        establishDatabaseConnection();

        // Obtenir le nom du projet à partir de la base de données
        projectName = getProjectName(projectNumber);

        // Créer une table avec le modèle de table des notes
        DefaultTableModel noteTableModel = new DefaultTableModel();
        noteTableModel.addColumn("Étudiant");
        noteTableModel.addColumn("Note rapport");
        noteTableModel.addColumn("Note soutenance");
        noteTableModel.addColumn("Jours de retard");
        noteTableModel.addColumn("Note finale");

        calculateAndDisplayNotes(noteTableModel);

        JTable noteTable = new JTable(noteTableModel);

        JScrollPane scrollPane = new JScrollPane(noteTable);

        noteTable.setShowGrid(false);

        JButton generatePDFButton = new JButton("Générer en PDF");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(generatePDFButton);

        generatePDFButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generatePDF(noteTableModel);
            }
        });

        // Ajoutez le trieur de lignes à votre modèle de table
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(noteTableModel);
        noteTable.setRowSorter(sorter);

        frame = new JFrame("Liste des notes des étudiants dans le projet " + projectName);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setPreferredSize(new Dimension(600, 400));
        frame.add(scrollPane);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(x, y);

        JTableHeader header = noteTable.getTableHeader();
        header.setBackground(new Color(108, 190, 213));
        header.setForeground(Color.WHITE); // 设置列头前景颜色
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 26));

        noteTable.setFont(new Font("Arial", Font.PLAIN, 12));
        noteTable.setRowHeight(23);

        noteTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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

        setIcons();

        MouseListener mouseListener = new MouseAdapter() {
            private JDialog dialog;
            @Override
            public void mouseEntered(MouseEvent e) {
                dialog = new JDialog((JFrame) null, "Aide", false);

                ImageIcon icon = new ImageIcon(getClass().getResource("/Picture/logo_D.jpg"));
                dialog.setIconImage(icon.getImage());

                int xOffset = 10;
                int yOffset = 150;
                Point componentPosition = e.getComponent().getLocationOnScreen();
                int xPosition = componentPosition.x + xOffset;
                int yPosition = componentPosition.y - dialog.getHeight() - yOffset;
                dialog.setLocation(xPosition, yPosition);

                JLabel label = new JLabel("<html>- NoteFinale = (rapport + soutenance1) / 2 <br><br> - En cas de retard (Jours de retard > 0), la note finale est diminuée de la moitié du nombre de jours de retard (soit une diminution de 0,5 point par jour). <br><br> - Si vous avez d'autres questions, veuillez contacter : info@dauphine.eu</html>");
                label.setPreferredSize(new Dimension(380, 150));
                label.setMaximumSize(new Dimension(500, 150));
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


        // Ajouter l'icône d'aide
        ImageIcon imageIcon = new ImageIcon(getClass().getResource("/Picture/wenhao.jpeg"));
        Image image = imageIcon.getImage();
        Image newImage = image.getScaledInstance(15, 15,  java.awt.Image.SCALE_SMOOTH); // 调整图像大小
        imageIcon = new ImageIcon(newImage);

        JLabel reminderLabel = new JLabel(imageIcon);
        reminderLabel.addMouseListener(mouseListener);
        buttonPanel.add(reminderLabel, BorderLayout.EAST);


        if ("student".equals(LoginPage.getCurrentUserRole())) {
            // Si le rôle est étudiant, le bouton est caché
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


    // Définir les icônes
    private void setIcons() {
        ImageIcon customIcon = new ImageIcon(getClass().getResource("/Picture/logo_D.jpg"));
        frame.setIconImage(customIcon.getImage());
    }


    // Calculer et afficher les notes
    private void calculateAndDisplayNotes(DefaultTableModel noteTableModel) {
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

            int joursDeRetard = calculateDaysOfDelay(dateRemise, Date.valueOf(dateRemiseEffective));

            if (joursDeRetard > 0) {
                noteFinale1 -= (joursDeRetard * 0.5);
                noteFinale2 -= (joursDeRetard * 0.5);
            }

            noteFinale1 = Math.max(0, noteFinale1);
            noteFinale2 = Math.max(0, noteFinale2);

            noteTableModel.addRow(new Object[]{etudiant1, rapport,soutenance1, joursDeRetard, noteFinale1});
            noteTableModel.addRow(new Object[]{etudiant2,rapport, soutenance2, joursDeRetard, noteFinale2});
        }
    }

    // Récupérer le nom du projet
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


    // Récupérer la date de remise
    static String getDateRemise(int projectNumber) {
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


    // Calculer le nombre de jours de retard
    private int calculateDaysOfDelay(java.sql.Date dateRemise, java.sql.Date dateRemiseEffective) {
        try {
            long diff = dateRemiseEffective.getTime() - dateRemise.getTime();
            return Math.max(0,(int) (diff / (1000 * 60 * 60 * 24)));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    // Générer un PDF
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

                document.add(new Paragraph("Liste des notes des étudiants dans le projet " + projectName));
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
}
