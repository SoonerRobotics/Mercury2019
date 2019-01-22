package networking;

import com.sun.security.ntlm.Client;

import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientConnection {

    public class NotConnectedException extends RuntimeException {
        public NotConnectedException() {
            super("Not connected to server.");
        }
    }

    private static final Logger LOGGER = Logger.getLogger( ClientConnection.class.getName() );
    private DatagramSocket socket;
    private InetAddress address;

    private int port;
    private String clientID;
    private String password;

    private boolean connected = false;
    private boolean otherConnected = false;

    public ClientConnection(String clientID, String password, String host, int port) throws IOException {
        //set instance data
        this.port = port;
        this.address = InetAddress.getByName(host);
        this.socket = new DatagramSocket();

        this.clientID = clientID;
        this.password = password;
    }

    public boolean isConnected() {
        return connected;
    }

    public void sendFrame(Frame frame) throws IOException{
        if (!connected) {
            throw new NotConnectedException();
        }
        DatagramPacket packet = frame.getPacket(address, port);
        socket.send(packet);
    }

    public Frame receiveFrame() throws IOException, ClassNotFoundException {
        if (!connected) {
            throw new NotConnectedException();
        }
        byte[] buffer = new byte[2048];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return new Frame(packet.getData());
    }

    public void waitForOther() throws IOException, ClassNotFoundException {
        if(!connected) {
            throw new NotConnectedException();
        }

        if(isOtherConnected()) {
            return;
        }

        Frame response = receiveFrame();
        if (response.type == Frame.FrameType.RESPONSE) {
            connected = true;
            otherConnected = (boolean) response.deserialize();
        }
    }

    public void connect() throws IOException {
        socket.setSoTimeout(2000); //TODO: Figure out good value for this

        //handshake with server
        Frame.Handshake handshake = new Frame.Handshake();
        handshake.clientID = clientID;
        handshake.password = password;

        Frame handshakeFrame = new Frame(handshake, Frame.FrameType.HANDSHAKE);
        DatagramPacket packet = handshakeFrame.getPacket(address, port);
        socket.send(packet);

        byte[] buffer = new byte[1024];
        packet = new DatagramPacket(buffer, buffer.length);
        try {
            socket.receive(packet);
        } catch (SocketTimeoutException e) {
            LOGGER.log(Level.SEVERE, "Received no ack from the server.");
            return;
        }

        try {
            Frame responseFrame = new Frame(packet.getData());

            if (responseFrame.type == Frame.FrameType.RESPONSE) {
                connected = true;
                otherConnected = (boolean) responseFrame.deserialize();
            } else {
                LOGGER.log(Level.SEVERE, "Something other than a response with given.");
            }
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Message received was not formatted correctly.");
        }

        socket.setSoTimeout(0);
    }

    public void waitUntilConnected() throws IOException, InterruptedException {
        while (!connected) {
            connect();
        }
    }

    public boolean isOtherConnected() {
        if (!connected) {
            throw new NotConnectedException();
        }
        return otherConnected;
    }
}
