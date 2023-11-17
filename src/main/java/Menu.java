import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;



public class Menu {
    JFrame frame = new JFrame(" Gestion de projets des étudiants");
    JPanel panel2 = new JPanel(new GridBagLayout());


    Menu() {
        // 初始化窗体
        initializeFrame();

        // 设置内容面板的背景
        setContentPaneBackground();

        // 设置窗体图标
        setIcons();

        // 设置内容面板透明
        setPanelTransparency();

        // 添加标题
        addTitleLabel();

        // 将面板添加到窗体
        addPanelsToFrame();

        // 添加标签到面板
        addLabelsToPanel2();

        // 设置窗体可见
        frame.setVisible(true);

    }



    private void initializeFrame() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLayout(null);
        frame.setIconImage(new ImageIcon("C:\\Users\\MATEBOOK14\\Desktop\\Gestion_projets\\logo_D.jpg").getImage());
        // 将窗口设置为屏幕中央
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - frame.getWidth()) / 2;
        int y = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(x, y);
    }


    private void addTitleLabel() {
        JLabel titleLabel = new JLabel(" Gestion de projets des étudiants");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 45));
        titleLabel.setForeground(new Color(87, 157, 180));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // 添加标题并为其添加空白边框（上方和下方）
        int verticalMargin = 140;  // 调整上下方空白的大小
        titleLabel.setBorder(BorderFactory.createEmptyBorder(verticalMargin, 0, verticalMargin, 0));

        // 使用 BorderLayout 将标题放在 frame 的北部（上方）
        frame.setLayout(new BorderLayout());
        frame.add(titleLabel, BorderLayout.NORTH);
    }


    private void setContentPaneBackground() {
        frame.setContentPane(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
                Image bgImage = new ImageIcon("C:\\Users\\MATEBOOK14\\Desktop\\Gestion_projets\\uni_logo.jpeg").getImage();
                g2d.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                g2d.dispose();
            }
        });
    }

    private void setPanelTransparency() {
        panel2.setOpaque(false);
    }

    private void addPanelsToFrame() {
        frame.add(panel2);
    }

    private void setIcons() {
        ImageIcon customIcon = new ImageIcon("C:\\Users\\MATEBOOK14\\Desktop\\Gestion_projets\\logo_D.jpg");
        frame.setIconImage(customIcon.getImage());
    }

    private void addLabelsToPanel2() {
        ImageIcon etudiantsIcon = new ImageIcon("C:\\Users\\MATEBOOK14\\Desktop\\Gestion_projets\\etu.png");
        ImageIcon projetsIcon = new ImageIcon("C:\\Users\\MATEBOOK14\\Desktop\\Gestion_projets\\proj.png");

        JLabel gestionEtudiantLabel = createStyledButton("Gestion des étudiants", etudiantsIcon);
        JLabel gestionProjetLabel = createStyledButton("Gestion des projets", projetsIcon);

        gestionEtudiantLabel.setFont(new Font("Arial", Font.BOLD, 13));
        gestionProjetLabel.setFont(new Font("Arial", Font.BOLD, 13));

        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.insets = new Insets(0, 20, 150, 0);  // 调整间距
        gbc1.anchor = GridBagConstraints.LINE_START;
        panel2.add(gestionEtudiantLabel, gbc1);

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 1;
        gbc2.gridy = 0;
        gbc2.insets = new Insets(0, 20, 150, 0);  // 调整间距
        gbc2.anchor = GridBagConstraints.LINE_START;
        panel2.add(gestionProjetLabel, gbc2);
    }



    private JLabel createStyledButton(String text, ImageIcon icon) {
        JLabel label = new JLabel(text);

        // 创建一个具有圆角的边框
        Border roundedBorder = new LineBorder(new Color(87, 157, 180), 5, true);

        label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (text != null) {
                    if (text.equals("Gestion des étudiants")) {
                        Gestion_etudiant gestion_etudiant = new Gestion_etudiant();
                        System.out.println("Gestion des étudiants clicked!");
                    } else if (text.equals("Gestion des projets")) {
                        Gestion_projet gestion_projet = new Gestion_projet();
                        System.out.println("Gestion des projets clicked!");
                    }
                    frame.setVisible(false); // 隐藏菜单窗口
                }
            }

            public void mouseEntered(MouseEvent evt) {
                // 设置鼠标悬停时的背景颜色和边框
                label.setBackground(new Color(87, 157, 180));
                label.setBorder(roundedBorder);
            }

            public void mouseExited(MouseEvent evt) {
                // 设置鼠标离开时的背景颜色和边框
                label.setBackground(new Color(108, 190, 213));
                label.setBorder(roundedBorder);
            }
        });

        label.setOpaque(true);
        label.setBackground(new Color(108, 190, 213));
        label.setPreferredSize(new Dimension(300, 100));

        // 调整图片大小
        if (icon != null) {
            Image originalImage = icon.getImage();
            Image scaledImage = originalImage.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            label.setIcon(scaledIcon);
        }

        // 设置圆角边框
        label.setBorder(roundedBorder);

        return label;
    }
}
