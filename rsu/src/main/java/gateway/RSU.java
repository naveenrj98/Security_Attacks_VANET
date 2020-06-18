package gateway;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.PrivateKey;
import java.security.KeyStore;

import globals.Resources;
import globals.Vector2D;
import globals.SignedCertificateDTO;

import remote.RemoteCAInterface;
import remote.RemoteRSUInterface;
import remote.RemoteVehicleNetworkInterface;


public class RSU {

	private ArrayList<Certificate> revokedCache;
	private Map<Vector2D, RemoteRSUInterface> rsuVacinity;
	private Vector2D position;
	private String name;

	// Security atributes
	private X509Certificate myCert;
	private X509Certificate caCert;
	private PrivateKey myPrKey;
	private KeyStore myKeystore;

	// Construtores //

	public RSU(String name, Vector2D position) {

		revokedCache = new ArrayList<Certificate>();
		rsuVacinity = new HashMap<Vector2D, RemoteRSUInterface>();
		this.position = position;
		this.name = name;
		String certificateName = Resources.RSU_NAME;

		String certsDir = Resources.CERT_DIR+certificateName+"/";

		// Read certificate file to a certificate object
		try {
			this.myCert = (X509Certificate)Resources.readCertificateFile(certsDir+certificateName+".cer"); }
		catch (Exception e) {
			System.out.println(Resources.ERROR_MSG("Error Loading certificate: "+e.getMessage()));
			System.out.println(Resources.ERROR_MSG("Exiting. RSU is useless without certificate"));
			System.exit(1);
		}
		try {
			this.myKeystore = Resources.readKeystoreFile(certsDir + certificateName + ".jks", Resources.STORE_PASS);
			this.myPrKey = Resources.getPrivateKeyFromKeystore(this.myKeystore, certificateName, Resources.KEY_PASS); }
		catch (Exception e) {
			System.out.println(Resources.ERROR_MSG("Error Loading PrivateKey: "+e.getMessage()));
			System.out.println(Resources.ERROR_MSG("Exiting. RSU is useless without PrivateKey"));
			System.exit(1);
		}
		try {
			this.caCert = (X509Certificate)Resources.getCertificateFromKeystore(this.myKeystore, Resources.CA_NAME); }
		catch (Exception e) {
			System.out.println(Resources.WARNING_MSG("Failed to get CA certificate from Keystore: " + e.getMessage()));
			System.out.println(Resources.ERROR_MSG("Exiting. RSU cannot authenticate messages without CACert"));
			System.exit(1);
		}
	}
	//////////////////

	public void shareRevoked(SignedCertificateDTO dto) {

		SignedCertificateDTO my_dto
			= new SignedCertificateDTO(dto.getCertificate(), this.getCertificate(), this.getPrivateKey());

		for(Map.Entry<Vector2D, RemoteRSUInterface> nearbyRSU : rsuVacinity.entrySet()) {
			try {
				nearbyRSU.getValue().receiveRevoked(my_dto); 
			} catch (RemoteException e) {
				System.err.println(Resources.WARNING_MSG("Nearby RSU not reached."));
			} 
		}

	}

	//////////////////

	public void addCertificateToCache(Certificate certificate) {
		if(!isCertInCache(certificate))
			revokedCache.add(certificate);
	}

	public void removeCertificateFromCache(Certificate certificate) {
		if(isCertInCache(certificate))
			revokedCache.remove(certificate);
	}

	public boolean isCertInCache(Certificate certificate) {
		return revokedCache.contains(certificate);
	}

	public void addRSU(Vector2D neighbor_position, RemoteRSUInterface neighbor_rsu) {
		if(inRange(neighbor_position))
			rsuVacinity.put(neighbor_position, neighbor_rsu);
	}

	public RemoteRSUService getRemoteRSUService() throws Exception {

            // Locate the certificate authority service
            Registry registry = LocateRegistry.getRegistry(Resources.REGISTRY_PORT);
            RemoteCAInterface ca_service = (RemoteCAInterface) registry.lookup(Resources.CA_NAME);

            // Locate the vehicle Network service
            RemoteVehicleNetworkInterface vehicle_network_service
            		= (RemoteVehicleNetworkInterface) registry.lookup(Resources.VANET_NAME);

            return new RemoteRSUService(this, ca_service, vehicle_network_service);
	}

	private boolean inRange(Vector2D pos2) {
		return this.getPosition().distance(pos2) <= Resources.MAX_RSU_RANGE;
	}

	// Getters
	public Vector2D getPosition() { return this.position; }
	public String getName() { return this.name; }
	public X509Certificate getCertificate()  { return this.myCert; }
	public X509Certificate getCACertificate() { return this.caCert; }
	public PrivateKey getPrivateKey() { return this.myPrKey; }
	public KeyStore getKeystore() { return this.myKeystore; }

	//  ------- UTILITY ------------
	@Override
	public String toString() {
		String res;
		res = this.getName() + ": ";
		res += "<pos>=(" + this.position.x + ", " + this.position.y + "); ";
		return res;
	}

}