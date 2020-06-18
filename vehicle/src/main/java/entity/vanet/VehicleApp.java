package entity.vanet;

import globals.Resources;
import globals.AttackerEnum;
import remote.RemoteVehicleNetworkInterface;
import globals.Vector2D;
import globals.map.DefaultMap;
import globals.map.Waypoint;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Hello world!
 *
 */
public class VehicleApp {
	public static final String VEHICLE_ARGS_USAGE = "vehicle <VIN> <cert_name> <posX,posY> <velX,velY> <attacker_type>";

	/**
	 * This works as follows:
	 *
	 * - Read arguments and store them, if none supplied use defaults
	 * - Create vehicle with set parameters (args or defaults)
	 * - Register this Vehicle in the RMI register
	 * - Connect to the VANET getting the vehicle unique name
	 * - Connect to the RSU
	 * - Create a RemoteVehicleService and publish its interface to RMI
	 * - Wait loop
	 */
	public static void main(String[] args) {

		System.out.println("\n");

		// Vehicle creation arguments
		Vector2D pos = null;
		Vector2D vel = new Vector2D(1, 0);
		String vin = "VIN1"; // TODO GENERATE ONE RANDOM MAYBE?
		String simulated_certName = "vehicle1"; // TODO select a diferent for each one
		AttackerEnum attacker = AttackerEnum.NO_ATTACKER;

		Vehicle vehicle;

		// Parse args
		if (args.length == 0)
			System.out.println(Resources.NOTIFY_MSG( "Assuming random values for position, velocity and VIN.\n\tTo specify this values you could call with: " + VEHICLE_ARGS_USAGE + "."));
		else if(args.length == 5) {
			try {
				String [] pos_args = args[2].split(",");
				String [] vel_args = args[3].split(",");
				pos = new Vector2D(Float.parseFloat(pos_args[0]), Float.parseFloat(pos_args[1]));
				vel = new Vector2D(Float.parseFloat(vel_args[0]), Float.parseFloat(vel_args[1]));
				vin = args[0];
				simulated_certName = args[1];

				switch(args[4]) {
					case "BAD_POSITIONS":
						 attacker = AttackerEnum.BAD_POSITIONS;
						 break;
    				case "BAD_SIGNATURES":
						 attacker = AttackerEnum.BAD_SIGNATURES;
						 break;
    				case "BAD_CERTIFICATE":
						 attacker = AttackerEnum.BAD_CERTIFICATE;
 						 break;
   					case "BAD_TIMESTAMPS":
						 attacker = AttackerEnum.BAD_TIMESTAMPS;
						 break;
    				case "BEACON_DOS":
						 attacker = AttackerEnum.BEACON_DOS;
						 break;
					default:
						attacker = AttackerEnum.NO_ATTACKER;
				}

			} catch (NumberFormatException e) {
				System.out.println( Resources.ERROR_MSG("Received the correct amount of arguments but couldn't convert to float."));
				return;
			}
		} else {
			System.out.println(Resources.ERROR_MSG("Incorrect amount of arguments. Expecting 4 but received" + args.length));
			System.out.println(Resources.NOTIFY_MSG("Usage: " + VEHICLE_ARGS_USAGE));
			return;
		}

		Waypoint closestWaypoint;
		if (pos == null) {
			closestWaypoint = DefaultMap.getInstance().getRandomWaypoint();
		} else {
			closestWaypoint = DefaultMap.getInstance().getClosestWaypoint(pos);
		}
		vehicle = new Vehicle(vin, simulated_certName, closestWaypoint.getPosition(), closestWaypoint.getRandomAdjancie(), attacker);

		System.out.println(Resources.OK_MSG("Started: " + vehicle));

		// Create registry if it doesn't exist
		try {
			LocateRegistry.createRegistry(Resources.REGISTRY_PORT);
		} catch(Exception e) {
			// registry is already created
		}

		// Connect to the VANET
		RemoteVehicleNetworkInterface VANET;
		String vehicleUniqueName;
		try {
			Registry registry = LocateRegistry.getRegistry(Resources.REGISTRY_PORT);
			VANET = (RemoteVehicleNetworkInterface) registry.lookup(Resources.VANET_NAME);
			// Get a unique name from the VANET
			vehicleUniqueName = VANET.getNextVehicleName();
		} catch(Exception e) {
			System.err.println(Resources.ERROR_MSG("Failed to connect to VANET: " +  e.getMessage()));
			System.exit(-1); // Return seems to not work for some reason
			return;
		}

		// Publish remote vehicle
		RemoteVehicleService remoteVehicle = new RemoteVehicleService(vehicle, vehicleUniqueName);
		remoteVehicle.publish();

		// Start the vehicle
		vehicle.start(VANET, vehicleUniqueName);

		// Add vehicle to the VANET
		try {
			boolean result = VANET.addVehicle(vehicleUniqueName, attacker);
			if(result == false) {
				throw new Exception("Remote call to the VANET to add this vehicle failed.");
			}
		} catch(Exception e) {
			System.err.println(Resources.ERROR_MSG("Failed add vehicle to the VANET: " +  e.getMessage()));
			System.exit(0); // Return seems to not work for some reason
			return;
		}

		// Handle wait and removal
		try {
			System.out.println("Press enter to kill the vehicle.");
			System.in.read();
			VANET.removeVehicle(vehicleUniqueName);
		} catch (java.io.IOException e) {
			System.out.println(Resources.ERROR_MSG("Unable to read from input. Exiting."));
		} finally {
			remoteVehicle.unpublish();
			System.out.println("\n");
			System.exit(0);
		}
	}
}
