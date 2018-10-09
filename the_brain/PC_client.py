import sys, pygame
import io
import threading
import time
from PIL import Image
from shared.MercuryConfig import MercuryConfig
from shared.MercuryConnection import MercuryConnection
from shared.ControllerState import ControllerState

conf = MercuryConfig()

pygame.init()
screen = pygame.display.set_mode((640, 480))
pygame.display.set_caption("Mercury 2019 GUI")

running = True

controllerFound = False
controlStatus = "Searching for control server..."
camStatus = "Searching for cam server..."

camimgstream = None

t1Stop = False
t2Stop = False


def inputHandler():
    global controllerFound
    global controlStatus
    while True:
        if t1Stop:
            return
        pygame.joystick.init()
        if pygame.joystick.get_count() > 0:
            controllerFound = True
            break
        time.sleep(0.5)
        pygame.joystick.quit()

    joystick = pygame.joystick.Joystick(0)
    joystick.init()

    server = MercuryConnection("PC")
    server.connect(conf.ip, conf.port, conf.password)

    controlStatus = "Connected to control server"

    controller = ControllerState()

    try:
        while True:
            if t1Stop:
                break

            horz = int(joystick.get_axis(1) * 128 + 128)
            vert = int(joystick.get_axis(3) * 128 + 128)

            controller.horizontal = horz
            controller.vertical = vert

            server.send(controller.encode())

            pygame.time.wait(100)
    finally:
        server.close()


def cameraHandler():
    global camStatus
    global camimgstream
    global t2Stop

    server = MercuryConnection("PC")
    server.connect(conf.ip, conf.port + 1, conf.password)

    camStatus = "Connected to cam server"

    connection = server.connection.makefile('rb')
    try:
        while True:
            if t2Stop:
                break
            # Construct a stream to hold the image data and read the image
            # data from the connection
            image_stream = io.StringIO(server.get())
            # Rewind the stream, open it as an image with PIL and do some
            # processing on it
            image_stream.seek(0)
            image = Image.open(image_stream)
            camimgstream = pygame.image.frombuffer(image.tobytes("raw"), (640, 480), "RGB")

    finally:
        connection.close()
        server.close()


t1 = threading.Thread(target=inputHandler)
t1.start()
t2 = threading.Thread(target=cameraHandler)
t2.start()

##MAIN##

font = pygame.font.SysFont("arial", 24)

# main loop
while running:

    # event handling, gets all event from the eventqueue
    for event in pygame.event.get():
        # only do something if the event is of type QUIT
        if event.type == pygame.QUIT:
            # change the value to False, to exit the main loop
            t1Stop = t2Stop = True
            running = False

    pressed = pygame.key.get_pressed()
    if pressed[pygame.K_ESCAPE]:
        t1Stop = t2Stop = True
        running = False

    if controllerFound:
        controllerStatus = font.render("Controller found", True, (0, 0, 0))
    else:
        controllerStatus = font.render("No controller found", True, (0, 0, 0))

    controlText = font.render(controlStatus, True, (0, 0, 0))
    camText = font.render(camStatus, True, (0, 0, 0))

    screen.fill((255, 255, 255))

    if not camimgstream is None:
        screen.blit(camimgstream, (0, 0))

    screen.blit(controllerStatus, (10, 10))
    screen.blit(controlText, (10, 20 + controlText.get_height()))
    screen.blit(camText, (10, 30 + 2 * camText.get_height()))

    pygame.display.flip()

t1.join()
t2.join()