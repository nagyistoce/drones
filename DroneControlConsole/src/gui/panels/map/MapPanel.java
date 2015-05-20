package gui.panels.map;

import gui.panels.UpdatePanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.Layer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.OsmFileCacheTileLoader;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.Style;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.interfaces.MapObject;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.MapQuestOsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;
import threads.UpdateThread;
import commoninterface.entities.Entity;
import commoninterface.entities.GeoEntity;
import commoninterface.entities.GeoFence;
import commoninterface.entities.RobotLocation;
import commoninterface.entities.Waypoint;
import commoninterface.network.messages.EntityMessage;
import commoninterface.utils.jcoord.LatLon;

public class MapPanel extends UpdatePanel {
	
	private static final long serialVersionUID = 1L;

    private JMapViewerTreeDrone treeMap = null;

    private JButton fitMarkersButton;
    
    private static int POSITION_HISTORY = 10;
    
    private int robotMarkerIndex = 0;
    
    private HashMap<String,LinkedList<MapMarker>> robotPositions = new HashMap<String, LinkedList<MapMarker>>();
    private LinkedList<MapMarker> waypointMarkers = new LinkedList<MapMarker>();
    
    private LinkedList<Waypoint> waypoints = new LinkedList<Waypoint>();
    private UpdateThread thread = null;
    
    private GeoFence geoFence = new GeoFence("geofence");
    private Layer geoFenceLayer;
    private boolean editingGeoFence = false;

    /**
     * Constructs the {@code Demo}.
     * @throws MalformedURLException 
     */
    public MapPanel() {

        treeMap = new JMapViewerTreeDrone("Zones");
        
        // final JMapViewer map = new JMapViewer(new MemoryTileCache(),4);
        // map.setTileLoader(new OsmFileCacheTileLoader(map));
        // new DefaultMapController(map);

        setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        JPanel panelTop = new JPanel();
        JPanel helpPanel = new JPanel();

        add(panel, BorderLayout.NORTH);
        add(helpPanel, BorderLayout.SOUTH);
        panel.setLayout(new BorderLayout());
        panel.add(panelTop, BorderLayout.NORTH);
        JLabel helpLabel = new JLabel("Left mouse: move // Double left click or mouse wheel: zoom // Right click: add markers");
        helpPanel.add(helpLabel);
        fitMarkersButton = new JButton("Fit Markers");
        fitMarkersButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                map().setDisplayToFitMapMarkers();
            }
        });
        
        try {
        
	        JComboBox<TileSource> tileSourceSelector = new JComboBox<>(new TileSource[] { new OfflineOsmTileSource((new File("tiles").toURI().toURL()).toString(),1,19) ,
	        		new OsmTileSource.Mapnik(), new BingAerialTileSource(), new MapQuestOsmTileSource()
	        });
	        tileSourceSelector.addItemListener(new ItemListener() {
	            public void itemStateChanged(ItemEvent e) {
	                map().setTileSource((TileSource) e.getItem());
	            }
	        });
	        
	        map().setTileSource(tileSourceSelector.getItemAt(0));
	        
	        panelTop.add(tileSourceSelector);
	        
	        try {
	        	map().setTileLoader(new OsmFileCacheTileLoader(map()));
	        } catch (IOException e) {
	        	map().setTileLoader(new OsmTileLoader(map()));
	        }
        
        } catch(MalformedURLException e) {
        	e.printStackTrace();
        }
        
        final JCheckBox showTreeLayers = new JCheckBox("Tree Layers");
        showTreeLayers.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                treeMap.setTreeVisible(showTreeLayers.isSelected());
            }
        });
        
        JButton clearWaypointsButton = new JButton("Clear Waypoints");
        clearWaypointsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearWaypoints();
			}
		});
        
        JButton geoFenceButton = new JButton(" Add GeoFence ");
        geoFenceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!editingGeoFence) {
					editingGeoFence = true;
					clearGeoFence();
					geoFenceButton.setText("Finish GeoFence");
				} else {
					editingGeoFence = false;
					geoFenceButton.setText(" Add GeoFence ");
				}
			}
		});
        
        panelTop.add(showTreeLayers);
        panelTop.add(fitMarkersButton);
        panelTop.add(geoFenceButton);
        panelTop.add(clearWaypointsButton);
        add(treeMap, BorderLayout.CENTER);
        
        geoFenceLayer = treeMap.addLayer("_GeoFence");
        
        //Lisbon
        map().setDisplayPosition(new Coordinate(38.7166700,-9.1333300), 13);

        map().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            	
               if(e.getButton() == MouseEvent.BUTTON3) {
<<<<<<< HEAD
            	   if(addingObstacle)
            		   addObstacle(map().getPosition(e.getPoint()));
            	   else if(editingGeoFence)
            		   addToGeoFence(map().getPosition(e.getPoint()));
            	   else
            		   addWaypoint(map().getPosition(e.getPoint()));
=======
            	   if(!editingGeoFence)
            		   addMarker(map().getPosition(e.getPoint()));
            	   else
            		   addToGeoFence(map().getPosition(e.getPoint()));
>>>>>>> branch 'master' of https://github.com/BioMachinesLab/drones.git
//            	   updateRobotPosition("drone", map().getPosition(e.getPoint()));
                }
            }
        });

        map().addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                boolean cursorHand = map().getAttribution().handleAttributionCursor(p);
                if (cursorHand) {
                    map().setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    map().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
    }
    
    private JMapViewer map(){
        return treeMap.getViewer();
    }
    
    public static Coordinate c(double lat, double lon){
        return new Coordinate(lat, lon);
    }

    public synchronized void addWaypoint(Coordinate c) {
    	
    	String layerName = "waypoints";
    	
    	Layer l = null;
    	
    	for(Layer layer : treeMap.getLayers())
    		if(layer.getName().equals("waypoints"))
    			l = layer;

    	if(l == null) {
    		l = treeMap.addLayer(layerName);
    	}
    	
    	String markerName = "waypoint"+waypoints.size();
    	
    	MapMarker m = new MapMarkerWaypoint(l, markerName , c);
    	
    	l.add(m);
    	waypointMarkers.add(m);
    	
    	map().addMapMarker(m);
    	
    	synchronized(this) {
    		Waypoint waypoint = new Waypoint(markerName, new LatLon(c.getLat(),c.getLon()));
    		waypoints.add(waypoint);
//    		notifyAll();
    	}
    }
    
    public synchronized void updateRobotPosition(RobotLocation di) {
    	
    	LatLon latLon = di.getLatLon();
    	
		double lat = latLon.getLat();
		double lon = latLon.getLon();
		
		if(lat == 0 && lon == 0)
			return;
		
		double orientation = di.getOrientation();
		
		String name = di.getName().isEmpty() ? "drone" : di.getName();
    	
    	Layer l = null;
    	
    	LinkedList<MapMarker> robotMarkers = robotPositions.get(name);
    	
    	if(robotMarkers == null) {
    		robotMarkers = new LinkedList<MapMarker>();
    		robotPositions.put(name, robotMarkers);
    	}
    	
    	
    	Iterator<MapMarker> i = map().getMapMarkerList().iterator();
    	
    	while(i.hasNext()) {
    		MapMarker m = i.next();
    		if(m.getLayer() != null && m.getLayer().getName().equals(name)) {
    			l = m.getLayer();
    			break;
    		}
    	}

    	if(l == null) {
    		l = treeMap.addLayer(name);
    	}
    	
    	if(!robotMarkers.isEmpty()) {
    	
	    	Style styleOld = new Style(Color.black, Color.LIGHT_GRAY, new BasicStroke(1), new Font("Dialog", Font.PLAIN, 0));
	    	
	    	//remove last value from previous iteration
	    	MapMarker last = robotMarkers.pollLast();
	    	treeMap.removeFromLayer(last);
	    	map().removeMapMarker(last);
	    	
	    	//add that same one with a different style
	    	MapMarker old = new MapMarkerDot(l,""+robotMarkerIndex++,last.getCoordinate(), styleOld);
	    	robotMarkers.add(old);
	    	l.add(old);
	    	map().addMapMarker(old);
	    	
    	}
    	
    	Style styleNew = null;
    	
    	switch(di.getDroneType()) {
    		case DRONE:
    			styleNew = new Style(Color.RED, Color.green, new BasicStroke(1), new Font("Dialog", Font.PLAIN, 12));
    			break;
    		case ENEMY:
    			styleNew = new Style(Color.RED, Color.RED, new BasicStroke(1), new Font("Dialog", Font.PLAIN, 12));
    			break;
    		default:
    			styleNew = new Style(Color.RED, Color.green, new BasicStroke(1), new Font("Dialog", Font.PLAIN, 12));
    	}
    	
    	MapMarker m = new MapMarkerDrone(l, name , c(lat,lon), styleNew, orientation);
    	l.add(m);
    	map().addMapMarker(m);
    	robotMarkers.add(m);
    	
    	while(robotMarkers.size() > POSITION_HISTORY) {
    		MapMarker old = robotMarkers.pollFirst();
    		treeMap.removeFromLayer(old);
        	map().removeMapMarker(old);
    	}
    	
    	if(robotMarkers.size() == 1 && robotPositions.size() == 1) {
//    		map().setDisplayToFitMapMarkers();
    	}
    }
    
    public synchronized void displayData(RobotLocation di) {
		
    	LatLon latLon = di.getLatLon();
    	
		double lat = latLon.getLat();
		double lon = latLon.getLon();
		
		if(lat == 0 && lon == 0)
			return;
		
		if(usefulRobotCoordinate(di.getName(), c(lat,lon))) {
			updateRobotPosition(di);
		}
	}
	
	private synchronized boolean usefulRobotCoordinate(String name, Coordinate n) {
		
		if(n.getLat() == -1 && n.getLon() == -1)
			return false;
		
		LinkedList<MapMarker> robotMarkers = robotPositions.get(name); 
		
		if(robotMarkers == null || robotMarkers.isEmpty())
			return true;
		
		Coordinate c = robotMarkers.peekLast().getCoordinate();
		
		if(c.getLat() == n.getLat() && c.getLon() == n.getLon())
			return false;
		
		return true;
	}
	
	@Override
	public long getSleepTime() {
		return 10;
	}
	
	@Override
	public void registerThread(UpdateThread t) {
		this.thread = t;		
	}
	
	@Override
	public synchronized void threadWait() {
		while(true) {
			try {
				wait();
			} catch(Exception e){}
		}
	}

	public void displayData(EntityMessage message) {
		System.out.println("TODO MapPanel");
	}
	
	private synchronized void addToGeoFence(Coordinate coord) {
		MapMarker marker = new MapMarkerDot(coord);
		geoFence.addWaypoint(new LatLon(coord.getLat(),coord.getLon()));
		geoFenceLayer.add(marker);
		map().addMapMarker(marker);
		
		ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
		
		for(Waypoint wp : geoFence.getWaypoints())
			coords.add(new Coordinate(wp.getLatLon().getLat(), wp.getLatLon().getLon()));

		map().removeAllMapPolygons();
		MapPolygonImpl po = new MapPolygonImpl(coords);
		geoFenceLayer.add(po);
		map().addMapPolygon(po);
	}
	
	private synchronized void addGeoFence(GeoFence geo) {
		ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
		
		for(Waypoint wp : geo.getWaypoints()) {
			Coordinate coord = c(wp.getLatLon().getLat(), wp.getLatLon().getLon());
			
			MapMarker marker = new MapMarkerDot(coord);
			geoFence.addWaypoint(new LatLon(coord.getLat(),coord.getLon()));
			geoFenceLayer.add(marker);
			map().addMapMarker(marker);
			coords.add(new Coordinate(wp.getLatLon().getLat(), wp.getLatLon().getLon()));
		}

		map().removeAllMapPolygons();
		MapPolygonImpl po = new MapPolygonImpl(coords);
		geoFenceLayer.add(po);
		map().addMapPolygon(po);
	}

	
	public LinkedList<Entity> getEntities() {
		LinkedList<Entity> entities = new LinkedList<>();

		if(!geoFence.getWaypoints().isEmpty()) {
			entities.add(geoFence);
		}
		
		if(waypoints != null)
			entities.addAll(waypoints);
		
		return entities;
	}
	
	private void addObstacle(Coordinate c) {
		String layerName = "obstacles";
    	
    	Layer l = null;
    	
    	for(Layer layer : treeMap.getLayers())
    		if(layer.getName().equals("obstacles"))
    			l = layer;

    	if(l == null) {
    		l = treeMap.addLayer(layerName);
    	}
    	
    	String markerName = "obstacle"+obstacles.size();
    	
    	MapMarker m = new MapMarkerObstacle(l, markerName , c);
    	
    	l.add(m);
    	obstacleMarkers.add(m);
    	
    	map().addMapMarker(m);
    	
    	synchronized(this) {
    		ObstacleLocation ol = new ObstacleLocation(markerName, new LatLon(c.getLat(),c.getLon()),OBSTACLE_RADIUS);
    		obstacles.add(ol);
    	}
		
		addingObstacle = false;
	}
	
	private void clearObstacles() {
		
		for(MapMarker m : obstacleMarkers) {
	    	treeMap.removeFromLayer(m);
	    	map().removeMapMarker(m);
		}
    	
    	obstacles.clear();
    	obstacleMarkers.clear();
	}
	
	private synchronized void clearGeoFence() {
		
		LinkedList<MapMarker> list = new LinkedList<MapMarker>();
		
		if(geoFenceLayer.getElements() != null) {
			for(MapObject mo : geoFenceLayer.getElements()) {
				if(mo instanceof MapMarker) {
					list.add((MapMarker)mo);
					map().removeMapMarker((MapMarker)mo);
				}
			}
			
			for(MapMarker m : list)
				treeMap.removeFromLayer(m);
			
			geoFenceLayer.getElements().clear();
			geoFence.clear();
		}
		map().removeAllMapPolygons();
	}
	
	private void clearWaypoints() {
		
		for(MapMarker m : waypointMarkers) {
	    	treeMap.removeFromLayer(m);
	    	map().removeMapMarker(m);
		}
    	
    	waypoints.clear();
    	waypointMarkers.clear();
	}
	
	public void clearHistory() {
		
		for(String s : robotPositions.keySet()) {
		
			LinkedList<MapMarker> robotMarkers = robotPositions.get(s);
		
			while(!robotMarkers.isEmpty()) {
	    		MapMarker old = robotMarkers.pollFirst();
	    		treeMap.removeFromLayer(old);
	        	map().removeMapMarker(old);
	    	}
		}
	}
	
	public void replaceEntities(ArrayList<Entity> entities) {
		clearObstacles();
		clearGeoFence();
		clearWaypoints();
		
		for(Entity e : entities) {
			if(e instanceof GeoEntity) {
				GeoEntity ge = (GeoEntity)e;
				if(ge instanceof Waypoint)
					addWaypoint(c(ge.getLatLon().getLat(),ge.getLatLon().getLon()));
				if(ge instanceof ObstacleLocation)
					addObstacle(c(ge.getLatLon().getLat(),ge.getLatLon().getLon()));
			}
			if(e instanceof GeoFence)
				addGeoFence((GeoFence)e);
		}
	}
}
