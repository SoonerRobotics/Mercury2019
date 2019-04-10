package ouscr.mercury;

import ouscr.mercury.networking.ClientConnection;
import ouscr.mercury.networking.Frame;
import ouscr.mercury.serial.Arduino;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RaspberryPiDriver {

    private static final Logger LOGGER = Logger.getLogger( RaspberryPiDriver.class.getName() );

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        LOGGER.setLevel(Level.ALL);

        boolean running = true;

        Arduino arduino = new Arduino();
        arduino.open();

        ClientConnection connection = new ClientConnection("PI", Config.password, Config.ip, Config.port);
        connection.waitUntilConnected();
        connection.waitForOther();

        Arduino.ArduinoEvent lastEvent = null;

        while (running) {
            Frame in = connection.receiveFrame();
            if (in.type == Frame.FrameType.ROBOT) {
                //LOGGER.log(Level.FINE, "Robot Instructions: " + Arrays.toString(in.bytes));

                //Literally just write any ArduinoEvent to the Arduino
                Arduino.ArduinoEvent event = (Arduino.ArduinoEvent) in.deserialize();

                if (lastEvent == null) {
                    lastEvent = event;
                }else if (event.id == lastEvent.id && Arrays.equals(event.data, lastEvent.data)) {
                    break;
                }

                //uncomment until arduino doesnt exist
                arduino.write(event);

                System.out.println("Robot Instructions: " + event.data[0] + ", " + event.data[1]);
                lastEvent = event;
            }
        }

        arduino.close();

    }
}
