"""
Original code taken from http://www.ibm.com/developerworks/webservices/library/ws-pyth11.html
Requires RSS.py by Mark Nottingham. Get it from http://www.mnot.net/python/RSS.py
Currently does _not_ require Beautiful Soup, if you need it, get it here: http://www.crummy.com/software/BeautifulSoup/ 
requires simpleOSC.py by ixi. get it here: http://www.ixi-software.net/content/body_backyard_python.html
"""

import sgmllib

# strip html from input to strip 
class Stripper(sgmllib.SGMLParser):
	def __init__(self):
		sgmllib.SGMLParser.__init__(self)
		
	def strip(self, some_html):
		self.theString = ""
		self.feed(some_html)
		self.close()
		return self.theString
		
	def handle_data(self, data):
		self.theString += data



def parseCurrentCond(csvRaw):
    """parse description text"""
    
    # split to lines and zap empty lines
    lines = [line.split(",") for line in csvRaw.splitlines() if line] 

    header = lines[0]
    lines = lines[1:]

    actual = lines[-1]

    valueDict = {}

    for (key, val) in zip(*[header, actual]):
        valueDict[key] = val

    for key in ["TemperatureC", "DewpointC", "Humidity", "WindDirectionDegrees", "WindSpeedKMH", "WindSpeedGustKMH", "PressurehPa", "SolarRadiationWatts/m^2", "dailyrainCM"]:
        if key in valueDict:
            valueDict[key] = float("".join([v for v in valueDict[key] if v.isdigit() or v == "."]))

    return valueDict
    

def getData(stationCode="IBIELEFE4"):
    """acquires rss data from wunderground and parses it for current weather conditions at stationCode"""
    
    import urllib, time

    uri = "http://www.wunderground.com/weatherstation/WXDailyHistory.asp?ID=%s&format=1" % stationCode
    f = urllib.urlopen(uri)
    raw = f.read()

    stripper = Stripper()
    raw = stripper.strip(raw)
    
    return parseCurrentCond(raw)


def main():
    
    import osc
    import time
    
    osc.init()
    
    
    for i in range(0, 100):
        
        print "acquiring new Data"
        
        valueDict = getData()
        # print valueDict
        
        msgArray = [
            "temp", valueDict["TemperatureC"], "hum", valueDict["Humidity"], "wind", valueDict["WindSpeedKMH"], "press", valueDict["PressurehPa"]
        ]
        
        for j in range(0, 5*60):
            print msgArray
            time.sleep(1)
        
    
    


if __name__ == '__main__':
    main()
