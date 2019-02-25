package ouscr.mercury;

import com.github.sarxos.webcam.Webcam;
import ouscr.mercury.networking.ClientConnection;
import ouscr.mercury.networking.Frame;
import ouscr.mercury.networking.VideoClientThread;
import ouscr.mercury.serial.Arduino;

import java.awt.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RaspberryPiDriver {

    private static final Logger LOGGER = Logger.getLogger( RaspberryPiDriver.class.getName() );

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        LOGGER.setLevel(Level.ALL);

        Dimension[] nonStandardResolutions = new Dimension[] {
                new Dimension(800, 450),
        };

        Webcam cam = Webcam.getDefault();
        cam.setCustomViewSizes(nonStandardResolutions);
        cam.setViewSize(nonStandardResolutions[0]);
        cam.open();

        VideoClientThread thread = new VideoClientThread(cam, "localhost", 6372, true);
        thread.start();

        boolean running = true;

        Arduino arduino = new Arduino();
        arduino.open();

        ClientConnection connection = new ClientConnection("PI", "crabcakes2018", "104.154.244.147", 6372);
        connection.waitUntilConnected();
        connection.waitForOther();

        while (running) {
            Frame in = connection.receiveFrame();
            if (in.type == Frame.FrameType.ROBOT) {
                //LOGGER.log(Level.FINE, "Robot Instructions: " + Arrays.toString(in.bytes));
                Frame.RobotInstruction ri = (Frame.RobotInstruction) in.deserialize();

                System.out.println(ri.leftMotor + ", " + ri.rightMotor);

                int[] data = {ri.leftMotor, ri.rightMotor};

                Arduino.ArduinoEvent event = new Arduino.ArduinoEvent("move", data);
                arduino.write(event);
            }
        }

        arduino.close();

    }
}
