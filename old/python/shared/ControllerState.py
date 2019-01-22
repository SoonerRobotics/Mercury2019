import struct

class ControllerState:
    
    strFormat = "<BB"

    def __init__(self):
        self.horizontal = 0
        self.vertical = 0

    def encode(self):
        return struct.pack(self.strFormat, self.horizontal, self.vertical)

    @classmethod
    def size(cls):
        return struct.calcsize(cls.strFormat)

    def decode(self, binarydata):
        unpacked = struct.unpack(self.strFormat, binarydata)
        self.horizontal = unpacked[0]
        self.vertical = unpacked[1]
