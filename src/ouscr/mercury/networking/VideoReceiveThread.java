package ouscr.mercury.networking;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class VideoReceiveThread extends Thread
{
    private ClientConnection connection;
    private int          videoServerPort;
    private JPanel       panel;
    private boolean      calling;

    public VideoReceiveThread(JPanel panel, boolean calling)
    {
        this.panel = panel;
        this.calling = calling;
    }

    private static BufferedImage createTransformed(
            BufferedImage image, AffineTransform at)
    {
        BufferedImage newImage = new BufferedImage(
                image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = newImage.createGraphics();
        g.transform(at);
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return newImage;
    }


    @Override
    public void run()
    {
        System.out.println("Video Server opened!");
        try
        {
            while (calling)
            {
                Frame f = connection.receiveFrame();
                if (f.type == Frame.FrameType.RAWBYTES) {
                    InputStream inputImage = new ByteArrayInputStream(f.bytes);
                    BufferedImage bufferedImage = ImageIO.read(inputImage);
                    /*
                    AffineTransform at = AffineTransform.getRotateInstance(
                            Math.PI, bufferedImage.getWidth()/2, bufferedImage.getHeight()/2.0);
                    bufferedImage = createTransformed(bufferedImage, at);
                    */
                    panel.getGraphics().drawImage(bufferedImage, 0, 0, 640, 480, null);
                    bufferedImage.flush();
                    inputImage.close();
                } else if (f.type == Frame.FrameType.STRING) {
                    System.out.println(Instant.now().toEpochMilli() - (long) f.deserialize());
                }
            }

        }

        catch (IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public void setConnection(ClientConnection connection) {
        this.connection = connection;
    }
}