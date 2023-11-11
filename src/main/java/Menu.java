import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
public class Menu {
    JFrame frame = new JFrame("Gestion de Projets des étudiants");
    JPanel Panel1 = new JPanel();
    JPanel Panel2 = new JPanel(new GridBagLayout());

    Menu() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280, 1000);

        // 设置Panel1和Panel2的背景颜色以便观察
        Panel2.setBackground(new Color(239, 248, 250));
        Panel1.setBackground(Color.WHITE);

        // 使用绝对布局
        frame.setLayout(null);

        // 设置Panel1和Panel2的位置和大小
        Panel1.setBounds(380, 250, 200, 250);
        Panel2.setBounds(580, 250, 300, 250);


        // 原始图像
        ImageIcon originalIcon = new ImageIcon("J:\\Julien ZHAO\\dauphine.jpeg");

// 获取原始图像
        Image originalImage = originalIcon.getImage();

// 缩放图像
        Image scaledImage = originalImage.getScaledInstance(130, 50, Image.SCALE_SMOOTH);

// 创建新的 ImageIcon，使用缩放后的图像
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

// 创建 JLabel 并将缩放后的图标添加到 JLabel
        JLabel imageLabel = new JLabel(scaledIcon);


        // 设置imageLabel在Panel1中的位置和尺寸
        imageLabel.setBounds(50, 10, 100, 100);

        // 在 Panel1 中添加一个文本 label
        JLabel textLabel = new JLabel("Université Paris");
        textLabel.setForeground(new Color(157, 212, 227));  // 设置字体颜色为蓝色
        textLabel.setFont(new Font("Times New Roman", Font.PLAIN, 14));

        // 设置 Gestion_etudiant 的位置
        GridBagConstraints gbcText = new GridBagConstraints();
        gbcText.gridx = 0;
        gbcText.gridy = 1;  // 将文本 label 放在图片 label 的正下方
        gbcText.anchor = GridBagConstraints.PAGE_START;  // 设置 anchor 为 PAGE_START，即上边缘对齐
        gbcText.insets = new Insets(30, 0, 0, 0);  // 设置垂直间距

        // 设置Panel1的布局为null
        Panel1.setLayout(new GridBagLayout());
        Panel1.add(imageLabel);
        Panel1.add(textLabel, gbcText);

        // 将Panel1和Panel2添加到JFrame
        frame.add(Panel1);
        frame.add(Panel2);

        frame.getContentPane().setBackground(new Color(125, 197, 217));

        ImageIcon customIcon = new ImageIcon("J:\\Julien ZHAO\\dauphine.jpeg");  // 替换为你的图标文件路径
        frame.setIconImage(customIcon.getImage());


        JLabel gestionEtudiantLabel = createHoverLabel("Gestion des etudiants");
        JLabel gestionProjetLabel = createHoverLabel("Gestion des projets");
        gestionEtudiantLabel.setForeground(new Color(108, 190, 213));  // 设置字体颜色为蓝色
        gestionProjetLabel.setForeground(new Color(108, 190, 213));  // 设置字体颜色为蓝色
        gestionEtudiantLabel.setFont(new Font("Times New Roman", Font.PLAIN, 16));
        gestionProjetLabel.setFont(new Font("Times New Roman", Font.PLAIN, 16));

        // 设置 Gestion_etudiant 和 Gestion_projet 垂直对齐
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.insets = new Insets(10, 0, 0, 0);  // 设置垂直间距
        gbc1.anchor = GridBagConstraints.LINE_START;
        Panel2.add(gestionEtudiantLabel, gbc1);

        // 设置垂直间距
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 0;
        gbc2.gridy = 1;
        gbc2.insets = new Insets(40, 0, 0, 0);  // 设置垂直间距
        gbc2.anchor = GridBagConstraints.LINE_START;
        Panel2.add(gestionProjetLabel, gbc2);

        frame.setVisible(true);
    }

    private JLabel createHoverLabel(String text) {
        JLabel label = new JLabel(text);
        label.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (text.equals("Gestion des etudiants")) {
                    Gestion_etudiant gestion_etudiant = new Gestion_etudiant();
                    System.out.println("Gestion des etudiants clicked!");
                } else if (text.equals("Gestion des projets")) {
                    Gestion_projet gestion_projet = new Gestion_projet();
                    System.out.println("Gestion des projets clicked!");
                }
                frame.setVisible(false); // 隐藏菜单窗口
            }

            public void mouseEntered(java.awt.event.MouseEvent evt) {
                label.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(125, 197, 217), 2));
                label.setOpaque(true);
                label.setBackground(Color.LIGHT_GRAY);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                label.setBorder(null);
                label.setOpaque(false);
                label.setBackground(null);
            }
        });
        return label;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Menu::new);
    }
}
