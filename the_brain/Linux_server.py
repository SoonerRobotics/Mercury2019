import socket
from shared.ControllerState import ControllerState
from shared.MercuryConfig import MercuryConfig

MercuryConfig.read()

controller = ControllerState()

serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
serversocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
serversocket.bind(('0.0.0.0', 8080))
serversocket.listen(1)

connectionPC, connectionRPi = None, None

while connectionPC == None or connectionRPi == None:
	connection, address = serversocket.accept()
	buf = connection.recv(128) #How big does this need to be?

	if len(buf) > 0:
		string = str(buf.decode())
		if string.startswith("PC") and string.endswith(MercuryConfig.password):
			connectionPC = connection
		elif string.startswith("RPi") and string.endswith(MercuryConfig.password):
			connectionRPi = connection
		else:
			connection.sendall("Wrong credentials.")
			connection.close()

try:
	while True:
		buf = connectionPC.recv(128) #How big does this need to be?
		if len(buf) > 0:
			connectionRPi.sendall(("CC" + buf.decode()).encode())
finally:
	serversocket.close()
