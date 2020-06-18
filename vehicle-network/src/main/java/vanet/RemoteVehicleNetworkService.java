package vanet;

import remote.RemoteVehicleInterface;
import remote.RemoteVehicleNetworkInterface;

import globals.Vector2D;
import globals.Resources;
import globals.AttackerEnum;
import globals.SignedBeaconDTO;
import globals.SignedCertificateDTO;

import java.util.Map;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RemoteVehicleNetworkService implements RemoteVehicleNetworkInterface {
	private boolean isPublished = false;
	private long nextVehicleNumber = 0;
	private VehicleNetwork vehicleNetwork;

	public RemoteVehicleNetworkService(VehicleNetwork vehicleNetwork) {
		this.vehicleNetwork = vehicleNetwork;
	}

// ------ INTERFACE METHODS --------

	@Override
	public void simulateBeaconBroadcast(String name, SignedBeaconDTO beacon) throws RemoteException {
		if(!vehicleNetwork.hasVehicle(name)) {
			System.out.println(Resources.WARNING_MSG("Vehicle \"" + name + "\" tried to beacon but is not in the network."));
			return;
		}

		Vector2D sendingVehiclePos = vehicleNetwork.getVehiclePos(name);

		for(Map.Entry<String, RemoteVehicleInterface> entry: vehicleNetwork.getVehicleEntrySet()) {
			if(entry.getKey().equals(name)) continue;

			RemoteVehicleInterface remoteVehicle = entry.getValue();
			try {
				Vector2D remoteVehiclePos = vehicleNetwork.getVehiclePos(entry.getKey());

				if(VehicleNetwork.inRangeForBeacon(sendingVehiclePos, remoteVehiclePos)) {
					remoteVehicle.receiveBeaconMessage(beacon);
				}
			} catch(RemoteException e) {
				System.out.println(Resources.WARNING_MSG("Vehicle \"" + entry.getKey() + "\" seems to be dead. (Exception: " + e.getMessage()));
				vehicleNetwork.removeVehicle(entry.getKey());
			}
		}
	}

	@Override
	public boolean addVehicle(String name, AttackerEnum attackerType) throws RemoteException {
		if(vehicleNetwork.hasVehicle(name))
			return false;

		RemoteVehicleInterface vehicleToAdd;
		Vector2D pos;
		try {
			Registry registry = LocateRegistry.getRegistry(Resources.REGISTRY_PORT); // @FIXME: only works for localhost
			vehicleToAdd = (RemoteVehicleInterface) registry.lookup(name);
			pos = vehicleToAdd.simulateGetPosition();
		} catch(Exception e) {
			System.err.println(Resources.ERROR_MSG("Failed to add vehicle \"" + name + "\" : " + e.getMessage()));
			return false;
		}

		vehicleNetwork.addVehicle(name, vehicleToAdd, pos, attackerType);
		return true;
	}

	@Override
	public boolean removeVehicle(String name) throws RemoteException {
		if(vehicleNetwork.hasVehicle(name)) {
			vehicleNetwork.removeVehicle(name);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public synchronized String getNextVehicleName() {
		return "V" + nextVehicleNumber++; // to match certificate names
	}

	@Override
	public void informVehiclesOfRevocation(SignedCertificateDTO dto, Vector2D rsuPosition) throws RemoteException {
		for(Map.Entry<String, RemoteVehicleInterface> entry: vehicleNetwork.getVehicleEntrySet()) {
			String vehicleName = entry.getKey();
			RemoteVehicleInterface remoteVehicle = entry.getValue();

			try {
				Vector2D remoteVehiclePos = vehicleNetwork.getVehiclePos(vehicleName);
				if(VehicleNetwork.inRangeForRsu(rsuPosition, remoteVehiclePos)) {
					remoteVehicle.addRevokedCertificate(dto); // Each vehicle adds the revoked certificate
				}
			} catch(RemoteException e) {
				System.out.println(Resources.WARNING_MSG("Vehicle \"" + entry.getKey() + "\" seems to be dead. (Exception: " + e.getMessage()));
				vehicleNetwork.removeVehicle(entry.getKey());
			}
		}
	}

	@Override
	public boolean addRSU(Vector2D rsu_position, String rsu_name) throws RemoteException {
		return this.vehicleNetwork.addRSU(rsu_position, rsu_name);
	}

	@Override
	public boolean removeRSU(String name) throws RemoteException {
		return this.vehicleNetwork.removeRSU(name);
	}

	@Override
	public String getNearestRSUName(Vector2D vehiclePosition) throws RemoteException {
		return this.vehicleNetwork.getNearestRSUName(vehiclePosition);
	}


// ------ REGISTRY METHODS --------

	public void publish() {
		if(isPublished) {
			System.out.println(Resources.WARNING_MSG(Resources.VANET_NAME+" already published."));
			return;
		}

		try {
			RemoteVehicleNetworkInterface stub = (RemoteVehicleNetworkInterface) UnicastRemoteObject.exportObject(this, 0);
			Registry registry;
			registry = LocateRegistry.getRegistry(Resources.REGISTRY_PORT);
			registry.rebind(Resources.VANET_NAME, stub);
			isPublished = true;
			System.out.println(Resources.OK_MSG(Resources.VANET_NAME+" published to registry."));
		} catch (Exception e) {
			System.err.println(Resources.ERROR_MSG("Failed to publish remote VANET: " + e.getMessage()));
		}
	}

	public void unpublish() {
		if(! isPublished) {
			System.out.println(Resources.WARNING_MSG("Unpublishing "+Resources.VANET_NAME+" that was never published."));
			return;
		}

		try {
			Registry registry = LocateRegistry.getRegistry(Resources.REGISTRY_PORT);
			registry.unbind(Resources.VANET_NAME);
			UnicastRemoteObject.unexportObject(this, true);
		} catch (Exception e) {
			System.err.println(Resources.ERROR_MSG("Unpublishing VANET: " + e.getMessage()));
		}
	}
}
