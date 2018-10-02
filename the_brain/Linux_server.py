import socket
from shared.ControllerState import ControllerState
from shared.MercuryConfig import MercuryConfig

MercuryConfig.read()

controller = ControllerState()

serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
serversocket.bind(('0.0.0.0', 8080))
serversocket.listen(3)

connection, address = serversocket.accept()

try:
	while True:
		buf = connection.recv(128) #How big does this need to be?
		if len(buf) > 0:
			controller.decode(buf.decode())
		print(controller.axes)
		print(controller.buttons)
finally:
	serversocket.close()
