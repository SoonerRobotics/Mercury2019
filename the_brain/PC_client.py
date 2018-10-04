import sys, pygame
import socket
import struct
import io
import threading
import time
from PIL import Image
from shared.ControllerState import ControllerState
from shared.MercuryConfig import MercuryConfig

MercuryConfig.read()

pygame.init()

screen = pygame.display.set_mode((600,400))
pygame.display.set_caption("Mercury 2019 GUI")

running = True

controllerFound = False
serverConnected = "Searching for control server..."
server2Connected = "Searching for cam server..."

camimage = ""

def inputHandler():
    global controllerFound
    global serverConnected
    while True:
        pygame.joystick.init()
        if pygame.joystick.get_count() > 0:
            controllerFound = True
            break
        time.sleep(0.1) 
        pygame.joystick.quit()

    joystick = pygame.joystick.Joystick(0)
    joystick.init()

    while True:
        try:
            server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            server.connect((MercuryConfig.ip, MercuryConfig.port))
            break
        except ConnectionRefusedError as err:
            time.sleep(3)

    handshake = "PC" + MercuryConfig.password
    server.sendall(struct.pack("<B29s", len(handshake), bytes(handshake, 'utf-8')))

    response = struct.unpack("<B", server.recv(1))[0]
    if response == 1:
        print("Connected. RPi connected. Starting program.")
        serverConnected = "Connected to control server"
    elif response == 2:
        print("Connected, waiting for RPi")
        serverConnected = "Connected to control server"
        response = struct.unpack("<B", server.recv(1))
        if response == 3:
            print("RPi connected. Starting program.")
    else:
        print("Could not connect to server. Error code: " + str(response))
        serverConnected = "Could not connect to control server. Error code: " + str(response)
        sys.exit()

    controller = ControllerState()

    try:
        while True:
            for event in pygame.event.get():
                if event.type == pygame.QUIT:
                    sys.exit()

            horz = int(joystick.get_axis(1) * 128 + 128)
            vert = int(joystick.get_axis(3) * 128 + 128)

            controller.horizontal = horz
            controller.vertical = vert

            server.sendall(controller.encode())

            pygame.time.wait(100)
    finally:
        server.close()

def cameraHandler():
    global server2Connected
    global camimage
    while True:
        try:
            server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            server.connect((MercuryConfig.ip, MercuryConfig.port + 1))
            break
        except ConnectionRefusedError as err:
            time.sleep(3)

    handshake = "PC" + MercuryConfig.password
    server.sendall(struct.pack("<B29s", len(handshake), bytes(handshake, 'utf-8')))

    response = struct.unpack("<B", server.recv(1))[0]
    if response == 1:
        print("Connected. RPi connected. Starting program.")
        server2Connected = "Connected to web server"
    elif response == 2:
        print("Connected, waiting for RPi")
        server2Connected = "Connected to web server"
        response = struct.unpack("<B", server.recv(1))
        if response == 3:
            print("RPi connected. Starting program.")
    else:
        print("Could not connect to server. Error code: " + str(response))
        server2Connected = "Could not connect to web server. Error code: " + str(response)
        sys.exit()

    connection = server.makefile('rb')
    try:
        while True:
            # Read the length of the image as a 32-bit unsigned int. If the
            # length is zero, quit the loop
            image_len = struct.unpack('<L', connection.read(4))[0]
            if not image_len:
                continue
            # Construct a stream to hold the image data and read the image
            # data from the connection
            image_stream = io.BytesIO()
            image_stream.write(connection.read(image_len))
            print("Found image of size" + str(image_len))
            # Rewind the stream, open it as an image with PIL and do some
            # processing on it
            image_stream.seek(0)
            image = Image.open(image_stream)
            print('Image is %dx%d' % image.size)
            image.verify()
            print('Image is verified')
            break
    finally:
        connection.close()
        server.close()

threading.Thread(target = inputHandler).start()
threading.Thread(target = cameraHandler).start()



##MAIN##

font = pygame.font.SysFont("arial", 24)

# main loop
while running:
    controllerFound

    # event handling, gets all event from the eventqueue
    for event in pygame.event.get():
        # only do something if the event is of type QUIT
        if event.type == pygame.QUIT:
            # change the value to False, to exit the main loop
            running = False

    if controllerFound:
        text = font.render("Controller found", True, (0, 0, 0))
    else:
        text = font.render("No controller found", True, (0, 0, 0))

    text2 = font.render(serverConnected, True, (0, 0, 0))
    text3 = font.render(server2Connected, True, (0, 0, 0))

    if not camimage == "":
        camimgstream = pygame.image.frombuffer(camimage, (640, 480), "RGB")

    screen.fill((255,255,255))
    
    if not camimage == "":
        screen.blit(camimgstream, (0,0))

    screen.blit(text, (10, 10))
    screen.blit(text2, (10, 20 + text2.get_height()))
    screen.blit(text3, (10, 30 + 2 * text2.get_height()))

    pygame.display.flip()