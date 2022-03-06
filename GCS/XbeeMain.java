/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.digi.xbee.example;

import com.digi.xbee.api.WiFiDevice;
import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.models.XBeeProtocol;
import java.util.*;
//import com.digi.xbee.api.packet.common.ReceivePacket;

public class XbeeMain {
    /* Constants */
    // TODO Replace with the port where your sender module is connected to.
    private static final String PORT = "/dev/ttyUSB0";
    // TODO Replace with the baud rate of your sender module.
    private static final int BAUD_RATE = 9600;
    private static final String DATA_TO_SEND = "Hello Xbee World!!!!";
    private XBeeDevice myDevice;
    
    public void connectGCSXbee(){
        myDevice = new XBeeDevice(PORT, BAUD_RATE);
        
        if(myDevice.isOpen()){
            myDevice.close();
        }
        
        try {
            myDevice.open();
        } catch (XBeeException e) {
            System.out.println(" myDevice.open() >> Error");
            e.printStackTrace();
            System.exit(1);  
        }
    }
    
    public void closeXbeePort(){
        myDevice.close();
    }
    
    public boolean checkXbeePortConnection(){
        return myDevice.isOpen();
    }
    
    public String receiveUAVXbeeData(){
        String msg;
        while(myDevice.readData() == null){}
        msg = myDevice.readData().getDataString();
        System.out.println(msg);
        
        return msg;
    }
    
    public Map structUAVXbeeData(String msg){
        String[] str_Array = msg.split("\n");                  
        HashMap mMap = new HashMap();
        
        for (String str_Array1 : str_Array) {
            String[] parsed_str = str_Array1.split(": ");
           //System.out.println(parsed_str[1]);
            mMap.put(parsed_str[0], parsed_str[1]);
        }

        return mMap;
    }
    
    
    
    public static void main(String[] args){
        
        XbeeMain xbee1 = new XbeeMain();
        xbee1.connectGCSXbee();

        try{
            while(true){
                
                String msg;
                msg = xbee1.receiveUAVXbeeData();
                
                System.out.println(" ++++++++++++++");
                
                Map mMap = null;
                mMap = xbee1.structUAVXbeeData(msg);

                for (Object name: mMap.keySet()) {
                    String key = name.toString();
                    String value = mMap.get(name).toString();
                    System.out.println(key + " " + value);
                }

            }
        }finally{       
            xbee1.closeXbeePort();
        }
        
    }
    
    
    
 /*   
    public static void main(String[] args) {
    XBeeDevice myDevice = new XBeeDevice(PORT, BAUD_RATE);
    byte[] dataToSend = DATA_TO_SEND.getBytes();
    int i=0;
    try {
        myDevice.open();
        while(true){
            System.out.format("Sending broadcast data: '%s'", new String(dataToSend));       
            System.out.println(i);
            i++;
            if (myDevice.getXBeeProtocol() == XBeeProtocol.XBEE_WIFI) {
                myDevice.close();
                myDevice = new WiFiDevice(PORT, BAUD_RATE);
                myDevice.open();
                ((WiFiDevice)myDevice).sendBroadcastIPData(0x2616, dataToSend);
                System.out.println(" Here");
            } else{
                //myDevice.sendBroadcastData(dataToSend);  
                
                System.out.println(myDevice.get64BitAddress().toString());
                String msg;
                while(myDevice.readData() == null){}
                
                msg = myDevice.readData().getDataString();
                System.out.println(msg);


                
                //System.out.println(myDevice.readData().getDataString());             
            }
            System.out.println(" >> Success");
            //i++;
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
 */   
}
