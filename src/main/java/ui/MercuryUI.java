package ui;

import networking.VideoServerThread;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class MercuryUI extends JFrame {
    private VideoServerThread server;

    public MercuryUI() throws IOException {
        JPanel panel = new JPanel();
        server = new VideoServerThread(6372, panel, true);

        add(panel);
        setPreferredSize(new Dimension(1600, 900));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
    }

    public void start() {
        server.start();
    }

    public void join() throws InterruptedException {
        server.join();
    }
}
