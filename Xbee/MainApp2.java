/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.digi.xbee.example;

import com.digi.xbee.api.WiFiDevice;
import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.models.XBeeProtocol;
public class MainApp2 {
    /* Constants */
    // TODO Replace with the port where your sender module is connected to.
    private static final String PORT = "/dev/ttyUSB0";
    // TODO Replace with the baud rate of your sender module.
    private static final int BAUD_RATE = 9600;
    private static final String DATA_TO_SEND = "Hello World!!!!";
    public static void main(String[] args) {
    XBeeDevice myDevice = new XBeeDevice(PORT, BAUD_RATE);
    byte[] dataToSend = DATA_TO_SEND.getBytes();
    int i=0;

    try {
        myDevice.open();
        while(i<10){
            System.out.format("Sending broadcast data: '%s'", new String(dataToSend));       
            if (myDevice.getXBeeProtocol() == XBeeProtocol.XBEE_WIFI) {
                myDevice.close();
                myDevice = new WiFiDevice(PORT, BAUD_RATE);
                myDevice.open();
                ((WiFiDevice)myDevice).sendBroadcastIPData(0x2616, dataToSend);
            } else
                myDevice.sendBroadcastData(dataToSend);      
            System.out.println(" >> Success");
            i++;
        }
            //Thread.sleep(250l);
        } catch (XBeeException e) {
            System.out.println(" >> Error");
            e.printStackTrace();
            System.exit(1);
        } finally {
            myDevice.close();
        }


    }
}
