package ouscr.mercury;

import ouscr.mercury.networking.ClientConnection;
import ouscr.mercury.networking.Frame;
import ouscr.mercury.serial.Arduino;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RaspberryPiDriver {

    private static final Logger LOGGER = Logger.getLogger( RaspberryPiDriver.class.getName() );

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        LOGGER.setLevel(Level.ALL);

        boolean running = true;

        Arduino arduino = new Arduino();
        arduino.open();

        ClientConnection connection = new ClientConnection("PI", "crabcakes2018", "3.19.27.173", 6372);
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
