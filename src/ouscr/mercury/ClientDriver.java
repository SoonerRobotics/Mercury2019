package ouscr.mercury;

import com.github.strikerx3.jxinput.XInputAxes;
import com.github.strikerx3.jxinput.XInputDevice;
import com.github.strikerx3.jxinput.exceptions.XInputNotLoadedException;
import com.github.sarxos.webcam.Webcam;
import ouscr.mercury.networking.Frame;
import ouscr.mercury.networking.ClientConnection;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientDriver {

    private static final Logger LOGGER = Logger.getLogger( ClientDriver.class.getName() );

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, XInputNotLoadedException {

        /*
        Dimension[] nonStandardResolutions = new Dimension[] {
                new Dimension(800, 450),
        };

        MercuryUI gui = new MercuryUI();

        Webcam cam = Webcam.getDefault();
        cam.setCustomViewSizes(nonStandardResolutions);
        cam.setViewSize(nonStandardResolutions[0]);
        cam.open();

        VideoClientThread thread = new VideoClientThread(cam, "localhost", 6372, true);
        thread.start();
        gui.start();
        */

        // Retrieve all devices
        XInputDevice[] devices = XInputDevice.getAllDevices();

        while (devices.length == 0 || !devices[0].isConnected()) {
            LOGGER.log(Level.INFO, "Waiting for controller...");
            Thread.sleep(1000);
            devices = XInputDevice.getAllDevices();
        }

        // Retrieve the device for player 1
        XInputDevice device = XInputDevice.getDeviceFor(0); // or devices[0]

        //104.154.244.147

        ClientConnection connection = new ClientConnection("PC", "crabcakes2018", "104.154.244.147", 6372);
        connection.waitUntilConnected();
        connection.waitForOther();

        Frame.RobotInstruction instructions = new Frame.RobotInstruction();

        while (device.poll()) {
            XInputAxes axes = device.getComponents().getAxes();
            instructions.leftMotor = axes.ly;
            instructions.rightMotor = axes.ry;
            connection.sendFrame(new Frame(instructions, Frame.FrameType.ROBOT));
            Thread.sleep(10); //TODO: is this needed?
        }

    }
}
