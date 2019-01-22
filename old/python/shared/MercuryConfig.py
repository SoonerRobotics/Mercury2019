import configparser
from typing import Any, Union


class MercuryConfig:

    def __init__(self):
        self.ip = ""
        self.port = ""
        self.password = ""

        config = configparser.ConfigParser()
        try:
            config.read_file(open('merc_bot.ini'))
            self.ip = config.get('NETWORK', 'ip')
            self.port = int(config.get('NETWORK', 'port'))
            self.password = config.get('NETWORK', 'password')
        except:
            print("Could not find merc_bot.ini, creating empty file. Please fill these in.")
            config['NETWORK'] = {"ip":"", "port":"", "password":""}
            try:
                with open('merc_bot.ini', 'w') as configfile:
                    config.write(configfile)
            except:
                print("Could not write config file to merc_bot.ini")