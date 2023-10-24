import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

public class Gestion_binome {
    private static Connection connection = null;
    private static DefaultTableModel tableModel;

    JFrame frame = new JFrame("Gestion des Binômes");

    Gestion_binome() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_projets", "root", "root");
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1200, 600));
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Création du tableau pour afficher la liste des binômes
        String[] columnNames = {"ID", "Projet", "Étudiant 1", "Étudiant 2", "Note Rapport", "Note Soutenance Étudiant 1", "Note Soutenance Étudiant 2", "Date de Remise Effective"};
        tableModel = new DefaultTableModel(columnNames, 0);

        // Création du panneau principal
        JTable binomeTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(binomeTable);

        // Création des boutons pour ajouter et supprimer des binômes
        JButton addBinomeButton = new JButton("Ajouter Binôme");
        JButton deleteBinomeButton = new JButton("Supprimer Binôme");

        // Bouton "Retour au Menu"
        JButton retourMenuButton = new JButton("Retour au Menu");

        retourMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Fermez l'interface de gestion des binômes et affichez l'interface de menu
                frame.dispose(); // Ferme l'interface de gestion des binômes
                new Menu(); // Crée une nouvelle instance de l'interface de menu
            }
        });

        // Création du formulaire pour ajouter un binôme
        JComboBox<String> projectComboBox = new JComboBox<>();
        JComboBox<String> student1ComboBox = new JComboBox<>();
        JComboBox<String> student2ComboBox = new JComboBox<>();
        JTextField rapportField = new JTextField(10);
        JTextField soutenance1Field = new JTextField(10);
        JTextField soutenance2Field = new JTextField(10);
        JTextField dateRemiseEffectiveField = new JTextField(10);

        // Ajout des composants au panneau principal
        JPanel binomeFormPanel = new JPanel(new GridLayout(8, 2));
        binomeFormPanel.add(new JLabel("Projet :"));
        binomeFormPanel.add(projectComboBox);
        binomeFormPanel.add(new JLabel("Étudiant 1 :"));
        binomeFormPanel.add(student1ComboBox);
        binomeFormPanel.add(new JLabel("Étudiant 2 :"));
        binomeFormPanel.add(student2ComboBox);
        binomeFormPanel.add(new JLabel("Note Rapport :"));
        binomeFormPanel.add(rapportField);
        binomeFormPanel.add(new JLabel("Note Soutenance Étudiant 1 :"));
        binomeFormPanel.add(soutenance1Field);
        binomeFormPanel.add(new JLabel("Note Soutenance Étudiant 2 :"));
        binomeFormPanel.add(soutenance2Field);
        binomeFormPanel.add(new JLabel("Date de Remise Effective (YYYY-MM-DD) :"));
        binomeFormPanel.add(dateRemiseEffectiveField);

        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(binomeFormPanel, BorderLayout.EAST);

// Créez un panneau pour les boutons "Ajouter Binôme" et "Retour au Menu"
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addBinomeButton);
        buttonPanel.add(deleteBinomeButton);
        buttonPanel.add(retourMenuButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);


        frame.add(mainPanel);
        frame.pack();
        frame.setVisible(true);
        loadBinomesFromDatabase();

        // Charger les projets et les étudiants dans les combobox
        loadProjectsIntoComboBox(projectComboBox);
        loadStudentsIntoComboBox(student1ComboBox, student2ComboBox);

        // Gestionnaire d'événements pour le bouton "Ajouter Binôme"
        addBinomeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedProject = (String) projectComboBox.getSelectedItem();
                String selectedStudent1 = (String) student1ComboBox.getSelectedItem();
                String selectedStudent2 = (String) student2ComboBox.getSelectedItem();
                String rapport = rapportField.getText();
                String soutenance1 = soutenance1Field.getText();
                String soutenance2 = soutenance2Field.getText();
                String dateRemiseEffective = dateRemiseEffectiveField.getText();

                if (isValidDateFormat(dateRemiseEffective)) {
                    try {
                        // Récupérer les IDs correspondants aux noms sélectionnés
                        int projectId = getProjectId(selectedProject);
                        int student1Id = getStudentId(selectedStudent1);
                        int student2Id = getStudentId(selectedStudent2);

                        String sql = "INSERT INTO Binomes (projet_numero, etudiant1_numero, etudiant2_numero, " +
                                "note_rapport, note_soutenance_etu1, note_soutenance_etu2, date_remise_effective) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?)";
                        PreparedStatement preparedStatement = connection.prepareStatement(sql);
                        preparedStatement.setInt(1, projectId);
                        preparedStatement.setInt(2, student1Id);
                        preparedStatement.setInt(3, student2Id);
                        preparedStatement.setString(4, rapport);
                        preparedStatement.setString(5, soutenance1);
                        preparedStatement.setString(6, soutenance2);
                        preparedStatement.setString(7, dateRemiseEffective);
                        preparedStatement.executeUpdate();




                        int binomeId = getLastInsertedBinomeId(); // Récupérer l'ID du binôme inséré

                        // Maintenant, insérez également un enregistrement dans la table de notes
                        String sqlNote = "INSERT INTO Notes (etudiant_id, projet_id, note_rapport, note_soutenance, note_finale) " +
                                "VALUES (?, ?, ?, ?, ?)";
                        PreparedStatement preparedStatementNote = connection.prepareStatement(sqlNote);
                        preparedStatementNote.setInt(1, student1Id); // Insérez le premier étudiant
                        preparedStatementNote.setInt(2, projectId);
                        preparedStatementNote.setString(3, rapport);
                        preparedStatementNote.setString(4, soutenance1);
                        preparedStatementNote.setString(5, "0");
                        preparedStatementNote.executeUpdate();

                        String sqlNote2 = "INSERT INTO Notes (etudiant_id, projet_id, note_rapport, note_soutenance, note_finale) " +
                                "VALUES (?, ?, ?, ?, ?)";
                        PreparedStatement preparedStatementNote2 = connection.prepareStatement(sqlNote2);
                        // Répétez le processus pour le deuxième étudiant du binôme
                        preparedStatementNote2.setInt(1, student2Id); // Insérez le deuxième étudiant
                        preparedStatementNote2.setInt(2, projectId);
                        preparedStatementNote2.setString(3, rapport);
                        preparedStatementNote2.setString(4, soutenance2);
                        preparedStatementNote2.setString(5, "0");
                        preparedStatementNote2.executeUpdate();



                        tableModel.addRow(new Object[]{getLastInsertedBinomeId(), selectedProject, selectedStudent1, selectedStudent2, rapport, soutenance1, soutenance2, dateRemiseEffective});

                        // Effacer les champs du formulaire
                        projectComboBox.setSelectedIndex(0);
                        student1ComboBox.setSelectedIndex(0);
                        student2ComboBox.setSelectedIndex(0);
                        rapportField.setText("");
                        soutenance1Field.setText("");
                        soutenance2Field.setText("");
                        dateRemiseEffectiveField.setText("");







                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Format de date incorrect. Utilisez le format YYYY-MM-DD.", "Erreur", JOptionPane.ERROR_MESSAGE);
                }



            }
        });

        // Gestionnaire d'événements pour le bouton "Supprimer Binôme"
        deleteBinomeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = binomeTable.getSelectedRow();
                if (selectedRow != -1) {
                    int binomeIdToDelete = (int) tableModel.getValueAt(selectedRow, 0);

                    try {
                        String deleteSql = "DELETE FROM Binomes WHERE numero = ?";
                        PreparedStatement preparedStatement = connection.prepareStatement(deleteSql);
                        preparedStatement.setInt(1, binomeIdToDelete);
                        preparedStatement.executeUpdate();

                        tableModel.removeRow(selectedRow);
                        projectComboBox.setSelectedIndex(0);
                        student1ComboBox.setSelectedIndex(0);
                        student2ComboBox.setSelectedIndex(0);
                        rapportField.setText("");
                        soutenance1Field.setText("");
                        soutenance2Field.setText("");
                        dateRemiseEffectiveField.setText("");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    private static void loadBinomesFromDatabase() {
        try {
            String sql = "SELECT b.numero, p.nom_matiere, e1.nom AS etudiant1, e2.nom AS etudiant2, " +
                    "b.note_rapport, b.note_soutenance_etu1, b.note_soutenance_etu2, b.date_remise_effective " +
                    "FROM Binomes b " +
                    "JOIN Projets p ON b.projet_numero = p.numero " +
                    "JOIN Etudiants e1 ON b.etudiant1_numero = e1.numero " +
                    "JOIN Etudiants e2 ON b.etudiant2_numero = e2.numero";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("numero");
                String project = resultSet.getString("nom_matiere");
                String student1 = resultSet.getString("etudiant1");
                String student2 = resultSet.getString("etudiant2");
                String rapport = resultSet.getString("note_rapport");
                String soutenance1 = resultSet.getString("note_soutenance_etu1");
                String soutenance2 = resultSet.getString("note_soutenance_etu2");
                String dateRemiseEffective = resultSet.getString("date_remise_effective");

                tableModel.addRow(new Object[]{id, project, student1, student2, rapport, soutenance1, soutenance2, dateRemiseEffective});
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Charger les noms de projet dans le JComboBox
    private void loadProjectsIntoComboBox(JComboBox<String> comboBox) {
        try {
            String sql = "SELECT nom_matiere FROM Projets";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                comboBox.addItem(resultSet.getString("nom_matiere"));
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Charger les noms d'étudiants dans les JComboBox
    private void loadStudentsIntoComboBox(JComboBox<String> comboBox1, JComboBox<String> comboBox2) {
        try {
            String sql = "SELECT nom FROM Etudiants";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                comboBox1.addItem(resultSet.getString("nom"));
                comboBox2.addItem(resultSet.getString("nom"));
            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Méthode pour valider le format de date
    private boolean isValidDateFormat(String date) {
        String regex = "\\d{4}-\\d{2}-\\d{2}";
        return date.matches(regex);
    }

    // Méthode pour obtenir l'ID du projet en fonction de son nom
    private int getProjectId(String projectName) {
        try {
            String query = "SELECT numero FROM Projets WHERE nom_matiere = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, projectName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("numero");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    // Méthode pour obtenir l'ID de l'étudiant en fonction de son nom
    private int getStudentId(String studentName) {
        try {
            String query = "SELECT numero FROM Etudiants WHERE nom = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, studentName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("numero");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    // Méthode pour obtenir le dernier numéro de binôme inséré
    private int getLastInsertedBinomeId() {
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




    public static int calculateDaysLate(Date dateRemiseEffective, Date datePrevueRemise) {
        // Calculez la différence en millisecondes entre les deux dates
        long differenceMillis = dateRemiseEffective.getTime() - datePrevueRemise.getTime();

        // Convertissez la différence en jours en divisant par le nombre de millisecondes par jour
        int joursDeRetard = (int) (differenceMillis / (1000 * 60 * 60 * 24));

        // Si la date de remise effective est antérieure à la date prévue, il n'y a pas de retard
        if (joursDeRetard < 0) {
            return 0;
        }

        return joursDeRetard;
    }

    // Méthode pour calculer la note finale en fonction des notes de rapport et de soutenance et du retard
    private static double calculateFinalGrade(double noteRapport, double noteSoutenance, int joursDeRetard) {
        // Vous pouvez ajuster la logique de calcul de la note finale ici, en fonction des critères
        // fournis. Par exemple, déduire 1 point par jour de retard.
        double noteFinale = noteRapport + noteSoutenance - (joursDeRetard * 1.0);

        // Assurez-vous que la note finale ne tombe pas en dessous de zéro
        if (noteFinale < 0) {
            noteFinale = 0;
        }

        return noteFinale;
    }

    // Méthode pour obtenir la date prévue de remise du projet en fonction du nom du projet
    private static Date getProjectDueDate(String projet) {
        try {
            String sql = "SELECT date_remise FROM Projets WHERE nom_matiere = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, projet);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Date dateRemise = resultSet.getDate("date_remise");
                resultSet.close();
                preparedStatement.close();
                return dateRemise;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return new Date(); // Renvoyer la date actuelle en cas d'erreur
    }


}
