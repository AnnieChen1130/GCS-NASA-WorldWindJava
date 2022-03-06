/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gov.nasa.worldwindx.examples;

import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.example.XbeeMain;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.EarthFlat;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import static gov.nasa.worldwindx.examples.ApplicationTemplate.start;

import java.util.concurrent.atomic.*;

import java.time.*;
import java.util.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
//import javax.swing.*;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author cppuav
 */
public class GCS5 extends ApplicationTemplate{
    
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        private Map<Integer,PointPlacemark> placemarkList=new HashMap<>();
        private Map<Integer,Path> pathList=new HashMap<>();
        private ArrayList<Color> ColorList = new ArrayList<Color>();
        //private LinkedList<Vehicle> VehicleList=new LinkedList<>();
        //private ArrayList<Vehicle> VehicleList=new ArrayList<>(); 
        private Map<Integer,Boolean> CLickedPathButtonList=new HashMap<>(); 
        private int vehicleNum = 0;        
        private int ConnectButtonID = 0;
        private int DisconnectButtonID = 0;
        private int PathButtonID = 0;
        private boolean ClickedNewVehicleButton = false;
        private boolean ClickedConnectButton = false;
        private boolean ClickedDisconnectButton = false;
        private final XbeeMain xbee1 = new XbeeMain();
        private Map<Integer,String> ICAOList=new HashMap<>();
        
        public AppFrame()
        {
            super(true, true, false);           
            
            LayerList layers = getWwd().getModel().getLayers();
            for (Layer layer: layers) {
                String layerName = layer.getName();
                if (layerName != null && layerName.toLowerCase().startsWith("bing")) {
                    layer.setEnabled(true);
                    break;
                }

            }
            
            // Add view controls selection panel
            this.getControlPanel().add(makeConnectionPanel(), BorderLayout.SOUTH);
        }
        
        private JPanel makeConnectionPanel() {
            JPanel connetionPanel = new JPanel();
            connetionPanel.setLayout(new BoxLayout(connetionPanel, BoxLayout.Y_AXIS));
            connetionPanel.setBorder(
                    new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("Connect Vehicles")));
            
            //Xbee button
            JButton xbeeButton = new JButton("Connect Xbee");
            AtomicBoolean ClickedXbeeButton = new AtomicBoolean(false);
            xbeeButton.addActionListener((ActionEvent actionEvent) -> {
                if (ClickedXbeeButton.get() == false){
                    xbeeButton.setText("Disconnect Xbee");
                    xbee1.connectGCSXbee();
                    ClickedXbeeButton.lazySet(true);
                } else{
                    xbeeButton.setText("Connect Xbee");
                    xbee1.closeXbeePort();
                    ClickedXbeeButton.lazySet(false);
                }
            });            
                        
            //New Vehicle buttons
            JButton newVehicleButton = new JButton("New Vehicle");
            newVehicleButton.addActionListener((ActionEvent actionEvent) -> {
                this.vehicleNum++;
                this.ClickedNewVehicleButton = true;
                this.ColorList.add(this.creatRandomColor());
                JPanel newVehicleConnectionPanel = newVehicleConnectionPanel(vehicleNum);
                connetionPanel.add(newVehicleConnectionPanel);
                getControlPanel().revalidate();
                getWwd().redraw();
                
            });
            
            JPanel smallPanel = new JPanel(new GridLayout(0, 2));
            
            xbeeButton.setLocation(0,0);
            newVehicleButton.setLocation(0,1);  
            
            smallPanel.add(xbeeButton);
            smallPanel.add(newVehicleButton);
            
            connetionPanel.add(smallPanel);
            
            return connetionPanel;
        }
        
        private JPanel newVehicleConnectionPanel(int numOfVehicle){
                       
            String vehicleName = "Vehicle " + numOfVehicle;
            AtomicBoolean vehicleType = new AtomicBoolean(true); //True: detection; False suppression
            
            RenderableLayer vehiclePPLayer = new RenderableLayer();
            RenderableLayer lineLayer = new RenderableLayer();            
            
            //Vehicle panel
            JPanel vehiclePanel = new JPanel();
            vehiclePanel.setLayout(new BoxLayout(vehiclePanel, BoxLayout.Y_AXIS));
            vehiclePanel.setBorder(
                    new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder(vehicleName)));
 
            //Vehicle ICAO panel
            JPanel IDPanel = new JPanel(new GridLayout(0, 3));
            IDPanel.setBorder(
                    new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("")));            
            Label IDLabel = new Label("ICAO:");
            JTextField IDinput = new JTextField();
            JButton enterButton = new JButton("Enter");
            //enterButton.putClientProperty("id", vehicleNum-1);
            enterButton.addActionListener((ActionEvent actionEvent) -> {
                ICAOList.put(vehicleNum-1, IDinput.getText());
                IDinput.setEditable(false);
            });   
            
            IDPanel.add(IDLabel);
            IDPanel.add(IDinput);
            IDPanel.add(enterButton);
            
            vehiclePanel.add(IDPanel);            
            
            // Buttons layout panel
            JPanel layoutPanel = new JPanel(new GridLayout(0, 2, 0, 0));
            
            //Radio Buttuon
            ButtonGroup group = new ButtonGroup();
            JRadioButton button = new JRadioButton("Fire Detection", true);
            group.add(button);
            button.addActionListener((ActionEvent actionEvent) -> {
                vehicleType.lazySet(true);
            });
            layoutPanel.add(button);
            button = new JRadioButton("Fire Suppression", false);
            group.add(button);
            button.addActionListener((ActionEvent actionEvent) -> {
                vehicleType.lazySet(false);
            });
            layoutPanel.add(button);              
            
            //Connect buttons
            JButton connectButton = new JButton("Connect");
            connectButton.putClientProperty("id", vehicleNum-1);
            connectButton.addActionListener((ActionEvent actionEvent) -> {
                this.ClickedConnectButton = true;
                ConnectButtonID = (int) connectButton.getClientProperty("id");                
                
                if(this.placemarkList.get(ConnectButtonID) == null){
                    PointPlacemark pp = new PointPlacemark(Position.fromDegrees(34.0434, -117.8126, 2e4));                    
                    this.placemarkList.put(ConnectButtonID, pp);
                    
                    Path line = new Path();
                    this.pathList.put(ConnectButtonID, line);
                    
                    addVehicleIcon(vehiclePPLayer, pp, numOfVehicle, vehicleType.get());
                    getWwd().redraw();                
                }                                    
            });
            layoutPanel.add(connectButton);

            //Disconnect buttons
            JButton disconnectButton = new JButton("Disconnect");
            disconnectButton.putClientProperty("id", vehicleNum-1);
            disconnectButton.addActionListener((ActionEvent actionEvent) -> {
                this.ClickedDisconnectButton = true;
                DisconnectButtonID = (int) disconnectButton.getClientProperty("id");
                if(this.placemarkList.get(DisconnectButtonID)!=null){
                    removeVehicleIcon(vehiclePPLayer);
                    removePath(lineLayer);
                    getWwd().redraw();
                }
            });
            layoutPanel.add(disconnectButton);
          
            //Show/Hide Path buttons
            JButton pathButton = new JButton("Show Path");
            pathButton.putClientProperty("id", vehicleNum-1);

            boolean ClickedPathButton = false;
            this.CLickedPathButtonList.put((int) pathButton.getClientProperty("id"),ClickedPathButton);
            
            pathButton.addActionListener((ActionEvent actionEvent) -> {
                PathButtonID = (int) pathButton.getClientProperty("id"); 
                               
                if(this.CLickedPathButtonList.get(PathButtonID) == false){                            
                    this.CLickedPathButtonList.replace(PathButtonID, true);
                    pathButton.setText("Hide Path");
                                     
                    this.showPath(lineLayer, this.pathList.get(PathButtonID), PathButtonID, this.ColorList.get(PathButtonID));
                    
                }    
                else{
                    this.CLickedPathButtonList.replace(PathButtonID, false);
                    pathButton.setText("Show Path");
                    this.removePath(lineLayer);
                }

                System.out.println("click path: "+ this.CLickedPathButtonList.get(PathButtonID));
                
                
                getWwd().redraw();
            });
            layoutPanel.add(pathButton);
            
            vehiclePanel.add(layoutPanel);
                    
            return vehiclePanel;
        }
               
        private void addVehicleIcon(RenderableLayer layer, PointPlacemark pp, int numOfVehicle, boolean vehicleType){
            String vehicleName = "Vehicle " + numOfVehicle;
            
            layer.setPickEnabled(true);
            layer.setName(vehicleName);
            layer.removeAllRenderables();
            
            pp.setLabelText(vehicleName);
            pp.setValue(AVKey.DISPLAY_NAME, "Clamp to ground, Label, Semi-transparent, Audio icon");
            pp.setLineEnabled(false);
            pp.setHighlighted(true);
            pp.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            pp.setEnableLabelPicking(true); // enable label picking for this placemark
            PointPlacemarkAttributes attrs = new PointPlacemarkAttributes();
            attrs.setImageAddress("src/images/quadcopter.png");
            //attrs.setImageColor(new Color(1f, 1f, 1f, 0.6f));
            attrs.setScale(0.8);
            attrs.setLabelOffset(new Offset(0.9d, 0.6d, AVKey.FRACTION, AVKey.FRACTION));
            pp.setAttributes(attrs);
            
            
            
            if (vehicleType){//Detection Red
                attrs.setImageColor(Color.RED);
               
            }
            else {//Suppression Yellow
                attrs.setImageColor(Color.YELLOW);
                pp.moveTo(Position.fromDegrees(34.0433, -117.8124));
            }
              
            
            layer.addRenderable(pp);   
            
            //ApplicationTemplate.insertAfterPlacenames(this.getWwd(), layer);
            this.getWwd().getModel().getLayers().add(layer);

        }
        
        private void removeVehicleIcon(RenderableLayer layer){
            layer.removeAllRenderables();
            this.placemarkList.remove(DisconnectButtonID);
        }
        
        private void showPath(RenderableLayer layer, Path line, int id, Color color){
            //System.out.println("show path id: " + id);
                       
            line.setSurfacePath(true);
            ShapeAttributes attrs = new BasicShapeAttributes();           
            Material m = new Material(color); 
            
            attrs.setOutlineWidth(3);
            attrs.setOutlineMaterial(m);
            
            line.setAttributes(attrs);
            layer.addRenderable(line);
            this.getWwd().getModel().getLayers().add(layer);
        }
        
        private void removePath(RenderableLayer layer){
            layer.removeAllRenderables();
            if(this.ClickedDisconnectButton)
                this.pathList.remove(DisconnectButtonID);
        }   
        
        private Color creatRandomColor(){
            Random rand = new Random();
            // Java 'Color' class takes 3 floats, from 0 to 1.
            float r = rand.nextFloat();
            float g = rand.nextFloat();
            float b = rand.nextFloat();
            Color randomColor = new Color(r, g, b);
            
            return randomColor;
        }
        
                
        
        public static void pause(double seconds) { 
            try { 
                Thread.sleep((long) (seconds * 1000)); 
            } 
            catch (InterruptedException e) {} 
        } 
               
        public PointPlacemark getPlacemark(int vehicleNum) {            
            return this.placemarkList.get(vehicleNum);
        }
        
        public boolean hasPlacemark(){
            //System.out.print(this.placemarkList.isEmpty());
            return !this.placemarkList.isEmpty();
        }
        
        public Path getPath(int vehicleNum) {            
            return this.pathList.get(vehicleNum);
        }
        
        public int getVehicleNum(){
            return this.vehicleNum;
        }    
        
        public int getConnectButtonID(){
            return this.ConnectButtonID;
        }
        
        public int getDisconnectButtonID(){
            return this.DisconnectButtonID;
        }
        
        public boolean getClickNewVehicleButton(){
            return this.ClickedNewVehicleButton;
        }
        
        public void resetClickNewVehicleButton(){
            this.ClickedNewVehicleButton = false;
        }
        
        public boolean getClickConnectButton(){
            return this.ClickedConnectButton;
        }
        
        public void resetClickConnectButton(){
            this.ClickedConnectButton = false;
        }
        
        public boolean getClickDisconnectButton(){
            return this.ClickedDisconnectButton;
        }
        
        public void resetClickDisconnectButton(){
            this.ClickedDisconnectButton = false;
        }
        
        public boolean getClickPathButton(int id){
            return this.CLickedPathButtonList.get(id);
        }
        
        public XbeeMain getXbee(){
            return this.xbee1;
        }
        
        public String getICAOnum(int id){
            return this.ICAOList.get(id);
        }
        
    }
    
    public static class Vehicle{
        private int ID;
        private boolean vehicleType;
        
        public Vehicle(int vehicleNum, boolean vehicleType){
            this.ID = vehicleNum;
            this.vehicleType = vehicleType;
        }
        
        public int getVehicleID(){
            return this.ID;
        }
        
        public boolean getVehicleType(){
            return vehicleType;
        }
        
        public void setVehicleType(boolean vehicleType){
            this.vehicleType = vehicleType;
        }
        
    }
    
    
    public static class AnimationThread extends Thread {

        protected GCS5.AppFrame appFrame;
        private final int id;    
        private boolean stop = false;
        private boolean running = false;
        //ArrayList<Position> pathPositions = new ArrayList<Position>();
        private final XbeeMain xbee;

        public AnimationThread(int id, GCS5.AppFrame appFrame) {
            this.id = id;
            this.appFrame = appFrame;
            this.xbee = appFrame.getXbee();
        }

        /*
        public ArrayList<Position> getPath(){
            //return this.pathPositions;
        }*/
                  
        public void stopThread(){
            this.stop = true;
            this.running = false;
        }
        
        public boolean isRunning(){
            return this.running;
        }
        
        @Override
        public void run() {
            stop = false;
            running = true;

            ArrayList<Position> iconPositions = new ArrayList<Position>();
            PointPlacemark placemark = appFrame.getPlacemark(id);
            ArrayList<Position> pathPositions = new ArrayList<Position>();
            WorldWindow wwd = this.appFrame.getWwd();
            String threadICAO = appFrame.getICAOnum(id);

            // Create Location Output File
            LocalDate todayDate = LocalDate.now();
            String filename = threadICAO+"-locationOut-"+todayDate.toString() + ".txt";
            File outputFile = new File("/home/cppuav/Desktop", filename);   
            
            while(!xbee.checkXbeePortConnection()){}
                        
            try{
                PrintWriter printWriter = new PrintWriter(new FileWriter(outputFile));
                while(!stop){
                    System.out.println(" ------------ ");
                    System.out.println(id);
                    //Thread.sleep(250l);
                    String msg;
                    msg = xbee.receiveUAVXbeeData();

                    System.out.println(" ++++++++++++++");

                    Map<String, String> mMap = null;
                    mMap = xbee.structUAVXbeeData(msg);
                    
                    String receiveICAO = mMap.get("ICAO");//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    System.out.println(receiveICAO);
                    System.out.println(threadICAO);
                    //if(threadICAO.contains(receiveICAO)){
                            // System.out.println(" !!!!Here!!");

                    for (String name: mMap.keySet()) {
                        String key = name;
                        String value = mMap.get(name);
                        System.out.println(key + " " + value);
                    }


                    //iconPositions.add(Position.fromDegrees(Double.parseDouble(mMap.get("Lattitude")), Double.parseDouble(mMap.get("Longitude")), 0));
                    System.out.println(" ===============");
                    System.out.println(mMap.get("Lattitude"));
                    System.out.println(mMap.get("Longitude"));
                    
                    LocalTime currentTime = LocalTime.now();
                    printWriter.printf("------- %s -----------", currentTime.toString());
                    printWriter.println();
                    printWriter.printf("Lattitude: %s",mMap.get("Lattitude"));
                    printWriter.println();
                    printWriter.printf("Longitude: %s",mMap.get("Longitude"));
                    printWriter.println();


                    Position pos = Position.fromDegrees(Double.parseDouble(mMap.get("Lattitude")), Double.parseDouble(mMap.get("Longitude")), 0);

                    System.out.println(pos);

                    placemark.setPosition(pos);
                    pathPositions.add(pos);
                    this.appFrame.getPath(id).setPositions(pathPositions);
                    wwd.redraw();
                    //}
                }
                
                printWriter.close();
                
            }catch (Exception ex) {
                ex.printStackTrace();
            }

        }

    
    }
    
    
    
    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.GLOBE_CLASS_NAME, EarthFlat.class.getName());
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 34.0434);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -117.8126);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 550);
        final AppFrame af = (AppFrame) start("WorldWind GCS5", GCS5.AppFrame.class);

        Map<Integer, AnimationThread> threadList=new HashMap<>(); 
        while(true){
            //System.out.printf("click connect: %s %n",af.getClickConnectButton());
            if(af.getClickNewVehicleButton()){
                int id = af.getVehicleNum()-1;
                AnimationThread ath = new AnimationThread(id, af);
                threadList.put(id, ath);
                af.resetClickNewVehicleButton();
            }
            //System.out.printf("click connect: %s %n",af.getClickConnectButton());
            if(af.getClickConnectButton()){                
                int id = af.getConnectButtonID();                
                if(threadList.get(id)==null){
                    AnimationThread ath = new AnimationThread(id, af);
                    threadList.put(id, ath);
                }
               
                if(!threadList.get(id).isRunning())
                    threadList.get(id).start();
                af.resetClickConnectButton();
            }
            if(af.getClickDisconnectButton()){
                int id = af.getDisconnectButtonID();
                
                if(threadList.get(id)!=null){
                    threadList.get(id).stopThread();
                    threadList.remove(id);
                }
                af.resetClickDisconnectButton();
            }
            
            try { 
                Thread.sleep((long) (1)); //* 1000
            } catch (InterruptedException e) {
                af.xbee1.closeXbeePort();               
            } 
        }
        

    }    
}
