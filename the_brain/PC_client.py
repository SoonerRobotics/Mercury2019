import sys, pygame
import socket
from shared.ControllerState import ControllerState
from shared.MercuryConfig import MercuryConfig

print(pygame)

MercuryConfig.read()

pygame.init()
pygame.joystick.init()

pygame.display.set_mode((1,1))

if pygame.joystick.get_count() == 0:
    print("No joysticks found")
    exit()

joystick = pygame.joystick.Joystick(0)
joystick.init()

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.connect((MercuryConfig.ip, MercuryConfig.port))

sock.sendall(("PC" + MercuryConfig.password).encode())

controller = ControllerState()

try:
    while True:
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                sys.exit()

        controller.buttons.clear()
        for i in range(joystick.get_numbuttons()):
            controller.buttons.append(joystick.get_button(i))

        controller.axes.clear()
        for i in range(joystick.get_numaxes()):
            axisval = joystick.get_axis(i)
            axisval = int(axisval * 256)
            if (axisval < 20)
                axisval = 0
            controller.axes.append(axisval)

        #print(controller.axes)
        #print(controller.encode())

        sock.sendall(controller.encode())

        pygame.time.wait(100)
finally:
    sock.close()