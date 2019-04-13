package ouscr.mercury.ui;

import ouscr.mercury.networking.ClientConnection;
import ouscr.mercury.networking.VideoReceiveThread;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class MercuryUI extends JFrame {
    private VideoReceiveThread server;

    public MercuryUI() throws IOException {
        JPanel panel = new JPanel();
        server = new VideoReceiveThread(panel, true);

        add(panel);
        setPreferredSize(new Dimension(1600, 900));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    public void start(ClientConnection connection) {
        server.setConnection(connection);
        server.start();
    }

    public void join() throws InterruptedException {
        server.join();
    }
}
