import socket
import smbus
import sys
import threading
import struct
import time
import picamera
from shared.ControllerState import ControllerState
from shared.MercuryConfig import MercuryConfig

MercuryConfig.read()

controller = ControllerState()

def controlserver() :

    bus = smbus.SMBus(1)
    address = 0x8

    try:
        server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server.connect((MercuryConfig.ip, MercuryConfig.port))
    except ConnectionRefusedError as err:
        print(err)
        sys.exit()

    handshake = "RP" + MercuryConfig.password
    server.sendall(struct.pack("<B29s", len(handshake), bytes(handshake, 'utf-8')))

    response = struct.unpack("<B", server.recv(1))[0]
    if response == 1:
        print("Connected control. PC connected. Starting program.")
    elif response == 2:
        print("Connected control, waiting for PC")
        response = struct.unpack("<B", server.recv(1))
        if response == 3:
            print("PC connected control. Starting program.")
    else:
        print("Could not connect to control server. Error code: " + str(response))
        sys.exit()

    try:
        while True:
            buf = server.recv(ControllerState.size())
            if len(buf) > 0:
                controller.decode(buf)
            if controller.vertical > 108 and controller.vertical < 148:
                controller.vertical = 128
            if controller.horizontal > 108 and controller.horizontal < 148:
                controller.horizontal = 128
            print(str(controller.vertical) + ", " + str(controller.vertical))
            bus.write_i2c_block_data(address, 0, [controller.vertical, controller.horizontal] )
    finally:
        server.close()

def webserver():
    try:
        server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server.connect((MercuryConfig.ip, MercuryConfig.port+1))
    except ConnectionRefusedError as err:
        print(err)
        sys.exit()

    handshake = "RP" + MercuryConfig.password
    server.sendall(struct.pack("<B29s", len(handshake), bytes(handshake, 'utf-8')))

    response = struct.unpack("<B", server.recv(1))[0]
    if response == 1:
        print("Connected cam. PC connected. Starting program.")
    elif response == 2:
        print("Connected cam, waiting for PC")
        response = struct.unpack("<B", server.recv(1))
        if response == 3:
            print("PC connected cam. Starting program.")
    else:
        print("Could not connect to cam server. Error code: " + str(response))
        sys.exit()

    # Make a file-like object out of the connection
    connection = server.makefile('wb')
    try:
        with picamera.PiCamera() as camera:
            camera.resolution = (640, 480)
            # Start a preview and let the camera warm up for 2 seconds
            camera.start_preview()
            time.sleep(2)

            # Note the start time and construct a stream to hold image data
            # temporarily (we could write it directly to connection but in this
            # case we want to find out the size of each capture first to keep
            # our protocol simple)
            start = time.time()
            stream = io.BytesIO()
            for foo in camera.capture_continuous(stream, 'jpeg'):
                # Write the length of the capture to the stream and flush to
                # ensure it actually gets sent
                connection.write(struct.pack('<L', stream.tell()))
                connection.flush()
                # Rewind the stream and send the image data over the wire
                stream.seek(0)
                connection.write(stream.read())
                # If we've been capturing for more than 30 seconds, quit
                if time.time() - start > 30:
                    break
                # Reset the stream for the next capture
                stream.seek(0)
                stream.truncate()
        # Write a length of zero to the stream to signal we're done
        connection.write(struct.pack('<L', 0))
    finally:
        connection.close()
        client_socket.close()

threading.Thread(target = controlserver).start()
threading.Thread(target = camserver).start()