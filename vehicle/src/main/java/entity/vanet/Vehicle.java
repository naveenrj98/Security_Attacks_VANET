package entity.vanet;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import globals.Resources;
import globals.AttackerEnum;
import globals.Vector2D;
import globals.map.Waypoint;
import globals.BeaconDTO;
import globals.SignedDTO;
import globals.SignedBeaconDTO;
import globals.SignedBooleanDTO;
import globals.SignedCertificateDTO;

import remote.RemoteVehicleNetworkInterface;
import remote.RemoteRSUInterface;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.security.PrivateKey;
import java.security.KeyStore;

public class Vehicle {
	private String VIN;
	private Vector2D position;
	private Vector2D velocity;
	private Waypoint nextWaypoint;

	private long lastUpdateMs;
	private RemoteVehicleNetworkInterface VANET;
	private RemoteRSUInterface RSU;
	private String rsuName;
	private String nameInVANET;
	private AttackerEnum attackerType;

	private X509Certificate myCert;
	private X509Certificate caCert;
	private PrivateKey myPrKey;
	private KeyStore myKeystore;

	private Map<X509Certificate, BeaconDTO> vicinity = new HashMap<>();
	private Map<X509Certificate, Timestamp> firstEntryInVicinity = new HashMap<>();
	private Set<X509Certificate> revokedCache = new HashSet<>();

	private List<SignedBeaconDTO> savedBeacons = new ArrayList<>();

	private boolean inDanger = false;
	private Timer resetInDangerTimer = new Timer();
	class ResetInDangerTask extends TimerTask {
		@Override
		public void run() {
			inDanger = false;
		}
	}

	private Timer engineTimer = new Timer();
	private TimerTask engineTask = new TimerTask() {
		@Override
		public void run() {
			simulatePositionUpdate();
			beacon();
		}
	};

	private Timer checkRsuInRangeTimer = new Timer();
	private TimerTask checkRsuInRangeTask = new TimerTask() {
		@Override
		public void run() {
			String newRsuName = getClosestRSUName();
			if(newRsuName.equals(rsuName) == false) {
				connectToRSU(rsuName);
			}
		}
	};

//  -----------------------------------



//  ------- CONSTRUCTOR  ------------

	public Vehicle(String VIN, String certificateName, Vector2D position, Waypoint nextWaypoint, AttackerEnum attackerType) {
		this.VIN = VIN;
		this.position = position;
		this.attackerType = attackerType;

		this.nextWaypoint = nextWaypoint;
		this.velocity = new Vector2D(0, 0);
		updateVelocity();

		String certsDir = Resources.CERT_DIR+certificateName+"/";
		// Read certificate file to a certificate object
		try {
			this.myCert = (X509Certificate)Resources.readCertificateFile(certsDir+certificateName+".cer"); }
		catch (Exception e) {
			System.out.println(Resources.ERROR_MSG("Error Loading certificate: "+e.getMessage()));
			System.out.println(Resources.ERROR_MSG("Exiting. Vehicle is useless without certificate"));
			System.exit(1);
		}

		try {
			this.myKeystore = Resources.readKeystoreFile(certsDir + certificateName + ".jks", Resources.STORE_PASS);
			this.myPrKey = Resources.getPrivateKeyFromKeystore(this.myKeystore, certificateName, Resources.KEY_PASS); }
		catch (Exception e) {
			System.out.println(Resources.ERROR_MSG("Error Loading PrivateKey: "+e.getMessage()));
			System.out.println(Resources.ERROR_MSG("Exiting. Vehicle is useless without PrivateKey"));
			System.exit(1);
		}

		try {
			this.caCert = (X509Certificate)Resources.getCertificateFromKeystore(this.myKeystore, Resources.CA_NAME); }
		catch (Exception e) {
			System.out.println(Resources.WARNING_MSG("Failed to get CA certificate from Keystore: " + e.getMessage()));
			System.out.println(Resources.ERROR_MSG("Exiting. Vehicle cannot authenticate messages without CACert"));
			System.exit(1);
		}
	}

//  ------- GETTERS  ------------

	public Vector2D getPosition() { return this.position; }
	public Vector2D getVelocity() { return this.velocity; }
	public X509Certificate getCertificate() { return this.myCert; }
	public X509Certificate getCACertificate() { return this.caCert; }
	public PrivateKey getPrivateKey() { return this.myPrKey; }
	public KeyStore getKeystore() { return this.myKeystore; }

	public void updateVelocity() {
		velocity.x = position.x - nextWaypoint.getPosition().x;
		velocity.y = position.y - nextWaypoint.getPosition().y;
		velocity.scale(20);
		//System.out.println("Updating velocity!! New velocity: " + velocity);
	}

	// Sets VANET, starts updating position and starts beaoning
	public void start(RemoteVehicleNetworkInterface VANET, String name) {
		this.VANET = VANET;
		this.nameInVANET = name;

		this.rsuName = getClosestRSUName();
		connectToRSU(rsuName);

		// Run the engine and beaconing on a timer
		lastUpdateMs = System.currentTimeMillis();
		engineTimer.scheduleAtFixedRate(engineTask, 2000, Resources.BEACON_INTERVAL);

		// Run closest rsu lookup
		checkRsuInRangeTimer.scheduleAtFixedRate(checkRsuInRangeTask, 2000, Resources.CHECK_RSU_RANGE_INTERVAL);
	}

//  ------- MAIN METHODS ------------

	public void beacon() {
		if(VANET == null || RSU == null) return;

		SignedBeaconDTO dto = null;
		if (attackerType == AttackerEnum.BAD_POSITIONS) {
			System.out.println("Generating atacker bad position.");
			Random randomgen = new Random();
			Vector2D p = new Vector2D(randomgen.nextDouble()*Resources.ATACKER_POSITION_RANGE, randomgen.nextDouble()*Resources.ATACKER_POSITION_RANGE);
			Vector2D v = new Vector2D(randomgen.nextDouble()*Resources.ATACKER_POSITION_RANGE, randomgen.nextDouble()*Resources.ATACKER_POSITION_RANGE);
			dto = new SignedBeaconDTO(p, v, this.myCert, this.myPrKey);
		} else if (attackerType == AttackerEnum.BAD_TIMESTAMPS) {
			System.out.println("Generating atacker bad Timestamp.");
			// replay attack
			for (SignedBeaconDTO b : this.savedBeacons)
				if ( ! Resources.timestampInRange(b.getTimestamp(), Resources.FRESHNESS_MAX_TIME) ) {
					dto = b; break;
				}
			dto = (dto == null ? new SignedBeaconDTO(this.position, this.velocity, this.myCert, this.myPrKey) : dto);
		} else if (attackerType == AttackerEnum.BAD_SIGNATURES) {
			dto = new SignedBeaconDTO(this.position, this.velocity, this.myCert, this.myPrKey);
			if (!this.savedBeacons.isEmpty()) {
				System.out.println("Generating atacker bad Signature.");
				byte[] sig = this.savedBeacons.get(0).getSignature(); // use another invalid signature
				dto.setSignature(sig);
			}
		} else {
			dto = new SignedBeaconDTO(this.position, this.velocity, this.myCert, this.myPrKey);
		}

		try {
			VANET.simulateBeaconBroadcast(nameInVANET, dto);
		} catch(Exception e) {
			System.out.println(Resources.ERROR_MSG("Unable to beacon message. Cause: " + e));
			System.out.println(Resources.ERROR_MSG("VANET seems dead... Exiting..."));
			System.exit(-1);
		}
	}

	public void simulatePositionUpdate() {
		long currentMs = System.currentTimeMillis();
		long deltaMs = lastUpdateMs - currentMs;
		double deltaSeconds = deltaMs / 1000.0d;

		if(inDanger == false) {
			position.update(velocity, deltaSeconds);
		}
		lastUpdateMs = currentMs;

		if(position.inRange(nextWaypoint.getPosition(), 20)) {
			position.x = nextWaypoint.getPosition().x;
			position.y = nextWaypoint.getPosition().y;
			nextWaypoint = nextWaypoint.getRandomAdjancie();
			updateVelocity();
		}
	}

	public void simulateBrain(SignedBeaconDTO dto) {
		X509Certificate beaconCert = dto.getSenderCertificate();
		BeaconDTO oldBeacon = vicinity.get(beaconCert);
		BeaconDTO newBeacon = dto.beaconDTO();

		// Save last 20 beacons for replay attacks
		if (this.savedBeacons.size() > Resources.HELD_BEACONS_FOR_REPLAY_ATTACKS) this.savedBeacons.remove(0);
		this.savedBeacons.add(dto);

		// Check if received position is dangerous
		if(isVehicleDangerous(newBeacon) == true) {
			if(inDanger = true) {
				resetInDangerTimer.cancel();
			}
			inDanger = true;
			resetInDangerTimer = new Timer();
			resetInDangerTimer.schedule(new ResetInDangerTask(), Resources.DANGER_RESET_INTERVAL);
		}

		// Data trust
		if(oldBeacon != null) {
			if(isDataTrustworthy(oldBeacon, newBeacon) == false) {
				SignedCertificateDTO certToRevoke = new SignedCertificateDTO(beaconCert, this.getCertificate(), this.getPrivateKey());
				try {
					SignedBooleanDTO response = RSU.tryRevoke(certToRevoke);
					if (! response.getValue()) // only log unsucessful attemps
						System.out.println(Resources.WARNING_MSG("Reporting vehicle beaconing unplausible data!"));
				} catch(RemoteException e) {
					System.out.println(Resources.ERROR_MSG("RSU seems dead... Cause: " + e.getMessage() + ". Exiting..."));
					System.exit(-1);
				}

			}
		}

		updateVicinity(beaconCert, dto.beaconDTO());
	}

	private boolean isVehicleDangerous(BeaconDTO otherBeacon) {
		// TODO: Probably we should use some more complex model. Use velocity and such to prevent false positives.
		double predictedDelta = (System.currentTimeMillis() - otherBeacon.getTimestamp().getTime()) / 1000.0d;
		Vector2D predictedPosition = otherBeacon.getPosition().predictedNext(otherBeacon.getVelocity(), predictedDelta);
//		System.out.println(otherBeacon.getPosition());
//		System.out.println(predictedPosition);
		if (position.inRange(predictedPosition, Resources.TOO_DANGEROUS_RANGE)) {
			System.out.println(Resources.WARNING_MSG("Proximity Alert!!"));
			return true;
		}
		return false;
	}

	private boolean isDataTrustworthy(BeaconDTO oldBeacon, BeaconDTO newBeacon) {
		double predictedDelta = (newBeacon.getTimestamp().getTime() - oldBeacon.getTimestamp().getTime()) / 1000.0d;
		Vector2D predictedPosition = oldBeacon.getPosition().predictedNext(oldBeacon.getVelocity(), predictedDelta);
		return predictedPosition.inRange(newBeacon.getPosition(), Resources.ACCEPTABLE_DATA_TRUST_VARIANCE);
	}

	public boolean isRevoked(SignedDTO beacon) {
		X509Certificate senderCert = beacon.getSenderCertificate();
		if(revokedCacheContain(senderCert) == true)
			return true;

		SignedCertificateDTO certToCheck = new SignedCertificateDTO(senderCert, this.getCertificate(), this.getPrivateKey());

		try {
			SignedBooleanDTO rev = RSU.isRevoked(certToCheck);
			if (!this.authenticateSender(rev)) {
				System.out.println(Resources.WARNING_MSG("Cannot trust RSU."));
				return false;
			}

			if(rev.getValue()) {
				addRevokedCertToCache(senderCert);
				return true;
			}
		} catch(RemoteException e) {
			System.out.println(Resources.ERROR_MSG("Unable to contact RSU. Cause: " + e));
			System.out.println(Resources.ERROR_MSG("Exiting..."));
			System.exit(-1);
		}

		return false;
	}

	private boolean authenticateSender(SignedBooleanDTO dto) {

		// verify if certificate was signed by CA
		if (!dto.verifyCertificate(this.getCACertificate())) {
			System.out.println(Resources.WARNING_MSG("Invalid CA Signature on RSU reply: " + dto));
			return false;  // certificate was not signed by CA, isRevoked  request is dropped
		}

		// verify if certificate has expired
		try { dto.getSenderCertificate().checkValidity();
		} catch (CertificateExpiredException e) {
			System.out.println(Resources.WARNING_MSG("Sender's Certificate has expired: " + dto));
			return false;  // certificate has expired, isRevoked  request is dropped

		} catch (CertificateNotYetValidException e) {
			System.out.println(Resources.WARNING_MSG("Sender's Certificate is not yet valid: " + dto));
			return false;  // certificate was not yet valid, isRevoked  request is dropped
		}

		// verify signature sent
		if (!dto.verifySignature()) {
			System.out.println(Resources.WARNING_MSG("Invalid digital signature on isRevoked request: " + dto));
			return false;  // certificate was not signed by sender
		}

		return true; // Sender is authenticated
	}

	void reportCertificate(SignedCertificateDTO certToRevoke) {
		try {
			RSU.tryRevoke(certToRevoke);
		} catch (RemoteException e) {
			System.out.println(Resources.ERROR_MSG("Unable to contact RSU. Cause: " + e));
			System.out.println(Resources.ERROR_MSG("Exiting..."));
			System.exit(-1);
		}
	}


	// -------------------------------
	// --- REVOKED CACHE FUNCTIONS ---
	// -------------------------------
	public boolean revokedCacheContain(X509Certificate cert) {
		return revokedCache.contains(cert);
	}

	public void addRevokedCertToCache(X509Certificate cert) {
		revokedCache.add(cert);
	}



	// --------------------------
	// --- VICINITY FUNCTIONS ---
	// --------------------------
	public boolean vicinityContains(X509Certificate cert) {
		// TODO: This only removes from the cache if it is ever searched again, leaving trash in the cache, but oh well
		BeaconDTO beacon = vicinity.get(cert);
		if(beacon == null)
			return false;

		Timestamp ts = firstEntryInVicinity.get(cert);

		if(!Resources.timestampInRange(ts, Resources.MAX_INTERVAL_VICINITY_IN_CACHE)) {
			removeFromVicinity(cert);
			return false;
		}
		return true;
	}

	public void updateVicinity(X509Certificate cert, BeaconDTO beacon) {
		if(!vicinityContains(cert)) {
			System.out.println(Resources.NOTIFY_MSG("Adding a vehicle to the vicinity."));
			vicinity.put(cert, beacon);
			firstEntryInVicinity.put(cert, beacon.getTimestamp());

		} else {
			vicinity.replace(cert, beacon);
		}
	}

	public void removeFromVicinity(X509Certificate cert) {
		vicinity.remove(cert);
		firstEntryInVicinity.remove(cert);
	}

	// -------------------------
	// --- UTILITY FUNCTIONS ---
	// -------------------------
	@Override
	public String toString() {
		String res;
		res = "Vehicle: <id>=" + VIN + "; ";
		res += "<pos>=(" + this.position.x + ", " + this.position.y + "); ";
		res += "<vel>=(" + this.velocity.x + ", " + this.velocity.y + ");";
		return res;
	}

	public void connectToRSU(String rsuName) {
		// Connect to the RSU
		try {
			System.out.println(Resources.OK_MSG("Connecting to rsu: " + rsuName));
			Registry registry = LocateRegistry.getRegistry(Resources.REGISTRY_PORT);
			RSU = (RemoteRSUInterface) registry.lookup(rsuName);
			this.rsuName = rsuName;
		} catch(Exception e) {
			System.err.println(Resources.ERROR_MSG("Failed to connect to RSU called: " +  e.getMessage()));
			System.exit(-1);
			return;
		}
	}

	private String getClosestRSUName() {
		// Get RSU name
		String newRsuName = null;
		try {
			newRsuName = VANET.getNearestRSUName(position);

			if(newRsuName == null)
				System.err.println(Resources.ERROR_MSG("Failed to find an RSU in range."));

		} catch(RemoteException e) {
			// VANET is Dead
			System.err.println(Resources.ERROR_MSG("Failed to connect to VANET: " +  e.getMessage()));
			System.exit(-1);
		}
		return newRsuName;
	}
}
