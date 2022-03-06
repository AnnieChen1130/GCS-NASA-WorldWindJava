import time
from xbee import XBee
import serial

PORT = "/dev/ttyUSB1"
BAUDRATE = 9600

def byte2hex(byteStr):
     return ''.join(["%02X" % x for x in byteStr]).strip()

def decodereceivedFrame(data):
     source_address = byte2hex(data['source_addr'])
     xbee_id = data['id']
     rf_data = data['rf_data']
     options = byte2hex(data['options'])
     return [source_address, xbee_id, rf_data, options]

#open serial port at receiving end
remote = serial.Serial(PORT, BAUDRATE)

#xbee object API=2
remote_xbee = XBee(remote, escaped=True)

while True:
    try:
       print ("yes i m here")
       data = remote_xbee.wait_read_frame()
       print ("data >>>", data)
       decoderdata = decodereceivedFrame(data)
       print ("data received<<<<", decoderdata)

    except KeyboardInterrupt:
       break

remote_xbee.halt()
remote.close()
