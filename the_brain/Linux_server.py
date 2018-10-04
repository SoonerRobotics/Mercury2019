import socket
import struct
from shared.ControllerState import ControllerState
from shared.MercuryConfig import MercuryConfig

MercuryConfig.read()

controller = ControllerState()

serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
serversocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
serversocket.bind(('0.0.0.0', MercuryConfig.port))
serversocket.listen(1)

connectionPC, connectionRPi = None, None

while connectionPC == None or connectionRPi == None:
	connection, address = serversocket.accept()
	buf = connection.recv(30) #struct.calcsize('<B29s')

	if len(buf) > 0:
		blen, bstr = struct.unpack("<B29s", buf)
		rstr = bstr.decode('utf-8')
		c_id = rstr[:2]
		c_pass = rstr[2:blen]
		if c_id == "PC":
			if connectionPC == None:
				if c_pass == MercuryConfig.password:
					connectionPC = connection
					if connectionRPi == None:
						connection.sendall(struct.pack("<B", 2))
					else:
						connection.sendall(struct.pack("<B", 1))
						connectionRPi.sendall(struct.pack("<B", 3))
				else:
					connection.sendall(struct.pack("<B", 42))
			else:
				connection.sendall(struct.pack("<B", 43))
		elif c_id == "RP":
			if connectionRPi == None:
				if c_pass == MercuryConfig.password:
					connectionRPi = connection
					if connectionPC == None:
						connection.sendall(struct.pack("<B", 2))
					else:
						connection.sendall(struct.pack("<B", 1))
						connectionPC.sendall(struct.pack("<B", 3))
				else:
					connection.sendall(struct.pack("<B", 42))
			else:
				connection.sendall(struct.pack("<B", 43))
		else:
			connection.sendall(struct.pack("<B", 41))
	else:
		connection.sendall(struct.pack("<B", 40))
		

try:
	while True:
		buf = connectionPC.recv(ControllerState.size())
		if len(buf) > 0:
			connectionRPi.sendall(buf)
finally:
	serversocket.close()
