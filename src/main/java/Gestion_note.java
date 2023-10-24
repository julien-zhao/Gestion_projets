import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Gestion_note {
    private static Connection connection = null;
    private static DefaultTableModel tableModel;

    JFrame frame = new JFrame("Gestion des Notes");

    public Gestion_note() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/gestion_projets", "root", "root");
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 600));
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Création du tableau pour afficher la liste des notes
        String[] columnNames = {"ID", "Étudiant", "Projet", "Note de Rapport", "Note de Soutenance", "Note Finale"};
        tableModel = new DefaultTableModel(columnNames, 0);

        // Création du panneau principal
        JTable notesTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(notesTable);

        // Bouton "Retour au Menu"
        JButton retourMenuButton = new JButton("Retour au Menu");

        retourMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Fermez l'interface de gestion des notes et affichez l'interface de menu
                frame.dispose(); // Ferme l'interface de gestion des notes
                new Menu(); // Crée une nouvelle instance de l'interface de menu
            }
        });

        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(retourMenuButton, BorderLayout.SOUTH);
        frame.add(mainPanel);

        frame.pack();
        frame.setVisible(true);

        loadNotesFromDatabase();
    }

    private static void loadNotesFromDatabase() {
        tableModel.setRowCount(0);

        try {
            // Créez une requête SQL pour récupérer les notes en vérifiant le numéro de projet
            String sql = "SELECT DISTINCT n.numero, e1.nom AS etudiant1, e2.nom AS etudiant2, p.nom_matiere AS projet, n.note_rapport, n.note_soutenance, n2.note_soutenance AS note_soutenance_etudiant2, b.date_remise_effective " +
                    "FROM Notes n " +
                    "JOIN Etudiants e1 ON n.etudiant_id = e1.numero " +
                    "JOIN Projets p ON n.projet_id = p.numero " +
                    "JOIN Binomes b ON n.etudiant_id = b.etudiant1_numero AND n.projet_id = b.projet_numero " +
                    "JOIN Etudiants e2 ON b.etudiant2_numero = e2.numero " +
                    "JOIN Notes n2 ON n.projet_id = n2.projet_id AND b.etudiant2_numero = n2.etudiant_id";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("numero");
                String etudiant1 = resultSet.getString("etudiant1");
                String etudiant2 = resultSet.getString("etudiant2");
                String projet = resultSet.getString("projet");
                double noteRapport = resultSet.getDouble("note_rapport");
                double noteSoutenance = resultSet.getDouble("note_soutenance");
                double noteSoutenance2 = resultSet.getDouble("note_soutenance_etudiant2");
                Date dateRemiseEffective = resultSet.getDate("date_remise_effective");
                Date dateRemisePrevue = getProjectDueDate(projet);

                int joursDeRetard = calculateDaysLate(dateRemiseEffective, dateRemisePrevue);
                double noteFinale = calculateFinalGrade(noteRapport, noteSoutenance, joursDeRetard);
                double noteFinale2 = calculateFinalGrade(noteRapport, noteSoutenance2, joursDeRetard);

                // Ajoutez les données au tableau du modèle
                tableModel.addRow(new Object[]{id, etudiant1, projet, noteRapport, noteSoutenance, noteFinale});
                tableModel.addRow(new Object[]{id, etudiant2, projet, noteRapport, noteSoutenance2, noteFinale2});

            }

            resultSet.close();
            preparedStatement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Méthode pour calculer le nombre de jours de retard
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
