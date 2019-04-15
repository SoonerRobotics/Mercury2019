package ouscr.mercury.networking;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.Instant;
import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;

public class VideoSendThread extends Thread
{
    private final String formatType = "jpg";
    private Webcam webcam;
    private boolean calling;

    private double frameRate = 15;

    private ClientConnection connection;

    public VideoSendThread(Webcam webcam, boolean calling)
    {
        this.webcam = webcam;
        this.calling = calling;
    }

    public void run()
    {
        try
        {
            Frame f;
            BufferedImage bufferedImage;
            while (calling)
            {
                ByteArrayOutputStream fbaos = new ByteArrayOutputStream();
                bufferedImage = webcam.getImage();
                ImageIO.write(bufferedImage, formatType, fbaos);
                f = new Frame(fbaos.toByteArray(), Frame.FrameType.RAWBYTES);
                connection.sendFrame(f);
                bufferedImage.flush();
                Thread.sleep((long) (1000/frameRate));
            }
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public void setConnection(ClientConnection connection) {
        this.connection = connection;
    }
}