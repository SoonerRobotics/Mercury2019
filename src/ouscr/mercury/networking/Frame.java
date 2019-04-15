package ouscr.mercury.networking;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;

public class Frame implements Serializable
{
    public enum FrameType {
        HANDSHAKE,
        VIDEO,
        UNKNOWN,
        STRING,
        RESPONSE,
        ROBOT,
        KEEPALIVE,
        RAWBYTES
    }

    public static class DataPacket implements Serializable{
        public byte[] data;
    }

    public static class KeepAlive implements Serializable{
        public long time;
    }

    public static class Handshake implements Serializable {
        public String clientID;
        public String password;
    }

    public FrameType type;
    public byte[] bytes;

    public Frame(String str) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(str);
        bytes = out.toByteArray();
        type = FrameType.STRING;
    }

    public Frame(byte[] bytes, FrameType type) throws IOException {
        this.bytes = bytes;
        this.type = type;
    }

    public Frame(Object obj, FrameType type) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        bytes = out.toByteArray();
        this.type = type;
    }

    //assume these bytes are formatted as something??
    public Frame(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ObjectInputStream is = new ObjectInputStream(in);

        Frame frame = (Frame)is.readObject();
        this.bytes = frame.bytes;
        this.type = frame.type;
    }

    public Object deserialize() throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

    public DatagramPacket getPacket(InetAddress address, int port) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(this);
        byte[] format = out.toByteArray();
        return new DatagramPacket(format, format.length, address, port);
    }
}
