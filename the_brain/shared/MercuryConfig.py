import configparser

class MercuryConfig:

    ip = ""
    port = ""
    password = ""
    
    @classmethod
    def read(cls):
        config = configparser.ConfigParser()
        try:
            config.readfp(open('merc_bot.ini'))
            cls.ip = config.get('NETWORK', 'ip')
            cls.port = config.get('NETWORK', 'port')
            cls.password = config.get('NETWORK', 'password')
        except:
            print("Could not find merc_bot.ini, creating empty file. Please fill these in.")
            config['NETWORK'] = {"ip":"", "port":"", "password":""}
            try:
                with open('merc_bot.ini', 'w') as configfile:
                    config.write(configfile)
            except:
                print("Could not write config file to merc_bot.ini")