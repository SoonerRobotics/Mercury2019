package ouscr.mercury.networking;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class VideoServerThread extends Thread
{
    private int          videoServerPort;
    private JPanel       panel;
    private boolean      calling;

    public VideoServerThread( int videoServerPort, JPanel panel, boolean calling)
    {
        this.videoServerPort = videoServerPort;
        this.panel = panel;
        this.calling = calling;
    }

    @Override
    public void run()
    {
        System.out.println("Video Server opened!");
        try
        {
            ServerSocket serverSocket = new ServerSocket(videoServerPort);
            Socket socket = serverSocket.accept();
            InputStream in = socket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(in);
            while (calling)
            {
                Frame f = (Frame) ois.readObject();
                InputStream inputImage = new ByteArrayInputStream(f.bytes);
                BufferedImage bufferedImage = ImageIO.read(inputImage);
                panel.getGraphics().drawImage(bufferedImage, 0, 0, 800, 450, null);
                bufferedImage.flush();
                inputImage.close();
            }

        }

        catch (IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }
}