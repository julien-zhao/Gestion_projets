import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Menu {
    JFrame frame = new JFrame();
    JPanel labelPanel = new JPanel(new GridBagLayout());

    Menu() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(420, 420);

        frame.add(labelPanel);

        frame.setVisible(true);

        // Créez des JLabel pour les options
        JLabel gestionEtudiantsLabel = new JLabel("Gestion des Étudiants");
        JLabel gestionProjetsLabel = new JLabel("Gestion des Projets");

        // Configuration des contraintes pour le GridBagLayout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(5, 0, 5, 0); // Ajustez l'inset pour décaler vers la gauche
        gbc.anchor = GridBagConstraints.WEST;

        // Ajoutez les JLabel au labelPanel avec l'effet visuel
        labelPanel.add(addHoverEffect(gestionEtudiantsLabel), gbc);
        gbc.gridy = 1;
        labelPanel.add(addHoverEffect(gestionProjetsLabel), gbc);
    }

    public JLabel addHoverEffect(JLabel label) {
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Ajoutez l'effet ici (par exemple, ouvrez une nouvelle fenêtre)
                if (label.getText().equals("Gestion des Étudiants")) {
                    Gestion_etudiant gestion_etudiant = new Gestion_etudiant();
                } else if (label.getText().equals("Gestion des Projets")) {
                    Gestion_projet gestion_projet = new Gestion_projet();
                }
                frame.setVisible(false); // Masquer la fenêtre du menu
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // Effet visuel lorsque la souris entre
                label.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
                label.setOpaque(true);
                label.setBackground(Color.LIGHT_GRAY);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // Effet visuel lorsque la souris quitte
                label.setBorder(null);
                label.setOpaque(false);
                label.setBackground(null);
            }
        });
        return label;
    }
}
