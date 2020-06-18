package vanet;

import globals.AttackerEnum;
import globals.Resources;
import globals.Vector2D;

import remote.RemoteVehicleInterface;
import vanet.gui.VehicleActor;
import remote.RemoteRSUInterface;

import com.badlogic.gdx.Gdx;


import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.TreeMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

import java.util.Timer;
import java.util.TimerTask;

/*
	Simulate physical wireless network
*/
public class VehicleNetwork {
	private vanet.gui.VanetGUI GUI;

	private Map<String, RemoteVehicleInterface> vehicleList = new ConcurrentHashMap<>();
	private Map<String, VehicleActor> vehicleActorPos = new ConcurrentHashMap<>();

	private Map<String, Vector2D> rsuListPos = new TreeMap<>();
	private Map<String, RemoteRSUInterface> rsuList = new TreeMap<>();


	private Timer timer = new Timer();
	private TimerTask vehiclePosUpdaterTask = new TimerTask() {
		@Override
		public void run() {
			for (Map.Entry<String, RemoteVehicleInterface> entry : vehicleList.entrySet()) {
				try {
					Vector2D pos = entry.getValue().simulateGetPosition();
					vehicleActorPos.get(entry.getKey()).updatePosition((float)pos.x, (float)pos.y);

				} catch(Exception e) {
					System.out.println(Resources.WARNING_MSG("Unable to update position for vehicle \"" + entry.getKey() + "\"."));
				}
			}
		}
	};

	public VehicleNetwork() {
		timer.scheduleAtFixedRate(vehiclePosUpdaterTask, 0, Resources.NETWORK_POSITION_UPDATE_INTERVAL);

		// Launch GUI
        GUI = new vanet.gui.VanetGUI();
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "VANET simulation";
		config.height = 600;
		config.width = 600;
		//config.useGL30 = true;
        new LwjglApplication(GUI, config);
	}

	public Set<Map.Entry<String, RemoteVehicleInterface>> getVehicleEntrySet() {
		return vehicleList.entrySet();
	}

	public boolean hasVehicle(String name) {
		return vehicleList.containsKey(name);
	}

	public void addVehicle(String name, RemoteVehicleInterface vehicleToAdd, Vector2D position, AttackerEnum attackerType) {
		System.out.println(Resources.OK_MSG("Adding vehicle \"" + name + "\" to the network."));
		vehicleList.put(name, vehicleToAdd);

		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run(){
				VehicleActor toAdd = new VehicleActor((float)position.x, (float)position.y, name, attackerType);
				GUI.addActor(toAdd);
				vehicleActorPos.put(name, toAdd);
			}
    	});

	}

	public void removeVehicle(String name) {
		System.out.println(Resources.OK_MSG("Removing vehicle \"" + name + "\" from the network."));
		vehicleList.remove(name);

		GUI.removeActor(vehicleActorPos.get(name));
		vehicleActorPos.remove(name);
	}

	public Vector2D getVehiclePos(String name) {
		Actor a = vehicleActorPos.get(name);
		return new Vector2D(a.getX(), a.getY());
	}

	public static boolean inRangeForBeacon(Vector2D pos1, Vector2D pos2) {
		return pos1.inRange(pos2, Resources.MAX_BEACON_RANGE);
	}

	public static boolean inRangeForRsu(Vector2D pos1, Vector2D pos2) {
		return pos1.inRange(pos2, Resources.MAX_RSU_RANGE);
	}

	// package private
	boolean addRSU(Vector2D rsu_position, String rsu_name) {
		if(!hasRSU(rsu_name)) {
			rsuListPos.put(rsu_name, rsu_position);
			rsuList.put(rsu_name, getRemoteRSU(rsu_name));
			System.out.println(Resources.NOTIFY_MSG(rsu_name + " added to network;"));
			return true;
		}

		return false;
	}

	boolean removeRSU(String name) {
		if(!hasRSU(name))
			return false;

		rsuListPos.remove(name);
		rsuList.remove(name);
		System.out.println(Resources.NOTIFY_MSG(name + " removed from network;"));
		return true;
	}

	private boolean hasRSU(String rsu_name) {
		return rsuList.containsKey(rsu_name);
	}

	private RemoteRSUInterface getRemoteRSU (String rsu_name) {

		RemoteRSUInterface rsu = null;

		try {
			Registry registry = LocateRegistry.getRegistry(Resources.REGISTRY_PORT);
			rsu = (RemoteRSUInterface) registry.lookup(rsu_name);
		} catch(Exception e) {
			System.err.println(Resources.ERROR_MSG("Failed to connect to RSU: " +  e.getMessage()));
			System.exit(0); // Return seems to not work for some reason
		}

		return rsu;
	}

	public String getNearestRSUName(Vector2D vehiclePosition) {

		String rsu_name = null;
		double min_distance = Integer.MAX_VALUE;

		for(Map.Entry<String, Vector2D> entry : rsuListPos.entrySet()) {
			if(inRangeForRsu(entry.getValue(),vehiclePosition)) {

				double candidate_distance
						= entry.getValue().distance(vehiclePosition);

				if(candidate_distance < min_distance) {
					min_distance = candidate_distance;
					rsu_name = entry.getKey();
				}
			}
		}

		return rsu_name;
	}

}
