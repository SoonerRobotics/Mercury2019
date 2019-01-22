import smbus
import time
import pygame

bus = smbus.SMBus(1)
address = 0x8

pygame.init()
screen = pygame.display.set_mode((640, 480))

while True:
    pygame.joystick.init()
    if pygame.joystick.get_count() > 0:
        controllerFound = True
        break
    time.sleep(0.5)
    pygame.joystick.quit()

print("Controller found.")
joystick = pygame.joystick.Joystick(0)
joystick.init()

running = True

while running:

    # event handling, gets all event from the eventqueue
    for event in pygame.event.get():
        # only do something if the event is of type QUIT
        if event.type == pygame.QUIT:
            # change the value to False, to exit the main loop
            running = False

    horz = int(joystick.get_axis(1) * 128 + 128)
    vert = int(joystick.get_axis(3) * 128 + 128)

    if 108 < vert < 148:
        vert = 128
    if 108 < horz < 148:
        horz = 128
    print(time.asctime() + str(vert) + ", " + str(horz))
    bus.write_i2c_block_data(address, 0, [vert, horz])

    time.sleep(0.03)