import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GestionProjetsAppSwing {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
        });
    }

    private static DefaultTableModel tableModel;

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Gestion des Projets Étudiants");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(800, 600));

        // Création du panneau principal
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Création du tableau pour afficher la liste des étudiants
        String[] columnNames = {"ID", "Nom", "Prénom", "Formation", "Promotion"};
        tableModel = new DefaultTableModel(columnNames, 0); // Utilisation du modèle de données

        JTable studentTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(studentTable);

        // Création des boutons pour ajouter et supprimer des étudiants
        JButton addStudentButton = new JButton("Ajouter Étudiant");
        JButton deleteStudentButton = new JButton("Supprimer Étudiant");

        // Création du formulaire pour ajouter un étudiant
        JTextField studentIdField = new JTextField(10);
        JTextField studentNameField = new JTextField(10);
        JTextField studentFirstNameField = new JTextField(10);
        JTextField studentFormationField = new JTextField(10);
        JTextField studentPromotionField = new JTextField(10);

        JButton saveStudentButton = new JButton("Enregistrer");

        // Ajout des composants au panneau principal
        JPanel studentFormPanel = new JPanel(new GridLayout(6, 2));
        studentFormPanel.add(new JLabel("ID :"));
        studentFormPanel.add(studentIdField);
        studentFormPanel.add(new JLabel("Nom :"));
        studentFormPanel.add(studentNameField);
        studentFormPanel.add(new JLabel("Prénom :"));
        studentFormPanel.add(studentFirstNameField);
        studentFormPanel.add(new JLabel("Formation :"));
        studentFormPanel.add(studentFormationField);
        studentFormPanel.add(new JLabel("Promotion :"));
        studentFormPanel.add(studentPromotionField);
        studentFormPanel.add(saveStudentButton);

        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(studentFormPanel, BorderLayout.EAST);
        mainPanel.add(addStudentButton, BorderLayout.SOUTH);
        mainPanel.add(deleteStudentButton, BorderLayout.NORTH);

        frame.add(mainPanel);
        frame.pack();
        frame.setVisible(true);

        // Ajout de gestionnaires d'événements
        addStudentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Récupérer les données du formulaire
                int id = Integer.parseInt(studentIdField.getText());
                String nom = studentNameField.getText();
                String prenom = studentFirstNameField.getText();
                String formation = studentFormationField.getText();
                String promotion = studentPromotionField.getText();

                // Ajouter les données à la table
                tableModel.addRow(new Object[]{id, nom, prenom, formation, promotion});

                // Effacer les champs du formulaire
                studentIdField.setText("");
                studentNameField.setText("");
                studentFirstNameField.setText("");
                studentFormationField.setText("");
                studentPromotionField.setText("");
            }
        });

        deleteStudentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = studentTable.getSelectedRow();
                if (selectedRow != -1) {
                    tableModel.removeRow(selectedRow);
                }
            }
        });

        saveStudentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Code pour enregistrer un étudiant (si nécessaire)
            }
        });
    }
}
