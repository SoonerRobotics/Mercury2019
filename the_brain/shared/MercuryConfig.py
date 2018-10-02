import configparser

class MercuryConfig:

    ip = ""
    port = ""
    password = ""
    
    @classmethod
    def read(cls):
        config = configparser.ConfigParser()
        if config.read("merc_bot.ini").count == 0:
            print("Could not find merc_bot.ini, creating empty file. Please fill these in.")
            config['NETWORK'] = {"ip":"", "port":"", "password":""}
            try:
                with open('example.ini', 'w') as configfile:
                    config.write(configfile)
            except:
                print("Could not write config file to merc_bot.ini")
        else:
            cls.ip = config["NETWORK"]["ip"]
            cls.port = config["NETWORK"]["port"]
            cls.password = config["NETWORK"]["password"]