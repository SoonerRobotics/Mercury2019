package ouscr.mercury;

import com.github.strikerx3.jxinput.XInputAxes;
import com.github.strikerx3.jxinput.XInputDevice;
import com.github.strikerx3.jxinput.exceptions.XInputNotLoadedException;
import com.github.sarxos.webcam.Webcam;
import ouscr.mercury.networking.Frame;
import ouscr.mercury.networking.ClientConnection;
import ouscr.mercury.ui.MercuryUI;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientDriver {

    private static final Logger LOGGER = Logger.getLogger( ClientDriver.class.getName() );

    private static final int TICKRATE = 20; //frequency (Hz) at which controller is polled
    private static final float DEADZONE = 0.15f; //deadzone percentages
    private static final float MIN_SPEED_LEFT = 0.5f; //the speed we reach right after pushing past the deadzone
    private static final float MAX_SPEED_LEFT = 1.0f; //max speed on a 0-1 scale.

    private static final float MIN_SPEED_RIGHT = 0.5f; //the speed we reach right after pushing past the deadzone
    private static final float MAX_SPEED_RIGHT = 1.0f; //max speed on a 0-1 scale.

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, XInputNotLoadedException {


        MercuryUI gui = new MercuryUI();

        gui.start();


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

        int lastLeft = 0;
        int lastRight = 0;

        while (device.poll()) {
            XInputAxes axes = device.getComponents().getAxes();

            instructions.leftMotor = (int)(Math.abs(axes.ly) > DEADZONE ?
                    (axes.ly) / (Math.abs(axes.ly)) * scale(Math.abs(axes.ly), DEADZONE, 1f, MIN_SPEED_LEFT, MAX_SPEED_LEFT) * 255
                    : 0
            );
            instructions.rightMotor = (int)(Math.abs(axes.ry) > DEADZONE ?
                    (axes.ry) / (Math.abs(axes.ry)) * scale(Math.abs(axes.ry), DEADZONE, 1f, MIN_SPEED_RIGHT, MAX_SPEED_RIGHT) * 255
                    : 0
            );

            //only send when there is no new information
            if (instructions.leftMotor != lastLeft || instructions.rightMotor != lastRight) {
                connection.sendFrame(new Frame(instructions, Frame.FrameType.ROBOT));
                lastLeft = instructions.leftMotor;
                lastRight = instructions.rightMotor;
            }

            Thread.sleep(1000 / TICKRATE);
        }

    }

    private static float scale(float x, float inmin, float inmax, float outmin, float outmax) {
        return outmin + (x - inmin) * (outmax - outmin) / (inmax - inmin);
    }
}
