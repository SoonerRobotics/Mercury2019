package ouscr.mercury;

import ouscr.mercury.networking.Frame;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerDriver {

    private static final Logger LOGGER = Logger.getLogger( ServerDriver.class.getName() );

    private static int port;
    private static String password;

    private static InetAddress PC;
    private static int PC_port = -1;
    private static InetAddress PI;
    private static int PI_port = -1;

    private static DatagramSocket socket;
    private static boolean running = true;

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        port = Config.port;
        password = Config.password;

        socket = new DatagramSocket(port);

        while (running) {
            try {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                Frame handshakeFrame = new Frame(packet.getData());

                if (handshakeFrame.type == Frame.FrameType.HANDSHAKE) {
                    Frame.Handshake handshake = (Frame.Handshake) handshakeFrame.deserialize();
                    if (handshake.password.equals(ServerDriver.password)) {
                        if (handshake.clientID.equals("PI")) {
                            LOGGER.log(Level.INFO, "PI Connection!");
                            PI = packet.getAddress();
                            PI_port = packet.getPort();

                            sendResponse(PI, PI_port, PC, PC_port);
                        } else if (handshake.clientID.equals("PC")) {
                            LOGGER.log(Level.INFO, "PC Connection!");
                            PC = packet.getAddress();
                            PC_port = packet.getPort();

                            sendResponse(PC, PC_port, PI, PI_port);
                        } else {
                            LOGGER.log(Level.WARNING, "Correct password received but invalid clientID: " + handshake.clientID);
                        }
                    }
                } else {
                    if (PC_port != -1 && PI_port != -1) {
                        if (packet.getAddress().equals(PC) && packet.getPort() == PC_port) {
                            packet.setAddress(PI);
                            packet.setPort(PI_port);
                            socket.send(packet);
                        } else if (packet.getAddress().equals(PI) && packet.getPort() == PI_port) {
                            packet.setAddress(PC);
                            packet.setPort(PC_port);
                            socket.send(packet);
                        } else {
                            LOGGER.log(Level.INFO, "Unknown sender.");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.WARNING, "Received incorrectly formatted Handshake.");
            }
        }
    }

    private static void sendResponse(InetAddress in, int in_port, InetAddress other, int other_port) throws IOException {
        socket.send(new Frame(other_port != -1, Frame.FrameType.RESPONSE)
                .getPacket(in, in_port));

        if (other_port != -1) {
            socket.send(new Frame(true, Frame.FrameType.RESPONSE)
                    .getPacket(other, other_port));
        }
    }
}