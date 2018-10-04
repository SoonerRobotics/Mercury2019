import socket
import smbus
import time
from shared.ControllerState import ControllerState
from shared.MercuryConfig import MercuryConfig

bus = smbus.SMBus(1)
address = 0x8

MercuryConfig.read()

controller = ControllerState()

server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.connect((MercuryConfig.ip, MercuryConfig.port))

server.sendall(struct.pack("<3s20s", "RPi", MercuryConfig.password))
response = struct.unpack("<B", server.recv(1))
if response == 1:
    print("Connected. PC connected. Starting program.")
elif response == 2:
    print("Connected, waiting for PC")
    response = struct.unpack("<B", server.recv(1))
    if response == 3:
        print("PC connected. Starting program.")
else:
    print("Could not connect to server. Error code: " + response)
    sys.exit(0)

try:
	while True:
		buf = server.recv(ControllerState.size())
		if len(buf) > 0:
			controller.decode(buf)
		print(controller.horizontal)
		print(controller.vertical)
		bus.write_byte(address, controller.vertical)
finally:
	server.close()
