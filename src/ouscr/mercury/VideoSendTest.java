package ouscr.mercury;

import com.github.sarxos.webcam.Webcam;
import ouscr.mercury.networking.ClientConnection;
import ouscr.mercury.networking.VideoSendThread;

import java.awt.*;
import java.io.IOException;

public class VideoSendTest {
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {

        Config.readConfig();

        ClientConnection connection = new ClientConnection("PI", Config.password, Config.ip, Config.port);
        connection.waitUntilConnected();
        connection.waitForOther();

        Dimension[] nonStandardResolutions = new Dimension[] {
                new Dimension(200, 100),
        };

        Webcam cam = Webcam.getDefault();
        cam.setCustomViewSizes(nonStandardResolutions);
        cam.setViewSize(nonStandardResolutions[0]);
        cam.open();

        VideoSendThread thread = new VideoSendThread(cam, true);
        thread.setConnection(connection);
        thread.start();

    }
}
