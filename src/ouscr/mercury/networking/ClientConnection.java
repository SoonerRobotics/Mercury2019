package ouscr.mercury.networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientConnection {

    public class NotConnectedException extends RuntimeException {
        NotConnectedException() {
            super("Not connected to server.");
        }
    }

    class KeepAliveScheduler extends TimerTask {

        long curId = 0;
        long tick = 0;
        ClientConnection conn;

        KeepAliveScheduler(ClientConnection connection) {
            conn = connection;
        }

        @Override
        public void run() {

            //have we lost connection to the server?
            long curTime = new Date().getTime();
            lostConnection = curTime - lastReceivedPacket > TIMEOUT_PERIOID;

            tick = (tick + 1)%5;

            //every five keepalives, send a packet to test aliveness
            if (tick == 0) {
                Frame.Heartbeat packet = new Frame.Heartbeat();
                packet.sendTime = new Date().getTime();
                packet.id = curId;
                curId++;

                Frame frame = null;
                try {
                    frame = new Frame(packet, Frame.FrameType.HEARTBEAT);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Could not create heartbeat packet!");
                }
                try {
                    conn.sendFrame(frame);
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Could not send heartbeat packet!");
                }
            }
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
    private boolean lostConnection = false;
    private static long lastReceivedPacket = Long.MAX_VALUE;

    private static final long TIMEOUT_PERIOID = 2000; //time in milliseconds of no packets to say disconnected

    private KeepAliveScheduler keepAliveScheduler;

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

    public boolean lostConnection() {
        return lostConnection;
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

        if (lostConnection) {
            keepAliveScheduler.cancel();
            connected = false;
            try {
                waitUntilConnected();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //Wait until we get a non keep-alive packet
        while(true) {
            byte[] buffer = new byte[1024*64];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            Frame frame = new Frame(packet.getData());
            lastReceivedPacket = new Date().getTime();
            if (frame.type != Frame.FrameType.HEARTBEAT) {
                return frame;
            }
        }
    }

    public void waitForOther() throws IOException, ClassNotFoundException {
        if(!connected) {
            throw new NotConnectedException();
        }

        if(isOtherConnected()) {
            return;
        }

        while (true) {
            try {
                Frame response = receiveFrame();
                if (response.type == Frame.FrameType.RESPONSE) {
                    connected = true;
                    otherConnected = (boolean) response.deserialize();
                }
                break;
            } catch (SocketTimeoutException e) {
                //this is fine, just try again
            }
        }
    }

    public void connect() throws IOException {
        socket.setSoTimeout(200); //TODO: Figure out good value for this

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

        lostConnection = false;
    }

    public void waitUntilConnected() throws IOException, InterruptedException {
        while (!connected) {
            connect();
        }

        //After we connect, keep alive
        keepAliveScheduler = new KeepAliveScheduler(this);
        Timer timer = new Timer();
        timer.schedule(keepAliveScheduler, 1000, 100); //Keep alive every 100 milliseconds
    }

    public boolean isOtherConnected() {
        if (!connected) {
            throw new NotConnectedException();
        }
        return otherConnected;
    }
}
