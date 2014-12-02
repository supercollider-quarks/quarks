"""
Original code taken from http://www.ibm.com/developerworks/webservices/library/ws-pyth11.html
Requires RSS.py by Mark Nottingham. Get it from <http://www.mnot.net/python/RSS.py>. It should be lying next to this file.
Currently does _not_ require Beautiful Soup, if you need it, get it here: http://www.crummy.com/software/BeautifulSoup/ 
requires simpleOSC.py by ixi. get it here: http://www.ixi-software.net/content/body_backyard_python.html
"""

def parseCurrentCond(description):
    """parse description text. Assumes that it is of type Current Condition"""
    
    raw = [items.split(":") for items in description.split("|")]

 
    valueDict = {};
    for rawItem in raw:
        valueDict[rawItem[0].replace(" ", "")] = " ".join(rawItem[1:]).split(" / ")[-1].strip()


    for key in ["Temperature", "Humidity", "WindSpeed", "Pressure"]:
        if key in valueDict:
            valueDict[key] = float("".join([v for v in valueDict[key] if v.isdigit() or v == "."]))
            
    return valueDict

"""    Graz: 11240
    Bielefeld: 10326"""

def getRSSData(stationCode=10326):
    """acquires rss data from wunderground and parses it for current weather conditions at stationCode"""
    """stationCode for Graz: 11240"""
    
    from RSS import ns, CollectionChannel, TrackingChannel
    
    #Create a tracking channel, which is a data structure that
    #Indexes RSS data by item URL
    tc = TrackingChannel()

    #Returns the RSSParser instance used, which can usually be ignored
    tc.parse("http://rss.wunderground.com/auto/rss_full/global/stations/%s.xml" % stationCode)


    RSS10_TITLE = (ns.rss10, 'title')
    RSS10_DESC = (ns.rss10, 'description')

    #You can also use tc.keys()
    items = tc.listItems()


    item = items[0]
    #Each item is a (url, order_index) tuple
    url = item[0]

    #Get all the data for the item as a Python dictionary
    item_data = tc.getItem(item)

    title = item_data.get(RSS10_TITLE, "(none)")
    description = item_data.get(RSS10_DESC, "(none)")


    #print "Title:", title
    #print "Description:", item_data.get(RSS10_DESC, "(none)")

    if title.find("Current Conditions") >= 0:
        valueDict = parseCurrentCond(description)

    return valueDict



def main():
    
    import osc
    import time
    
    osc.init()
    
    
    while 1:
        print "acquiring new Data"
        
        valueDict = getRSSData()
        
        msgArray = [
            "temp", valueDict["Temperature"], "hum", valueDict["Humidity"], "wind", valueDict["WindSpeed"], "press", valueDict["Pressure"]
        ]
        
        print msgArray
        for j in range(0, 59):
            osc.sendMsg("/weather", msgArray, "127.0.0.1", 57120) # send normal msg
            time.sleep(10)

    
    


if __name__ == '__main__':
    main()






"""
for item in items:
    #Each item is a (url, order_index) tuple
    url = item[0]
    #print "RSS Item:", url
    #Get all the data for the item as a Python dictionary
    item_data = tc.getItem(item)
    #print "Title:", item_data.get(RSS10_TITLE, "(none)")
    #print "Description:", item_data.get(RSS10_DESC, "(none)")
    # item_data.get(RSS10_TITLE, "(none)") is of type str
    location = item_data.get(RSS10_TITLE, "(none)").split()[-1]
    description = item_data.get(RSS10_DESC, "(none)")
"""
