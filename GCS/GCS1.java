/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.EarthFlat;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.render.*;

import java.util.concurrent.atomic.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

/**
 *
 * @author cppuav
 */
public class GCS1 extends ApplicationTemplate{
    
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
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
             
            //New Vehicle buttons
            JButton newVehicleButton = new JButton("New Vehicle");
            AtomicInteger numOfVehicle = new AtomicInteger(0);
            newVehicleButton.addActionListener((ActionEvent actionEvent) -> {
                numOfVehicle.incrementAndGet();
                JPanel newVehicleConnectionPanel = newVehicleConnectionPanel(numOfVehicle.get());
                connetionPanel.add(newVehicleConnectionPanel);
                getControlPanel().revalidate();
                getWwd().redraw();
                
            });
            
            connetionPanel.add(newVehicleButton);
            return connetionPanel;
        }
        
        private JPanel newVehicleConnectionPanel(int numOfVehicle){
                       
            String vehicleName = "Vehicle " + numOfVehicle;
            AtomicBoolean vehicleType = new AtomicBoolean(true); //True: detection; False suppression
            
            IconLayer vehicleIconLayer = new IconLayer();
            
            // Buttons layout panel
            JPanel layoutPanel = new JPanel(new GridLayout(0, 2, 0, 0));
            layoutPanel.setBorder(
                    new CompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), new TitledBorder(vehicleName)));
  
            //Radio Buttuon
            ButtonGroup group = new ButtonGroup();
            JRadioButton button = new JRadioButton("Fire Detection", true);
            group.add(button);
            button.addActionListener((ActionEvent actionEvent) -> {
                //viewControlsLayer.setLayout(AVKey.HORIZONTAL);
                //getWwd().redraw();
                vehicleType.lazySet(true);
            });
            layoutPanel.add(button);
            button = new JRadioButton("Fire Suppression", false);
            group.add(button);
            button.addActionListener((ActionEvent actionEvent) -> {
                //viewControlsLayer.setLayout(AVKey.VERTICAL);
                //getWwd().redraw();
                vehicleType.lazySet(false);
            });
            layoutPanel.add(button);              
            
            //Connect buttons
            JButton connectButton = new JButton("Connect");
            connectButton.addActionListener((ActionEvent actionEvent) -> {
                UserFacingIcon icon = new UserFacingIcon("src/images/quadcopter.png",
                new Position(Angle.fromDegreesLatitude(34.0434), Angle.fromDegreesLongitude(-117.8126), 0));
                addVehicleIcon(vehicleIconLayer, icon, numOfVehicle, vehicleType.get());
                
                /*
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {               
                            updateIconLocation(vehicleIconLayer, icon);
                            
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });   */         
                getWwd().redraw();
            });
            layoutPanel.add(connectButton);

            //Disconnect buttons
            JButton disconnectButton = new JButton("Disconnect");
            disconnectButton.addActionListener((ActionEvent actionEvent) -> {
                removeVehicleIcon(vehicleIconLayer);
                getWwd().redraw();
            });
            layoutPanel.add(disconnectButton);
          

            return layoutPanel;
        }
        
        
        private void addVehicleIcon(IconLayer layer, UserFacingIcon icon, int numOfVehicle, boolean vehicleType){
            String vehicleName = "Vehicle " + numOfVehicle;
            
            layer.setPickEnabled(true);
            layer.setAllowBatchPicking(false);
            layer.setRegionCulling(true);
            layer.setName(vehicleName);
            layer.removeAllIcons();


            icon.setSize(new Dimension(24, 24));
            icon.setToolTipText(vehicleName);
            icon.setShowToolTip(true);

            
            if (vehicleType){//Detection Red
                BufferedImage circleRed = createBitmap(PatternFactory.PATTERN_CIRCLE, Color.RED);
                icon.setBackgroundImage(circleRed);  
            }
            else {//Suppression Yellow
                BufferedImage circleYellow = createBitmap(PatternFactory.PATTERN_CIRCLE, Color.YELLOW);
                icon.setBackgroundImage(circleYellow); 
                icon.moveTo(Position.fromDegrees(34.0433, -117.8124));
            }
              
            icon.setBackgroundScale(2);
            
            layer.addIcon(icon);   
            
            //ApplicationTemplate.insertAfterPlacenames(this.getWwd(), layer);
            this.getWwd().getModel().getLayers().add(layer);

        }
        
        private void removeVehicleIcon(IconLayer layer){
            layer.removeAllIcons();
        }
        
        private void updateIconLocation(IconLayer layer, UserFacingIcon icon){
            
            //vehicleThread vehicle = new vehicleThread(icon);
            //vehicle.start();
            //vehicle.run();
            
            
            ArrayList<Position> iconPositions = new ArrayList<Position>();
            iconPositions.add(Position.fromDegrees(34.0433, -117.8124, 0));
            iconPositions.add(Position.fromDegrees(34.0432, -117.8122, 0));
            iconPositions.add(Position.fromDegrees(34.0430, -117.8120, 0));
            iconPositions.add(Position.fromDegrees(34.0431, -117.8118, 0));
            iconPositions.add(Position.fromDegrees(34.0433, -117.8117, 0));
            iconPositions.add(Position.fromDegrees(34.0434, -117.8116, 0));
            iconPositions.add(Position.fromDegrees(34.0436, -117.8114, 0));
            iconPositions.add(Position.fromDegrees(34.0438, -117.8118, 0));
            iconPositions.add(Position.fromDegrees(34.0440, -117.8119, 0));
            iconPositions.add(Position.fromDegrees(34.0441, -117.8121, 0));

            for (Position pos:iconPositions){
                icon.moveTo(pos);
                layer.addIcon(icon);
                getWwd().redraw();
                pause(2);
            }          
            
        }
        
        public class vehicleThread implements Runnable {
            
            private UserFacingIcon icon;
            
            public vehicleThread(UserFacingIcon icon){
                this.icon = icon;
            }
            
            public void run() {
                System.out.println("Hello from a thread!");
                ArrayList<Position> iconPositions = new ArrayList<Position>();
                iconPositions.add(Position.fromDegrees(34.0433, -117.8124, 0));
                iconPositions.add(Position.fromDegrees(34.0432, -117.8122, 0));
                iconPositions.add(Position.fromDegrees(34.0430, -117.8120, 0));
                iconPositions.add(Position.fromDegrees(34.0431, -117.8118, 0));
                iconPositions.add(Position.fromDegrees(34.0433, -117.8117, 0));
                iconPositions.add(Position.fromDegrees(34.0434, -117.8116, 0));
                iconPositions.add(Position.fromDegrees(34.0436, -117.8114, 0));
                iconPositions.add(Position.fromDegrees(34.0438, -117.8118, 0));
                iconPositions.add(Position.fromDegrees(34.0440, -117.8119, 0));
                iconPositions.add(Position.fromDegrees(34.0441, -117.8121, 0));
                
                for (Position pos:iconPositions){
                    icon.moveTo(pos);
                    getWwd().redraw();
                    pause(1);
                }
                
            }

            /*
            public static void main(String args[]) {
                (new HelloThread()).start();
            }*/

        }

        // Create a blurred pattern bitmap
        private BufferedImage createBitmap(String pattern, Color color)
        {
            // Create bitmap with pattern
            BufferedImage image = PatternFactory.createPattern(pattern, new Dimension(128, 128), 0.7f,
                color, new Color(color.getRed(), color.getGreen(), color.getBlue(), 0));
            // Blur a lot to get a fuzzy edge
            image = PatternFactory.blur(image, 13);
            image = PatternFactory.blur(image, 13);
            image = PatternFactory.blur(image, 13);
            image = PatternFactory.blur(image, 13);
            return image;
        }
        
        public static void pause(double seconds) { 
            try { 
                Thread.sleep((long) (seconds * 1000)); 
            } 
            catch (InterruptedException e) {} 
        } 
    }
    
    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.GLOBE_CLASS_NAME, EarthFlat.class.getName());
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 34.0434);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -117.8126);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 550);

        ApplicationTemplate.start("WorldWind GCS", GCS1.AppFrame.class);
    }    
}
