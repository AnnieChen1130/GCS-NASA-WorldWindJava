import time
from xbee import XBee
import serial

PORT = "COM4"
BAUDRATE = 9600

#open serial port
sender_port = serial.Serial(PORT, BAUDRATE)
print ("serial port object>>>", sender_port)

#xbee object API=2
sender = XBee(sender_port,escaped=True)

#address of the remote xbee to which data is to sent
ADDRESS = "\x00\x13\xA2\x00\x40\xD9\x6F\xE5"

#send data using the tx_long_addr
while True:
    try:
        print ("sending data...")
        sender.send('tx', frame_id='A', dest_addr="\x5E\x71", dest_addr_long="\x00\x13\xA2\x00\x40\xD9\x6F\xE5", data="Hello")
        #sender.send("HI")
        time.sleep(1)

    except KeyboardInterrupt:
        break


sender.halt()
sender_port.close()