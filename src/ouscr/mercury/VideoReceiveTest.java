package ouscr.mercury;

import ouscr.mercury.networking.ClientConnection;
import ouscr.mercury.ui.MercuryUI;

import java.io.IOException;

public class VideoReceiveTest {
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {

        Config.readConfig();

        MercuryUI ui = new MercuryUI();

        System.out.println("ip: " + Config.ip + ", pass: " + Config.password);
        ClientConnection connection = new ClientConnection("PC", Config.password, Config.ip, Config.port);

        ui.start(connection);
    }
}
