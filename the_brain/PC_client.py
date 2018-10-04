import sys, pygame
import socket
import struct
from shared.ControllerState import ControllerState
from shared.MercuryConfig import MercuryConfig

print(pygame)

MercuryConfig.read()

pygame.init()
pygame.joystick.init()

pygame.display.set_mode((1,1))

#if pygame.joystick.get_count() == 0:
#    print("No joysticks found")
#    exit()

#joystick = pygame.joystick.Joystick(0)
#joystick.init()

try:
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.connect((MercuryConfig.ip, MercuryConfig.port))
except ConnectionRefusedError as err:
    print(err)
    sys.exit(0)

server.sendall(struct.pack("<3s20s", "PC", MercuryConfig.password))
response = struct.unpack("<B", server.recv(1))
if response == 1:
    print("Connected. RPi connected. Starting program.")
elif response == 2:
    print("Connected, waiting for RPi")
    response = struct.unpack("<B", server.recv(1))
    if response == 3:
        print("RPi connected. Starting program.")
else:
    print("Could not connect to server. Error code: " + response)
    sys.exit(0)

controller = ControllerState()

try:
    while True:
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                sys.exit()

        horz = int(joystick.get_axis(0) * 128 + 128)
        vert = int(joystick.get_axis(1) * 128 + 128)

        controller.horizontal = horz
        controller.vertical = vert

        server.sendall(controller.encode())

        pygame.time.wait(100)
finally:
    server.close()