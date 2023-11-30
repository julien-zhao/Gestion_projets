import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class Menu {
    JFrame frame = new JFrame(" Gestion de projets des étudiants");
    JPanel panel2 = new JPanel(new GridBagLayout());

    Menu() {
        initializeFrame();

        setContentPaneBackground();

        setIcons();

        setPanelTransparency();

        addTitleLabel();

        addLogoutButton();

        addPanelsToFrame();

        addLabelsToPanel2();

        frame.setVisible(true);

    }

    // Initialise la fenêtre principale
    private void initializeFrame() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLayout(null);

        // Chargement de l'icône depuis le classpath
        ImageIcon icon = new ImageIcon(getClass().getResource("/Picture/logo_D.jpg"));
        Image iconImage = icon.getImage();
        frame.setIconImage(iconImage);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(x, y);
    }


    // Ajoute le titre à la fenêtre principale
    private void addTitleLabel() {
        JLabel titleLabel = new JLabel(" Gestion de projets des étudiants");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 45));
        titleLabel.setForeground(new Color(87, 157, 180));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        int verticalMargin = 140;
        titleLabel.setBorder(BorderFactory.createEmptyBorder(verticalMargin, 0, verticalMargin, 0));

        frame.setLayout(new BorderLayout());
        frame.add(titleLabel, BorderLayout.NORTH);
    }


    // Définit l'arrière-plan du contenu de la fenêtre
    private void setContentPaneBackground() {
        frame.setContentPane(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));

                // Chargement de l'image depuis le classpath
                ImageIcon icon = new ImageIcon(getClass().getResource("/Picture/uni_logo.jpeg"));
                Image bgImage = icon.getImage();

                g2d.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                g2d.dispose();
            }
        });
    }



    // Définit la transparence du panneau
    private void setPanelTransparency() {
        panel2.setOpaque(false);
    }


    // Ajoute les panneaux à la fenêtre
    private void addPanelsToFrame() {
        frame.add(panel2);
    }


    // Définit les icônes de la fenêtre
    private void setIcons() {
        ImageIcon customIcon = new ImageIcon(getClass().getResource("/Picture/logo_D.jpg"));
        frame.setIconImage(customIcon.getImage());
    }


    // Ajoute les libellés au panneau2
    private void addLabelsToPanel2() {
        ImageIcon etudiantsIcon = new ImageIcon(getClass().getResource("/Picture/etu.png"));
        ImageIcon projetsIcon = new ImageIcon(getClass().getResource("/Picture/proj.png"));

        JLabel gestionEtudiantLabel = createStyledButton("Gestion des étudiants", etudiantsIcon);
        JLabel gestionProjetLabel = createStyledButton("Gestion des projets", projetsIcon);

        gestionEtudiantLabel.setFont(new Font("Arial", Font.BOLD, 13));
        gestionProjetLabel.setFont(new Font("Arial", Font.BOLD, 13));

        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.insets = new Insets(0, 20, 150, 0);
        gbc1.anchor = GridBagConstraints.LINE_START;
        panel2.add(gestionEtudiantLabel, gbc1);

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 1;
        gbc2.gridy = 0;
        gbc2.insets = new Insets(0, 20, 150, 0);
        gbc2.anchor = GridBagConstraints.LINE_START;
        panel2.add(gestionProjetLabel, gbc2);
    }


    // Ajoute le bouton de déconnexion à la fenêtre
    private void addLogoutButton() {
        JButton logoutButton = new JButton("Déconnexion");

        // Définit la police, la couleur de fond et la couleur avant-plan du bouton
        Font buttonFont = new Font("Arial", Font.BOLD, 14);
        logoutButton.setFont(buttonFont);
        logoutButton.setBackground(new Color(255, 182, 193)); // 浅红色的 RGB 值
        logoutButton.setForeground(Color.BLACK);

        // 鼠标进入按钮时加深颜色
        logoutButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                logoutButton.setBackground(new Color(255, 140, 149)); // 加深红色的 RGB 值
            }

            @Override
            public void mouseExited(MouseEvent e) {
                logoutButton.setBackground(new Color(255, 182, 193)); // 恢复浅红色的 RGB 值
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = JOptionPane.showConfirmDialog(frame, "Voulez-vous vraiment vous déconnecter ?", "Confirmation", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    LoginPage loginPage = new LoginPage();
                    frame.dispose(); // Fermez la fenêtre actuelle
                }
            }
        });

        // Ajoutez le bouton de déconnexion à la barre de titre
        frame.add(logoutButton, BorderLayout.SOUTH);
    }


    // Crée un libellé stylisé avec une icône
    private JLabel createStyledButton(String text, ImageIcon icon) {
        JLabel label = new JLabel(text);

        Border roundedBorder = new LineBorder(new Color(87, 157, 180), 5, true);

        label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (text != null) {
                    if (text.equals("Gestion des étudiants")) {
                        Gestion_etudiant gestion_etudiant = new Gestion_etudiant();
                    } else if (text.equals("Gestion des projets")) {
                        Gestion_projet gestion_projet = new Gestion_projet();
                    }
                    frame.setVisible(false);
                }
            }

            public void mouseEntered(MouseEvent evt) {
                label.setBackground(new Color(87, 157, 180));
                label.setBorder(roundedBorder);
            }

            public void mouseExited(MouseEvent evt) {
                label.setBackground(new Color(108, 190, 213));
                label.setBorder(roundedBorder);
            }
        });

        label.setOpaque(true);
        label.setBackground(new Color(108, 190, 213));
        label.setPreferredSize(new Dimension(300, 100));

        if (icon != null) {
            Image originalImage = icon.getImage();
            Image scaledImage = originalImage.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            label.setIcon(scaledIcon);
        }

        label.setBorder(roundedBorder);

        return label;
    }
}
