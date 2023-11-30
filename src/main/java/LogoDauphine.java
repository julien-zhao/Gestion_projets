import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


public class LogoDauphine extends JPanel {
    private BufferedImage logoImage;

    public LogoDauphine() {
        try {
            // Charge l'image du logo de Dauphine depuis le fichier spécifié
            logoImage = ImageIO.read(new File("src/main/resources/Picture/Dauphine_logo.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (logoImage != null) {
            int x = 0;
            int y = 0;
            int width = 80;
            int height = 30;
            // Dessine l'image du logo de Dauphine avec les dimensions spécifiées
            g.drawImage(logoImage, x, y, width, height, this);
        }
    }
}
