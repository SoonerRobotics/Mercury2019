import socket
import struct
import sys
import time

FIXED_SEND_LENGTH = 1024
HEADER_TYPE = '>I'
HEADER_TYPE_SIZE = struct.calcsize(HEADER_TYPE)


def _send_chunk(c, msg):
    while msg:
        msg = msg[c.send(msg):]


def _get_chunk(c, length: int):
    if length <= 0:
        return ''
    buf = ''
    while len(buf) < length:
        buf2 = str(c.recv(length - len(buf)))
        if not buf2:
            if buf:
                raise RuntimeError("Connection to server lost.")
            else:
                return ''
        buf += buf2
    return buf


class MercuryConnection:

    def __init__(self, id: str):
        self.id = id
        self.connection = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    def connect(self, ip, port, password):
        tries = 1
        while (True):
            try:
                self.connection.connect((ip, port))
                break
            except ConnectionError as e:
                tries += 1
                if tries <= 3:
                    print("Could not reach server, trying again. Attempt " + str(tries))
                    time.sleep(1)
                else:
                    print("Could not reach server after 3 tries, quitting.")
                    sys.exit()

        handshake = self.id + password
        self.connection.sendall(struct.pack("<B29s", len(handshake), bytes(handshake, 'utf-8')))

        response = struct.unpack("<B", self.connection.recv(1))[0]
        if response == 1:
            print("Both devices connected. Starting program.")
        elif response == 2:
            print("Connected, waiting for other")
            response = struct.unpack("<B", self.connection.recv(1))
            if response == 3:
                print("Both devices connected. Starting program.")
        else:
            raise RuntimeError("Could not connect to server. Error code: " + str(response))

    def send(self, msg):
        header = struct.pack(HEADER_TYPE, len(msg))
        _send_chunk(self.connection, header)
        _send_chunk(self.connection, msg)

    def get(self):
        header = struct.unpack(HEADER_TYPE, _get_chunk(self.connection, HEADER_TYPE_SIZE))[0]
        return _get_chunk(self.connection, header)

    def close(self):
        self.connection.close()


class MercuryServer:

    def __init__(self):
        self.server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.connectionPC = None
        self.connectionRPi = None

    def send(self, msg, id):
        if id == "PC":
            header = struct.pack(HEADER_TYPE, len(msg))
            _send_chunk(self.connectionPC, header)
            _send_chunk(self.connectionPC, msg)
        elif id == "RP":
            header = struct.pack(HEADER_TYPE, len(msg))
            _send_chunk(self.connectionRPi, header)
            _send_chunk(self.connectionRPi, msg)
        else:
            raise RuntimeError("Attempt to get data from unknown device")

    def get(self, id):
        if id == "PC":
            header = struct.unpack(HEADER_TYPE, _get_chunk(self.connectionPC, HEADER_TYPE_SIZE))[0]
            return _get_chunk(self.connectionPC, header)
        elif id == "RP":
            header = struct.unpack(HEADER_TYPE, _get_chunk(self.connectionRPi, HEADER_TYPE_SIZE))[0]
            return _get_chunk(self.connectionRPi, header)
        else:
            raise RuntimeError("Attempt to get data from unknown device")

    def connect(self, port, password):
        self.server.bind(('0.0.0.0', port))
        self.server.listen(3)

        while self.connectionPC is None or self.connectionRPi is None:
            connection, address = self.server.accept()
            buf = connection.recv(30)  # struct.calcsize('<B29s')

            if len(buf) > 0:
                blen, bstr = struct.unpack("<B29s", buf)
                rstr = bstr.decode('utf-8')
                c_id = rstr[:2]
                c_pass = rstr[2:blen]
                if c_id == "PC":
                    if self.connectionPC is None:
                        if c_pass == password:
                            self.connectionPC = connection
                            if self.connectionRPi is None:
                                connection.sendall(struct.pack("<B", 2))
                            else:
                                connection.sendall(struct.pack("<B", 1))
                                self.connectionRPi.sendall(struct.pack("<B", 3))
                        else:
                            connection.sendall(struct.pack("<B", 42))
                    else:
                        connection.sendall(struct.pack("<B", 43))
                elif c_id == "RP":
                    if self.connectionRPi is None:
                        if c_pass == password:
                            self.connectionRPi = connection
                            if self.connectionPC is None:
                                connection.sendall(struct.pack("<B", 2))
                            else:
                                connection.sendall(struct.pack("<B", 1))
                                self.connectionPC.sendall(struct.pack("<B", 3))
                        else:
                            connection.sendall(struct.pack("<B", 42))
                    else:
                        connection.sendall(struct.pack("<B", 43))
                else:
                    connection.sendall(struct.pack("<B", 41))
            else:
                connection.sendall(struct.pack("<B", 40))

    def close(self):
        self.server.close()
        self.connectionRPi.close()
        self.connectionPC.close()