package ouscr.mercury;

import com.github.strikerx3.jxinput.XInputAxes;
import com.github.strikerx3.jxinput.XInputDevice;
import com.github.strikerx3.jxinput.exceptions.XInputNotLoadedException;
import ouscr.mercury.networking.ClientConnection;
import ouscr.mercury.networking.Frame;
import ouscr.mercury.serial.Arduino;
import ouscr.mercury.ui.MercuryUI;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientDriver {

    private static final Logger LOGGER = Logger.getLogger( ClientDriver.class.getName() );

    private static final int TICKRATE = 12; //frequency (Hz) at which controller is polled

    private static final float DEADZONE = 0.15f; //deadzone percentages

    private static final float MIN_SPEED_LEFT = 0f; //the speed we reach right after pushing past the deadzone
    private static final float MAX_SPEED_LEFT = 0.7f; //max speed on a 0-1 scale.
    private static final boolean REVERSE_LEFT = false; //reverse the direction of the left motor

    private static final float MIN_SPEED_RIGHT = 0f; //the speed we reach right after pushing past the deadzone
    private static final float MAX_SPEED_RIGHT = 0.7f; //max speed on a 0-1 scale.
    private static final boolean REVERSE_RIGHT = true; //reverse the direction of the right motor

    private static final boolean SWAP_STICKS = true;


    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, XInputNotLoadedException {

        MercuryUI ui = new MercuryUI();

        // Retrieve all devices
        XInputDevice[] devices = XInputDevice.getAllDevices();

        while (devices.length == 0 || !devices[0].isConnected()) {
            LOGGER.log(Level.INFO, "Waiting for controller...");
            Thread.sleep(1000);
            devices = XInputDevice.getAllDevices();
        }

        // Retrieve the device for player 1
        XInputDevice device = XInputDevice.getDeviceFor(0); // or devices[0]

        ClientConnection connection = new ClientConnection("PC", Config.password, Config.ip, Config.port);
        connection.waitUntilConnected();
        connection.waitForOther();

        ui.start(connection);

        while (device.poll()) {
            XInputAxes axes = device.getComponents().getAxes();

            int[] data = new int[2];

            //Left Motor
            data[0] = (int)(Math.abs(axes.ly) > DEADZONE ?
                    (axes.ly) / (Math.abs(axes.ly)) * scale(Math.abs(axes.ly), DEADZONE, 1f, MIN_SPEED_LEFT, MAX_SPEED_LEFT) * 255
                    : 0
            );

            if (REVERSE_LEFT) {
                data[0] *= -1;
            }

            //Right Motor
            data[1] = (int)(Math.abs(axes.ry) > DEADZONE ?
                    (axes.ry) / (Math.abs(axes.ry)) * scale(Math.abs(axes.ry), DEADZONE, 1f, MIN_SPEED_RIGHT, MAX_SPEED_RIGHT) * 255
                    : 0
            );

            if (REVERSE_RIGHT) {
                data[1] *= -1;
            }

            if (SWAP_STICKS) {
                int temp = data[0];
                data[0] = data[1];
                data[1] = temp;
            }

            Arduino.ArduinoEvent event = new Arduino.ArduinoEvent();

            event.motor1 = data[0];
            event.motor2 = data[1];

            event.launcher = axes.rt > 0.8 ? 40 : 115;

            if (device.getComponents().getButtons().left) {
                event.arm = 50;
            } else {
                event.arm = 140;
            }

            event.lights = new int[32];
            for (int i=0; i<32; i++) {
                event.lights[i] = (axes.lt*32) > i ? 2 : 0;
            }

            connection.sendFrame(new Frame(event, Frame.FrameType.ROBOT));

            Thread.sleep(1000 / TICKRATE);
        }

    }

    private static float scale(float x, float inmin, float inmax, float outmin, float outmax) {
        return outmin + (x - inmin) * (outmax - outmin) / (inmax - inmin);
    }
}
