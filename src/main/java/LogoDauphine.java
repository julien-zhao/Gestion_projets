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
            logoImage = ImageIO.read(new File("C:\\Users\\MATEBOOK14\\Desktop\\Gestion_projets\\Dauphine_logo.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (logoImage != null) {
            int x = 0; // 调整X坐标以控制图像的水平位置
            int y = 0; // 调整Y坐标以控制图像的垂直位置
            int width = 80; // 调整宽度以控制图像的大小
            int height = 30; // 调整高度以控制图像的大小
            g.drawImage(logoImage, x, y, width, height, this);
        }
    }
}
