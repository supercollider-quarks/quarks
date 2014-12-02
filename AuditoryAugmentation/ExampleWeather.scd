// Example Weather data

// cold, no wind
NetAddr.localAddr.sendMsg("/weather", 'temp', 0, 'hum', 45.0, 'wind', 0, 'press', 1000.0)

// ultracold, windy
NetAddr.localAddr.sendMsg("/weather", 'temp', -10, 'hum', 55.0, 'wind', 20.01, 'press', 950.0)


// warm, windy, humid
NetAddr.localAddr.sendMsg("/weather", 'temp', 30, 'hum', 85.0, 'wind', 20.01, 'press', 1000.0)

// summer, approx. no wind
NetAddr.localAddr.sendMsg("/weather", 'temp', 16, 'hum', 65.0, 'wind', 3, 'press', 10016.0)

NetAddr.localAddr.sendMsg("/weather", 'temp', 16, 'hum', 65.0, 'wind', 4, 'press', 10016.0)
NetAddr.localAddr.sendMsg("/weather", 'temp', 16, 'hum', 65.0, 'wind', 4.1, 'press', 10016.0)
