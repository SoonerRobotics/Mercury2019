package ouscr.mercury.networking;

import java.io.EOFException;
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

    class ConnectionManagerThread extends Thread {

        long curId = 0;
        long tick = 0;
        ClientConnection conn;

        ConnectionManagerThread(ClientConnection connection) {
            conn = connection;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                //have we lost connection to the server?
                long curTime = new Date().getTime();

                //have we lost connection? If so, reset literally everything we're fucked
                if (connected && curTime - lastReceivedPacket > TIMEOUT_PERIOID) {
                    System.out.println("lost connection, uh oh");
                    connected = false;

                    while (!connected) {
                        try {
                            connect();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (!isOtherConnected()) {
                        while (true) {
                            try {
                                Frame response = receiveFrame();
                                if (response.type == Frame.FrameType.RESPONSE) {
                                    otherConnected = (boolean) response.deserialize();
                                }
                                break;
                            } catch (SocketTimeoutException e) {
                                //this is fine, just try again
                            } catch (IOException | ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    synchronized (connectionBlock) {
                        connectionBlock.notify();
                    }
                }


                //send heartbeat
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

                try {
                    Thread.sleep(350);
                } catch (InterruptedException e) {
                    System.out.println("can't sleep uwu");
                }
            }
        }
    }

    private final Object connectionBlock = new Object();

    private static final Logger LOGGER = Logger.getLogger( ClientConnection.class.getName() );
    private DatagramSocket socket;
    private InetAddress address;

    private int port;
    private String clientID;
    private String password;

    private boolean connected = false;
    private boolean otherConnected = false;
    private static long lastReceivedPacket = Long.MAX_VALUE;

    private static final long TIMEOUT_PERIOID = 3500; //time in milliseconds of no packets to say disconnected

    public ClientConnection(String clientID, String password, String host, int port) throws IOException {
        //set instance data
        this.port = port;
        this.address = InetAddress.getByName(host);
        this.socket = new DatagramSocket();

        this.clientID = clientID;
        this.password = password;

        while (!connected) {
            try {
                connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!isOtherConnected()) {
            while (true) {
                try {
                    Frame response = receiveFrame();
                    if (response.type == Frame.FrameType.RESPONSE) {
                        otherConnected = (boolean) response.deserialize();
                    }
                    break;
                } catch (SocketTimeoutException e) {
                    //this is fine, just try again
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        ConnectionManagerThread cmt = new ConnectionManagerThread(this);
        cmt.start();
    }

    public boolean isConnected() {
        return connected;
    }

    public void sendFrame(Frame frame) throws IOException{
        if (!connected) {
            blockUntilConnected();
        }

        DatagramPacket packet = frame.getPacket(address, port);
        socket.send(packet);
    }

    public Frame receiveFrame() throws IOException, ClassNotFoundException {
        if (!connected) {
            blockUntilConnected();
        }

        //Wait until we get a non keep-alive packet
        while(true) {
            try {
                byte[] buffer = new byte[1024 * 64];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                Frame frame = new Frame(packet.getData());
                lastReceivedPacket = new Date().getTime();
                if (frame.type != Frame.FrameType.HEARTBEAT) {
                    return frame;
                }
            } catch (SocketTimeoutException e) {
                //this is fine...
            }
        }
    }

    public Frame receiveFrameNonBlocking() throws IOException, ClassNotFoundException {
        if (!connected) {
            return null;
        }

        //Wait until we get a non keep-alive packet
        while(true) {
            try {
                byte[] buffer = new byte[1024 * 64];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                Frame frame = new Frame(packet.getData());
                lastReceivedPacket = new Date().getTime();
                if (frame.type != Frame.FrameType.HEARTBEAT) {
                    return frame;
                }
            } catch (SocketTimeoutException e) {
                //this is fine...
            }
        }
    }

    public void connect() throws IOException {
        socket.setSoTimeout(400); //TODO: Figure out good value for this

        //handshake with server
        Frame.Handshake handshake = new Frame.Handshake();
        handshake.clientID = clientID;
        handshake.password = password;

        Frame handshakeFrame = new Frame(handshake, Frame.FrameType.HANDSHAKE);
        DatagramPacket packet = handshakeFrame.getPacket(address, port);
        socket.send(packet);

        //wait for connection
        while (true) {
            byte[] buffer = new byte[1024];
            packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
            } catch (SocketTimeoutException e) {
                LOGGER.log(Level.SEVERE, "Received no ack from the server.");
                return;
            }

            lastReceivedPacket = new Date().getTime();

            try {
                Frame responseFrame = new Frame(packet.getData());

                if (responseFrame.type == Frame.FrameType.RESPONSE) {
                    connected = true;
                    System.out.println("connected!");
                    otherConnected = (boolean) responseFrame.deserialize();
                    return;
                }

                //else
                //LOGGER.log(Level.SEVERE, "Something other than a response with given.");

            } catch (ClassNotFoundException | EOFException e) {
                // this is fine, most likely just some video data incoming or some trash
            }

        }
    }

    public void blockUntilConnected() {
        synchronized (connectionBlock) {
            while (!connected) {
                try {
                    connectionBlock.wait();
                } catch (InterruptedException e) {
                    System.out.println("Couldn't block thread!");
                }
            }
        }
    }

    public boolean isOtherConnected() {
        if (!connected) {
            throw new NotConnectedException();
        }
        return otherConnected;
    }
}
