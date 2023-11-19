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
            logoImage = ImageIO.read(new File("src/Picture/Dauphine_logo.png"));
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
            g.drawImage(logoImage, x, y, width, height, this);
        }
    }
}
