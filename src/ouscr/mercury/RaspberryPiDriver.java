package ouscr.mercury;

import com.fazecast.jSerialComm.SerialPort;
import ouscr.mercury.networking.ClientConnection;
import ouscr.mercury.networking.Frame;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RaspberryPiDriver {

    private static final Logger LOGGER = Logger.getLogger( RaspberryPiDriver.class.getName() );


    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {


        boolean running = true;

        SerialPort[] comPorts = SerialPort.getCommPorts();
        SerialPort comPort = getArduinoPort(comPorts);

        while (comPort == null) {
            LOGGER.log(Level.INFO, "Waiting for Arduino...");
            Thread.sleep(1000);
            comPorts = SerialPort.getCommPorts();
            comPort = getArduinoPort(comPorts);
        }

        ClientConnection connection = new ClientConnection("PI", "crabcakes2018", "104.154.244.147", 6372);
        connection.waitUntilConnected();
        connection.waitForOther();

        comPort.openPort();
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);
        while (running) {
            Frame in = connection.receiveFrame();
            if (in.type == Frame.FrameType.ROBOT) {
                LOGGER.log(Level.FINE, "Robot Instructions: " + Arrays.toString(in.bytes));
                comPort.writeBytes(in.bytes, in.bytes.length);
            }
        }
        comPort.closePort();
    }

    private static SerialPort getArduinoPort(SerialPort[] comPorts) {
        if (comPorts.length > 0) {
            for (SerialPort com : comPorts) {
                System.out.println("Found " + com.getDescriptivePortName());
                if (com.getDescriptivePortName().contains("USB")){
                    return com;
                }
            }
        }
        return null;
    }
}
