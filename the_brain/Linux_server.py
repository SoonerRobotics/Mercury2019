import threading
from shared.MercuryConfig import MercuryConfig
from shared.MercuryConnection import MercuryServer

conf = MercuryConfig()


def controlserver():
    server = MercuryServer()
    server.connect(conf.port, conf.password)

    try:
        while True:
            server.send(server.get("PC"), "RP")  # Redirect PC to RP
    finally:
        server.close()


def camserver():
    server = MercuryServer()
    server.connect(conf.port, conf.password)

    try:
        while True:
            server.send(server.get("RP"), "PC")  # Redirect RP to PC
    finally:
        server.close()


threading.Thread(target=controlserver).start()
threading.Thread(target=camserver).start()
