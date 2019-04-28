package ouscr.mercury;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.ds.v4l4j.V4l4jDriver;
import com.github.strikerx3.jxinput.XInputDevice;
import com.github.strikerx3.jxinput.exceptions.XInputNotLoadedException;
import ouscr.mercury.networking.ClientConnection;
import ouscr.mercury.networking.Frame;
import ouscr.mercury.networking.VideoSendThread;
import ouscr.mercury.serial.Arduino;

import java.awt.*;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RaspberryPiDriver {

    private static final Logger LOGGER = Logger.getLogger( RaspberryPiDriver.class.getName() );

    static {
        Webcam.setDriver(new V4l4jDriver());
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, XInputNotLoadedException {
        LOGGER.setLevel(Level.ALL);

        boolean running = true;

        XInputDevice[] devices = XInputDevice.getAllDevices();

        if (!(devices.length == 0 || !devices[0].isConnected())) {
            RaspberryPiByControllerDriver.main(args);
            return;
        }

        Arduino arduino = new Arduino();
        arduino.open();

        Arduino.ArduinoEvent ohaio = new Arduino.ArduinoEvent();
        ohaio.status = 2;

        arduino.write(ohaio);

        ClientConnection connection = new ClientConnection("PI", Config.password, Config.ip, Config.port);
        connection.blockUntilConnected();

        Dimension[] nonStandardResolutions = new Dimension[] {
                new Dimension(192, 108),
        };

        Webcam cam = Webcam.getDefault();
        cam.setCustomViewSizes(nonStandardResolutions);
        cam.setViewSize(nonStandardResolutions[0]);
        cam.open();

        VideoSendThread thread = new VideoSendThread(cam, true);
        thread.setConnection(connection);
        thread.start();

        boolean hasReceivedPacket = false;

        while (running) {
            Frame in = connection.receiveFrameNonBlocking();
            if (hasReceivedPacket && in == null) {
                //we have lost connection, ruh roh
                Arduino.ArduinoEvent event = new Arduino.ArduinoEvent();
                event.status = 1;

                arduino.write(event);

                Thread.sleep(200);
            } else if (in.type == Frame.FrameType.ROBOT) {
                hasReceivedPacket = true;
                //LOGGER.log(Level.FINE, "Robot Instructions: " + Arrays.toString(in.bytes));

                //Literally just write any ArduinoEvent to the Arduino
                Arduino.ArduinoEvent event = (Arduino.ArduinoEvent) in.deserialize();

                //uncomment until arduino doesnt exist
                arduino.write(event);

                System.out.println(event.getJson());
            } else {
                Arduino.ArduinoEvent event = new Arduino.ArduinoEvent();
                event.status = 2;

                arduino.write(event);

                Thread.sleep(200);
            }
        }

        arduino.close();

    }
}
