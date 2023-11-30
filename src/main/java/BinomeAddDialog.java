import javax.swing.*;
import javax.swing.text.MaskFormatter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


public class BinomeAddDialog extends JFrame {
    private Connection connection;
    private JFrame parentFrame;
    private DefaultTableModel tableModel;
    private int projectNumber = -1;
    private Set<String> studentsInBinome = new HashSet<>();


    // Constructeur de la classe BinomeAddDialog
    public BinomeAddDialog(Connection connection, JFrame parentFrame, DefaultTableModel tableModel, int projectNumber) {
        this.connection = connection;
        this.parentFrame = parentFrame;
        this.tableModel = tableModel;
        this.projectNumber = projectNumber;

        setTitle("Ajouter Binôme");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(new Dimension(400, 250));

        JPanel mainPanel = new JPanel(new GridLayout(7, 2));

        initializeStudentsInBinome();
        JComboBox<String> student1ComboBox = new JComboBox<>(getStudentOptions());
        JComboBox<String> student2ComboBox = new JComboBox<>(getStudentOptions());

        JTextField rapportField = new JTextField(10);
        JTextField soutenance1Field = new JTextField(10);
        JTextField soutenance2Field = new JTextField(10);

        rapportField.setText("0");
        soutenance1Field.setText("0");
        soutenance2Field.setText("0");

        MaskFormatter dateMask = null;
        try {
            dateMask = new MaskFormatter("####-##-##"); // (YYYY-MM-DD)
            dateMask.setPlaceholderCharacter('_');
        } catch (ParseException e) {
            e.printStackTrace();
        }
        JFormattedTextField dateRemiseEffectiveField = new JFormattedTextField(dateMask);
        dateRemiseEffectiveField.setColumns(10);

        mainPanel.add(new JLabel("Étudiant 1 :"));
        mainPanel.add(student1ComboBox);
        mainPanel.add(new JLabel("Étudiant 2 :"));
        mainPanel.add(student2ComboBox);
        mainPanel.add(new JLabel("Note Rapport :"));
        mainPanel.add(rapportField);
        mainPanel.add(new JLabel("Note Soutenance Étudiant 1 :"));
        mainPanel.add(soutenance1Field);
        mainPanel.add(new JLabel("Note Soutenance Étudiant 2 :"));
        mainPanel.add(soutenance2Field);
        mainPanel.add(new JLabel("Date de Remise Effective (YYYY-MM-DD) :"));
        mainPanel.add(dateRemiseEffectiveField);

        JButton validerButton = new JButton("Valider");
        JButton effacerButton = new JButton("Effacer");
        mainPanel.add(validerButton);
        mainPanel.add(effacerButton);

        add(mainPanel);

        ImageIcon icon = new ImageIcon(getClass().getResource("/Picture/logo_D.jpg"));
        setIconImage(icon.getImage());

        //Button pour valider
        validerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Vérifier l'indicateur avant d'autoriser la réouverture de la boîte de dialogue

                String selectedStudent1 = (String) student1ComboBox.getSelectedItem();
                String selectedStudent2 = (String) student2ComboBox.getSelectedItem();
                String rapport = rapportField.getText();
                String soutenance1 = soutenance1Field.getText();
                String soutenance2 = soutenance2Field.getText();
                String dateRemiseEffective = dateRemiseEffectiveField.getText().replace('_', '-');

                if (!selectedStudent1.isEmpty() && (!selectedStudent2.isEmpty() || selectedStudent2 == null) && !rapport.isEmpty() && !soutenance1.isEmpty() && !soutenance2.isEmpty() && !dateRemiseEffective.isEmpty() && isValidDateFormat(dateRemiseEffective) && isValidGrade(rapport) && isValidGrade(soutenance1) && isValidGrade(soutenance2)) {
                    int projectId = projectNumber;
                    int student1Id = getStudentId(selectedStudent1);
                    int student2Id = getStudentId(selectedStudent2);

                    String tableName = "Project_" + projectId;

                    try {
                        String insertSql = "INSERT INTO " + tableName +
                                " (projet_numero, etudiant1_numero, etudiant2_numero, note_rapport, note_soutenance_etu1, note_soutenance_etu2, date_remise_effective) VALUES (?, ?, ?, ?, ?, ?, ?)";
                        PreparedStatement preparedStatement = connection.prepareStatement(insertSql);
                        preparedStatement.setInt(1, projectId);
                        preparedStatement.setInt(2, student1Id);
                        preparedStatement.setInt(3, student2Id);
                        preparedStatement.setString(4, rapport);
                        preparedStatement.setString(5, soutenance1);
                        preparedStatement.setString(6, soutenance2);
                        preparedStatement.setString(7, dateRemiseEffective);
                        preparedStatement.executeUpdate();

                        int binomeId = getLastInsertedBinomeId();

                        tableModel.addRow(new Object[]{binomeId, projectNumber, selectedStudent1, selectedStudent2, rapport, soutenance1, soutenance2, dateRemiseEffective});

                        student1ComboBox.setSelectedIndex(0);
                        student2ComboBox.setSelectedIndex(0);
                        rapportField.setText("");
                        soutenance1Field.setText("");
                        soutenance2Field.setText("");
                        dateRemiseEffectiveField.setText("");

                        // Fermez la fenêtre après avoir ajouté le binôme avec succès
                        setVisible(false);
                        JOptionPane.showMessageDialog(parentFrame, "Binôme ajouté avec succès!", "Confirmation", JOptionPane.INFORMATION_MESSAGE);


                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(parentFrame, "Veuillez remplir tous les champs correctement et assurez-vous que les notes sont entre 0 et 20.", "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });


        //Button pour effacer le contenu
        effacerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                student1ComboBox.setSelectedIndex(0);
                student2ComboBox.setSelectedIndex(0);
                rapportField.setText("");
                soutenance1Field.setText("");
                soutenance2Field.setText("");
                dateRemiseEffectiveField.setText("");
            }
        });
        setLocationRelativeTo(parentFrame);
        setVisible(true);
    }


    // Méthode pour obtenir l'ID de l'étudiant
    private int getStudentId(String studentName) {
        int studentId = -1;
        try {
            String[] names = studentName.split(" ");
            String firstName = names[0];
            String lastName = names[1];

            String query = "SELECT numero FROM Etudiants WHERE nom = ? AND prenom = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, lastName);
            preparedStatement.setString(2, firstName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                studentId = resultSet.getInt("numero");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return studentId;
    }


    // Méthode pour vérifier le format de la date
    private boolean isValidDateFormat(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);

        try {
            Date parsedDate = sdf.parse(date);
            return parsedDate != null;
        } catch (ParseException e) {
            return false;
        }
    }


    // Méthode pour obtenir l'ID du dernier binôme inséré
    private int getLastInsertedBinomeId() {
        int binomeId = -1;
        try {
            String query = "SELECT MAX(numero) as max_binome_id FROM Binomes";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                binomeId = resultSet.getInt("max_binome_id");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return binomeId;
    }


    // Méthode pour obtenir la liste des options d'étudiants
    private DefaultComboBoxModel<String> getStudentOptions() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        try {
            String query = "SELECT nom, prenom, numero FROM Etudiants";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String studentLastName = resultSet.getString("nom");
                String studentFirstName = resultSet.getString("prenom");
                int studentId = resultSet.getInt("numero");
                String fullName = studentFirstName + " " + studentLastName;

                // Exclude students already in a binome
                if (!studentsInBinome.contains(String.valueOf(studentId))) {
                    model.addElement(fullName);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return model;
    }


    // Méthode pour vérifier si la note est entre 0 et 20
    private boolean isValidGrade(String grade) {
        try {
            double value = Double.parseDouble(grade);
            return value >= 0 && value <= 20;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Méthode pour initialiser les étudiants dans le binôme
    private void initializeStudentsInBinome() {
        try {
            String name = "project_" + projectNumber;
            String query = "SELECT DISTINCT etudiant1_numero, etudiant2_numero FROM " + name;

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int student1 = resultSet.getInt("etudiant1_numero");
                int student2 = resultSet.getInt("etudiant2_numero");
                studentsInBinome.add(String.valueOf(student1));
                studentsInBinome.add(String.valueOf(student2));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
