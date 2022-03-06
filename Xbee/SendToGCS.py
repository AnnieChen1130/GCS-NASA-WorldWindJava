import time
from xbee import XBee
import serial
from digi.xbee.devices import XBeeDevice

PORT = "/dev/ttyUSB2"
BAUDRATE = 9600

#open serial port
device = XBeeDevice(PORT, BAUDRATE)
device.open()

def send_UAV_data_Xbee(ICAO, pos_lat, pos_lon, pos_alt_rel,velocity,airspeed):
    
    #print("In send ADSB funtion\n")
    msg = "ICAO: " + ICAO + '\n'
    msg += "Lattitude: " + pos_lat + '\n'
    msg += "Longitude: " + pos_lon + '\n'
    msg += "Altitude: " + pos_alt_rel + '\n'
    msg += "Velocity: " + velocity + '\n'
    msg += "Airspeed: " + airspeed + '\n'

    return msg

coordinateList = [
"34.0433, -117.8124",
"34.0432, -117.8122",
"34.0430, -117.8120",
"34.0431, -117.8118",
"34.0433, -117.8117", 
"34.0434, -117.8116",
"34.0436, -117.8114",
"34.0438, -117.8118",
"34.0440, -117.8119",
"34.0441, -117.8121"]

i=0

while True:
    #try:
    if(i == len(coordinateList)):
        i=0
    parsed_string = coordinateList[i].split(', ')
    Lattitude = parsed_string[0]
    Longitude = parsed_string[1]
    print(coordinateList[i])

    i+=1

    print ("sending data...")
    device.send_data_broadcast(send_UAV_data_Xbee("B", Lattitude, Longitude, "50","15.0","5.0"))
    #device.send_data_broadcast()
    time.sleep(1)

    #except KeyboardInterrupt:
    #    break















