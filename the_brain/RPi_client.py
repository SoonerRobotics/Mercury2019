import sys, pygame
import socket

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

aPressed = False

try:
    while True:
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                sys.exit()

        if joystick.get_button(0) and not aPressed:
            sock.sendall("A pressed".encode())
            print("A pressed")
            aPressed = True
        elif not joystick.get_button(0) and aPressed:
            aPressed = False

        pygame.time.wait(16)
finally:
    sock.close()