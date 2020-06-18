package entity.vanet;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.security.cert.X509Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;

import globals.Resources;
import globals.Vector2D;
import globals.SignedBeaconDTO;
import globals.SignedCertificateDTO;
import globals.SignedDTO;


import remote.RemoteVehicleInterface;

public class RemoteVehicleService implements RemoteVehicleInterface {
	private boolean isPublished = false;
	private String name;

	private Vehicle vehicle;

	public RemoteVehicleService(Vehicle vehicle, String name) {
		this.vehicle = vehicle;
		this.name = name;
	}

// ------ INTERFACE METHODS --------
	@Override
	public Vector2D simulateGetPosition() throws RemoteException {
		return vehicle.getPosition();
	}

	@Override
	public void receiveBeaconMessage(SignedBeaconDTO beacon) throws RemoteException {
		// TODO: Only do this security check periodically, not for every beacon
		// TODO: Maybe here or just have a function that receives a normal beaconDTO

		System.out.println(Resources.NOTIFY_MSG("Received beacon at pos: " + beacon.getPosition()));

		// Verify that sender is trustworthy
		if(!authenticateBeaconMessage(beacon))
			return;

		// Process beacon
		vehicle.simulateBrain(beacon);
	}

	@Override
	public void addRevokedCertificate(SignedCertificateDTO dto) throws RemoteException {
		// Verify that dto is from a trustworthy RSU
		if(!authenticateAddRevokedCertificate(dto)) {
			System.out.println(Resources.WARNING_MSG("Received a request to add a certificate to the revocation list, but it wasn't properly authenticated."));
			return;
		}

		vehicle.addRevokedCertToCache(dto.getCertificate());
	}

	//------------------------
	//--- INTERNAL METHODS ---
	//------------------------
	private boolean authenticateBeaconMessage(SignedBeaconDTO dto) {
		// If we have it in the cache:
		//    - We DO NOT need to check if the certificate is signed by the CA or if it is revoked
		//    - We DO need to confirm the expiration date and if the message signature is correct
		X509Certificate senderCert = dto.getSenderCertificate();
		if(!vehicle.vicinityContains(senderCert)) {
			if(!authenticateSenderCert(dto))
				return false;

			vehicle.updateVicinity(senderCert, dto.beaconDTO());
		}

		return authenticateSenderSignature(dto);
	}

	// Authenticate a message received from de RSU
	private boolean authenticateAddRevokedCertificate(SignedDTO dto) {
		if(!authenticateSenderCert(dto))
			return false;
		return authenticateSenderSignature(dto);
	}

	/*
	 * Verifies if certificate was signed by the CA
	 * Verifies if its not revoked (cached or contact CA through rsu)
	*/
	private boolean authenticateSenderCert(SignedDTO dto) {

		// verify Timestamp freshness
		if(!dto.verifyFreshness(Resources.FRESHNESS_MAX_TIME)) {
			System.out.println(Resources.WARNING_MSG("Sender's communication is not fresh: " + dto));
			return false;  // beacon is not fresh, beacon is dropped
		}

		// Verify if certificate was signed by CA
		if (!dto.verifyCertificate(this.vehicle.getCACertificate())) {
			System.out.println(Resources.WARNING_MSG("Invalid CA Signature on beacon: " + dto.toString() + "\nReporting"));

			SignedCertificateDTO my_dto = new SignedCertificateDTO(dto.getSenderCertificate(),
					this.vehicle.getCertificate(), this.vehicle.getPrivateKey());
			vehicle.reportCertificate(my_dto);
			return false;  // certificate was not signed by CA, beacon is dropped
		}

		// Check cache and or contact RSU, to ensure it is not revoked
		if(vehicle.isRevoked(dto)) {
			System.out.println(Resources.WARNING_MSG("Sender's Certificate is revoked"));
			return false; // certificate was revoked, beacon is dropped
		}

		return true;
	}

	/*
	 * Verifies if certificate has expired
	 * Verifies dto is signed by the sender
	 */
	private boolean authenticateSenderSignature(SignedDTO dto) {
		// Verify if certificate has expired
		try {
			dto.getSenderCertificate().checkValidity();

		} catch (CertificateExpiredException e) {
			System.out.println(Resources.WARNING_MSG("Sender's Certificate has expired: " + dto.toString()));
			return false;  // certificate has expired, beacon is dropped

		} catch (CertificateNotYetValidException e) {
			System.out.println(Resources.WARNING_MSG("Sender's Certificate is not yet valid: " + dto.toString()));
			return false;  // cender certificate is not yet valid, beacon request is dropped
		}

		// Verify digital signature
		if (!dto.verifySignature()) {
			System.out.println(Resources.WARNING_MSG("Invalid digital signature on beacon: " + dto.toString()));
			return false;  // message was not signed by sender, beacon is dropped
		}

		return true;
	}


// ------ REGISTRY METHODS --------

	public void publish() {
		if(isPublished == true) {
			System.out.println(Resources.WARNING_MSG("Vehicle already published."));
			return;
		}

		try {
			RemoteVehicleInterface stub = (RemoteVehicleInterface) UnicastRemoteObject.exportObject(this, 0); // zero means anonymous port
			Registry registry = LocateRegistry.getRegistry(Resources.REGISTRY_PORT);
			registry.rebind(name, stub);
			isPublished = true;
			System.out.println(Resources.OK_MSG("Published vehicle: " + name + "."));
		} catch (Exception e) {
			System.err.println(Resources.ERROR_MSG("Failed to publish remote vehicle: " +  e.getMessage()));
		}
	}

	public void unpublish() {
		if(isPublished == false) {
			System.out.println(Resources.WARNING_MSG("Unpublishing vehicle that was never published"));
			return;
		}

		try {
			Registry registry = LocateRegistry.getRegistry(Resources.REGISTRY_PORT);
			registry.unbind(name);
			UnicastRemoteObject.unexportObject(this, true);
		} catch (Exception e) {
			System.err.println(Resources.ERROR_MSG("Failed to unpublish remote vehicle: " + e.getMessage()));
		}
	}
}