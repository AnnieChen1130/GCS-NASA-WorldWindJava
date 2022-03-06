#import cv2 as cv
import serial
import RPi.GPIO as GPIO
import numpy as np
import dronekit as dk
#import dronekit_sitl
from pymavlink import mavutil
import time
import math
from time import gmtime, strftime
import os


import time
from xbee import XBee
import serial
from digi.xbee.devices import XBeeDevice


def arm_and_takeoff(aTargetAltitude):
    print("Basic pre-arm checks")
    while not vehicle.is_armable:
        print(" Waiting for vehicle to initialise...")
        time.sleep(1)

    print("Arming motors")
    vehicle.mode = dk.VehicleMode("GUIDED")
    vehicle.armed = True
    while not vehicle.armed:
        print(" Waiting for arming...")
        time.sleep(1)

    time.sleep(8)

    print("Taking off!")
    vehicle.simple_takeoff(aTargetAltitude)  # Take off to target altitude
    while True:
        print(" Altitude: ", vehicle.location.global_relative_frame.alt)
        if vehicle.location.global_relative_frame.alt >= aTargetAltitude * 0.95:
            print("Reached target altitude")
            break
        time.sleep(1)

def send_ned_velocity(velocity_x, velocity_y, velocity_z):
    msg = vehicle.message_factory.set_position_target_local_ned_encode(
        0,  # time_boot_ms (not used)
        0, 0,  # target system, target component
        mavutil.mavlink.MAV_FRAME_BODY_NED,  # frame
        0b0000111111000111,  # type_mask (only speeds enabled)
        0, 0, 0,  # x, y, z positions (not used)
        velocity_x, velocity_y, velocity_z,  # x, y, z velocity in m/s
        0, 0, 0,  # x, y, z acceleration (not supported yet, ignored in GCS_Mavlink)
        0, 0)  # yaw, yaw_rate (not supported yet, ignored in GCS_Mavlink)
    vehicle.send_mavlink(msg)
    vehicle.flush()


def goto_position_target_local_ned(north, east, down): #
														
    msg = vehicle.message_factory.set_position_target_local_ned_encode(
        0,  # time_boot_ms (not used)
        0, 0,  # target system, target component
        mavutil.mavlink.MAV_FRAME_BODY_NED,  # frame
        0b0000111111111000,  # type_mask (only positions enabled)
        north, east, down,  # x, y, z positions (or North, East, Down in the MAV_FRAME_BODY_NED frame
        0, 0, 0,  # x, y, z velocity in m/s  (not used)
        0, 0, 0,  # x, y, z acceleration (not supported yet, ignored in GCS_Mavlink)
        0, 0)  # yaw, yaw_rate (not supported yet, ignored in GCS_Mavlink)
    # send command to vehicle
    vehicle.send_mavlink(msg)
    vehicle.flush()

#calculate the distance from waypoint
def distance_to_waypoint (clocation, nwaypoint): # arguments are current location and waypoint
    R = 6371000 #radius of Earth in meters
    x = math.pi*(nwaypoint[1]-clocation[1])/180*math.cos(math.pi/180*(clocation[0]+nwaypoint[0])/2)
    X = R*x
    y = math.pi/180*(nwaypoint[0]-clocation[0])
    Y = R*y
    return math.sqrt(X**2+Y**2)

def send_UAV_data_Xbee(ICAO, pos_lat, pos_lon, pos_alt_rel,velocity,airspeed):
    
    #print("In send ADSB funtion\n")
    msg = "ICAO: " + ICAO + '\n'
    msg += "Lattitude: " + pos_lat + '\n'
    msg += "Longitude: " + pos_lon + '\n'
    msg += "Altitude: " + pos_alt_rel + '\n'
    msg += "Velocity: " + velocity + '\n'
    msg += "Airspeed: " + airspeed + '\n'

    return msg

#----------------------------------------------KEEP IT THERE FOR REFERENCE
#connection_string = '/dev/ttyACM0'	#Establishing Connection With Flight Controller
#vehicle = dk.connect(connection_string, wait_ready=True, baud=115200)
#cmds = vehicle.commands
#cmds.download()
#cmds.wait_ready()
#waypoint1 = dk.LocationGlobalRelative(cmds[0].x, cmds[0].y, 3)  # Destination point 1
#----------------------------------------------

#END of definitions!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

#Setting up GPIO
#GPIO.setmode(GPIO.BCM)
#GPIO.setup(18, GPIO.OUT)
#p = GPIO.PWM(18, 50)
#p.start(2.5)
#time.sleep(1)

#setting up xbee communication
#GPIO.setwarnings(False)
'''
ser = serial.Serial(
    
    port='/dev/ttyUSB0',
    baudrate = 9600,
    parity=serial.PARITY_NONE,
    stopbits=serial.STOPBITS_ONE,
    bytesize=serial.EIGHTBITS,
    timeout=1   
)
'''

PORT = "/dev/ttyUSB0"
BAUDRATE = 9600

#open serial port
device = XBeeDevice(PORT, BAUDRATE)
device.open()

#!!!!!!!!!!!!!IN CASE WE USE DRONE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

# Before initializing, wait for a press of a button
# print("Now waiting for Button Press...")
# btn="off"
# while btn !="on": #if incoming bytes are waiting to be read from the serial input buffer
#     btn=ser.read(5).decode()
	
# # LISTENING FOR COORDINATES loop ------------

        
# once the right message has been received
# RETRIEVE coordinates it this way: ------------------------------

lat = float(34.0435614)
lon = float(-117.8115872)
Targ_Location = dk.LocationGlobalRelative(lat, lon, 12)  
# NOW  that we have string translated to coordinates
# !!!!!!!!!!!IN CASE WE USE DRONE - WAIT AND START Flying !!!!!!!!!
print ("Coordinates saved, Connecting to vehicle")
connection_string = '/dev/ttyACM0'	#Establishing Connection With PIXHAWK
vehicle = dk.connect(connection_string, wait_ready=True, baud=115200)# PIXHAWK is PLUGGED to NUC (RPi too?) VIA USB
#!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
#cmds = vehicle.commands
#cmds.download() #DOWNLOAD DATA FROM PIXHAWK (ALL)
#cmds.wait_ready()
#waypoint1 = dk.LocationGlobalRelative(cmds[0].x, cmds[0].y, 3)  #FROM ALL DATA DOWNLOADED FROM PIXHAWK, GET 1st WAYPOINT COORDINATES 
arm_and_takeoff(12)
vehicle.airspeed = 2.0 # set drone speed to be used with simple_goto
vehicle.simple_goto(Targ_Location)#trying to go to TARGET LOCATION							
#------------------------------------------------------------
target_reached = 0
while not target_reached:
#    lat = vehicle.location.global_relative_frame.lat  # get the current latitude
#    lon = vehicle.location.global_relative_frame.lon  # get the current longitude

    current_loc = [vehicle.location.global_relative_frame.lat, vehicle.location.global_relative_frame.lon] #current coordinates of the drone
    distance_wp = distance_to_waypoint(current_loc, [Targ_Location.lat,Targ_Location.lon]) #distance to next waypoint

    print ("sending data...")
    device.send_data_broadcast(send_UAV_data_Xbee("B", vehicle.location.global_relative_frame.lat, vehicle.location.global_relative_frame.lon, "50","15.0","5.0"))
    #device.send_data_broadcast()
    time.sleep(1)

    if distance_wp < 2:  
	    target_reached = 1
    else:
        print(distance_wp)
print ("Reached Target Location")
send_ned_velocity(0, 0, 0)  # stop the vehicle AT THE TARGET LOCATION
time.sleep(3) #for 3 seconds

# DROP PAYLOAD --------------------------------
#HERE ShOULD BE THE SERVO CODE

# RETURN HOME CODE ----------------------------
vehicle.mode    = dk.VehicleMode("RTL")
print ("Returning RTL")
vehicle.flush()
print ("Vehicle Flushed")

