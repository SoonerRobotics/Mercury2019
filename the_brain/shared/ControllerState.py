class ControllerState:
    
    axes = [0.0,0.0,0.0,0.0,0.0]
    buttons = [0,0,0,0,0,0,0,0,0,0]

    def encode(self):
        encodedString = ""
        for axis in self.axes:
            encodedString += str(axis) + ";"
        for button in self.buttons:
            encodedString += str(button)
        return encodedString.encode()

    def decode(self, string):
        results = string.split(";")
        print(results)
        for x in range(5):
            self.axes[x] = float(results[x])
        i = 0
        for char in results[5]:
            self.buttons[i] = int(char)
            i += 1
