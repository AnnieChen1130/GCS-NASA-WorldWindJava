/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gov.nasa.worldwindx.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.EarthFlat;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwindx.examples.util.ToolTipController;

import java.awt.*;
import java.util.ArrayList;

/**
 *
 * @author cppuav
 */
public class GCS extends ApplicationTemplate{
    
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
            
            IconLayer layer = new IconLayer();
            layer.setPickEnabled(true);
            layer.setAllowBatchPicking(false);
            layer.setRegionCulling(true);

            UserFacingIcon icon = new UserFacingIcon("src/images/quadcopter.png",
                new Position(Angle.fromDegreesLatitude(34.0434), Angle.fromDegreesLongitude(-117.8126), 0));
            icon.setSize(new Dimension(24, 24));
            layer.addIcon(icon);   
            
            ApplicationTemplate.insertAfterPlacenames(this.getWwd(), layer);
        }
    }
    
    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.GLOBE_CLASS_NAME, EarthFlat.class.getName());
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 34.0434);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -117.8126);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 550);

        ApplicationTemplate.start("WorldWind GCS", GCS.AppFrame.class);
    }    
}
