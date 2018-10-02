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
sock.connect(("104.154.244.147", 8080))

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
            controller.axes.append(joystick.get_axis(i))

        #print(controller.axes)
        #print(controller.encode())

        sock.sendall(controller.encode())

        pygame.time.wait(200)
finally:
    sock.close()