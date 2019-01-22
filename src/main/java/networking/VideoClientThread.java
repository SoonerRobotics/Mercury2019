package networking;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;

public class VideoClientThread extends Thread
{
    private final String formatType = "jpg";
    private Webcam     webcam;
    private String       ip;
    private int          port;
    private boolean      calling;

    public VideoClientThread(Webcam webcam, String ip, int port, boolean calling)
    {
        this.webcam = webcam;
        this.ip = ip;
        this.port = port;
        this.calling = calling;
    }

    public void run()
    {
        try
        {
            Socket socket = new Socket(ip, port);
            socket.setSoTimeout(5000);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            Frame f;
            BufferedImage bufferedImage;
            while (calling)
            {
                ByteArrayOutputStream fbaos = new ByteArrayOutputStream();
                bufferedImage = webcam.getImage();
                ImageIO.write(bufferedImage, formatType, fbaos);
                f = new Frame(fbaos.toByteArray());
                oos.writeObject(f);
                oos.flush();
                bufferedImage.flush();
                //Thread.sleep((1000/20));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        // catch (InterruptedException e)
        // {
        // e.printStackTrace();
        // }
    }
}