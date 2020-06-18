package remote;

import globals.AttackerEnum;
import globals.SignedBeaconDTO;
import globals.SignedCertificateDTO;
import globals.Vector2D;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteVehicleNetworkInterface extends Remote {

	/**
	 * Simulates beacon propagation to nearby network nodes
	 * @param	String			name of the vehicle in the network
	 * @param	SignedBeaconDTO		data transfer object containing signed beacon data
	 */
	public void simulateBeaconBroadcast(String name, SignedBeaconDTO beacon) throws RemoteException;

	/**
	 * Adds a node (vehicle) to the network
	 * @param	String	vehicle name in the network
	 * @return	boolean	true if node was added sucessfully
	 */
	public boolean addVehicle(String name, AttackerEnum attackerType) throws RemoteException;

	/**
	 * Removes a node (vehicle) to the network
	 * @param	String	vehicle name in the network
	 * @return	boolean	true if node was removed sucessfully
	 */
	public boolean removeVehicle(String name) throws RemoteException;

	/**
	 * Gets the next available name in the network
	 * @return	String	next available name
	 */
	public String getNextVehicleName() throws RemoteException;

	/**
	 * Gets the nearesr available rsu_name in the network
	 * @return	String	nearest rsu name
	 */
	public String getNearestRSUName(Vector2D vehiclePosition) throws RemoteException;

	/**
	 * Adds a node (rsu) to the network
	 * @param	Vector2D			position of the rsu
	 * @param	String				rsu name in the network
	 * @return	boolean				true if node was added sucessfully
	 */
	public boolean addRSU(Vector2D rsu_position, String rsu_name) throws RemoteException;

	/**
	 * Removes a node (rsu) from the network
	 * @param	String	rsu name in the network
	 * @return	boolean	true if node was removed sucessfully
	 */
	public boolean removeRSU(String name) throws RemoteException;

	/**
	 * Propagate new revoked certificates to vehicles in range
	 * @param	SignedCertificateDTO	DTO containing certificate to be shared
	 */
	public void informVehiclesOfRevocation(SignedCertificateDTO dto, Vector2D rsu_position) throws RemoteException;

}