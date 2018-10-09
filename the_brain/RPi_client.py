import smbus
import io
import threading
import time
import picamera
from shared.ControllerState import ControllerState
from shared.MercuryConfig import MercuryConfig
from shared.MercuryConnection import MercuryConnection

conf = MercuryConfig()

controller = ControllerState()


def controlserver():
    bus = smbus.SMBus(1)
    address = 0x8

    server = MercuryConnection("RP")
    server.connect(conf.ip, conf.port, conf.password)

    try:
        while True:
            buf = server.get()
            if len(buf) > 0:
                controller.decode(buf)
            if 108 < controller.vertical < 148:
                controller.vertical = 128
            if 108 < controller.horizontal < 148:
                controller.horizontal = 128
            print(time.asctime() + str(controller.vertical) + ", " + str(controller.horizontal))
            bus.write_i2c_block_data(address, 0, [controller.vertical, controller.horizontal])
    finally:
        server.close()


def camserver():

    server = MercuryConnection("RP")
    server.connect(conf.ip, conf.port + 1, conf.password)

    # Make a file-like object out of the connection
    try:
        with picamera.PiCamera() as camera:
            camera.resolution = (320, 240)
            # Start a preview and let the camera warm up for 2 seconds
            # camera.start_preview()
            # time.sleep(2)

            # Note the start time and construct a stream to hold image data
            # temporarily (we could write it directly to connection but in this
            # case we want to find out the size of each capture first to keep
            # our protocol simple)
            stream = io.BytesIO()
            for foo in camera.capture_continuous(stream, 'jpeg'):
                # Rewind the stream and send the image data over the wire
                stream.seek(0)
                server.send(stream.read())

                # Reset the stream for the next capture
                stream.seek(0)
                stream.truncate()
    finally:
        server.close()


threading.Thread(target=controlserver).start()
threading.Thread(target=camserver).start()
