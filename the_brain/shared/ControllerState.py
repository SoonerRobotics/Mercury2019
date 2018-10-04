import struct

class ControllerState:
    
    strFormat = "<BB"
    horizontal = 0
    vertical = 0

    def encode(self):
        return struct.pack(strFormat, self.horizontal, self.vertical)

    def size():
        return struct.calcsize(strFormat)

    def decode(self, binarydata):
        unpacked = struct.unpack(strFormat, binarydata)
        self.horizontal = unpacked[0]
        self.vertical = unpacked[1]
