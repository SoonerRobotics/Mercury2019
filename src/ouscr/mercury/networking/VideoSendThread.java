package ouscr.mercury.networking;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;

public class VideoSendThread extends Thread
{
    private final String formatType = "jpg";
    private Webcam webcam;
    private boolean calling;

    private double frameRate = 10;

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
                Frame.DataPacket dp = new Frame.DataPacket();
                dp.data = fbaos.toByteArray();
                f = new Frame(dp, Frame.FrameType.STRING);
                Frame.DataPacket out = (Frame.DataPacket)f.deserialize();
                System.out.println(out.data.length);
                connection.sendFrame(f);
                bufferedImage.flush();
                Thread.sleep((long) (1000/frameRate));
            }
        }
        catch (IOException | InterruptedException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public void setConnection(ClientConnection connection) {
        this.connection = connection;
    }
}