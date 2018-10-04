import socket
import struct
import io
import time
import threading
from shared.ControllerState import ControllerState
from shared.MercuryConfig import MercuryConfig

MercuryConfig.read()

def controlserver():
	serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	serversocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
	serversocket.bind(('0.0.0.0', MercuryConfig.port))
	serversocket.listen(3)

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

def camserver():
	serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	serversocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
	serversocket.bind(('0.0.0.0', MercuryConfig.port+1))
	serversocket.listen(3)

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
			

	connection = connectionRPi.makefile('rb')
	connectionw = connectionPC.makefile('wb')
	try:
		while True:
			# Read the length of the image as a 32-bit unsigned int. If the
			# length is zero, quit the loop
			image_len = struct.unpack('<L', connectionRPi.recv(4))[0]
			if not image_len:
				break
			# Construct a stream to hold the image data and read the image
			# data from the connection
			stream = io.BytesIO()
			connectionw.write(struct.pack('<L', stream.tell()))
			connectionw.flush()
			# Rewind the stream and send the image data over the wire
			stream.seek(0)
			connectionw.write(stream.read())
			# Reset the stream for the next capture
			stream.seek(0)
			stream.truncate()
	finally:
		serversocket.close()


threading.Thread(target = controlserver).start()
threading.Thread(target = camserver).start()