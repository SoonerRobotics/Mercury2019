import socket
import smbus
import time
from shared.ControllerState import ControllerState
from shared.MercuryConfig import MercuryConfig

bus = smbus.SMBus(1)
address = 0x8

MercuryConfig.read()

controller = ControllerState()

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.connect((MercuryConfig.ip, MercuryConfig.port))

sock.sendall(("RPi" + MercuryConfig.password).encode())

try:
	while True:
		buf = sock.recv(128) #How big does this need to be?
		if len(buf) > 0:
			controller.decode(buf.decode())
		print(controller.axes)
		print(controller.buttons)
		bus.write_byte(address, int(controller.axes[0]))
finally:
	sock.close()
