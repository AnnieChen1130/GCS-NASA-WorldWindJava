import time
from xbee import XBee
import serial
from digi.xbee.devices import XBeeDevice

PORT = "COM9"
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

while True:

    try:
        print ("Receive data...")
        #device.send_data_broadcast(send_UAV_data_Xbee("A", "34.00", "-174.50", "30","10.0","5.0"))
        #device.send_data_broadcast("Com\n")
        msg = device.read_data()
        print(msg)
        time.sleep(1)

    except KeyboardInterrupt:
        break