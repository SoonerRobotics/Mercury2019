class ControllerState:
    
    axes = []
    buttons = []

    def encode(self):
        encodedString = ""
        for axis in self.axes:
            encodedString += str(axis) + ";"
        for button in self.buttons:
            encodedString += str(button)
        return encodedString.encode()

    def decode(self, string):
        if string.startswith("CC"):

            string = string[2:]
            results = string.split(";")

            print(results)
            self.axes = [0] * (len(results)-1)
            for x in range(len(results)-1):
                self.axes[x] = int(results[x])
            i = 0
            self.buttons = [0] * len(results[len(results)-1])
            for char in results[len(results)-1]:
                if i < len(self.buttons):
                    self.buttons[i] = int(char)
                    i += 1
