import struct

class ControllerState:
    
    format = "<bb"
    horizontal = 0
    vertical = 0

    def encode(self):
        return struct.pack(format, horizontal, vertical)

    def size(self):
        return struct.calcsize(format)

    def decode(self, binarydata):
        unpacked = struct.unpack(format, binarydata)
        horizontal = unpacked[0]
        vertical = unpacked[1]
