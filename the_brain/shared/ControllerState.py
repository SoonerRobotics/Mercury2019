import struct

class ControllerState:
    
    format = "<bb"
    horizontal = 0
    vertical = 0

    def encode(self):
        return struct.pack(self.format, self.horizontal, self.vertical)

    def size(self):
        return struct.calcsize(self.format)

    def decode(self, binarydata):
        unpacked = struct.unpack(self.format, binarydata)
        self.horizontal = unpacked[0]
        self.vertical = unpacked[1]
