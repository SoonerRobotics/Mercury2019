package ouscr.mercury;

import com.github.strikerx3.jxinput.exceptions.XInputNotLoadedException;

import java.io.IOException;

public class Driver {
    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException, XInputNotLoadedException {
        if (args.length == 0) {
            System.exit(0);
        }

        if (args[0].equalsIgnoreCase("server")) {
            ServerDriver.main(null);
        }

        if (args[0].equalsIgnoreCase("pi")) {
            RaspberryPiDriver.main(null);
        }

        if (args[0].equalsIgnoreCase("client")) {
            ClientDriver.main(null);
        }
    }
}
