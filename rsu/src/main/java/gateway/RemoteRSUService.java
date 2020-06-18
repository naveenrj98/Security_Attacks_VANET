package gateway;

import globals.Resources;
import globals.SignedBooleanDTO;
import remote.RemoteRSUInterface;
import remote.RemoteCAInterface;
import remote.RemoteVehicleNetworkInterface;

import globals.SignedCertificateDTO;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;


public class RemoteRSUService implements RemoteRSUInterface {

	private RSU rsu;
	private boolean isPublished = false;
	private RemoteCAInterface ca;
	private RemoteVehicleNetworkInterface vehicle_network;

	public RemoteRSUService(RSU rsu, RemoteCAInterface ca, RemoteVehicleNetworkInterface vehicle_network) {
		this.rsu = rsu;
		this.ca = ca;
		this.vehicle_network = vehicle_network;
	}

	@Override
	public SignedBooleanDTO isRevoked(SignedCertificateDTO dto) throws RemoteException {

		// Verify that sender is trustworthy
		if(!authenticateSender(dto))
			return new SignedBooleanDTO(false, this.rsu.getCertificate(), this.rsu.getPrivateKey());

		// verify if certificate has expired
		try { dto.getCertificate().checkValidity();
		} catch (CertificateExpiredException e) {
			rsu.addCertificateToCache(dto.getCertificate()); //doesnt add duplicates. checks if certificate is in cache
			return new SignedBooleanDTO(true, this.rsu.getCertificate(), this.rsu.getPrivateKey());  // certificate has expired
		} catch (CertificateNotYetValidException e) {

			// QUESTION: dont add to cache, so we dont have to remove it when it becomes valid?
			//			 Or has same behaviour as CertificateExpiredException?
			return new SignedBooleanDTO(true, this.rsu.getCertificate(), this.rsu.getPrivateKey());
		}

		// verify if revoked certificate is in cache
		if(rsu.isCertInCache(dto.getCertificate())) {
			System.out.println(Resources.OK_MSG("Certificate is revoked"));
			return new SignedBooleanDTO(true, this.rsu.getCertificate(), this.rsu.getPrivateKey());
		}

		// Contact CA with possible revoked certificate
		if(ca.isRevoked(new SignedCertificateDTO(dto.getCertificate() ,rsu.getCertificate(), rsu.getPrivateKey()))) {

			try { rsu.addCertificateToCache(dto.getCertificate());
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}

			System.out.println(Resources.OK_MSG("Certificate is revoked"));
			return new SignedBooleanDTO(true, this.rsu.getCertificate(), this.rsu.getPrivateKey());
		}

		return new SignedBooleanDTO(false, this.rsu.getCertificate(), this.rsu.getPrivateKey());
	}

	@Override
	public SignedBooleanDTO tryRevoke(SignedCertificateDTO dto) throws RemoteException {

		// Verify that sender is trustworthy
		if(!authenticateSender(dto))
			return new SignedBooleanDTO(false, this.rsu.getCertificate(), this.rsu.getPrivateKey());

		// verify if certificate has expired
		try { dto.getCertificate().checkValidity();
		} catch (CertificateExpiredException e) {
			rsu.addCertificateToCache(dto.getCertificate()); //doesnt add duplicates. checks if certificate is in cache
			return new SignedBooleanDTO(true, this.rsu.getCertificate(), this.rsu.getPrivateKey());  // certificate has expired
		} catch (CertificateNotYetValidException e) {
			return new SignedBooleanDTO(true, this.rsu.getCertificate(), this.rsu.getPrivateKey());
		}


		// verify if certificate is in cache
		if(rsu.isCertInCache(dto.getCertificate()))
			return new SignedBooleanDTO(true, this.rsu.getCertificate(), this.rsu.getPrivateKey());

		if(ca.tryRevoke(dto)) {
			try {
				rsu.addCertificateToCache(dto.getCertificate());
				rsu.shareRevoked(dto);
			} catch(Exception e) {
				System.out.println(Resources.WARNING_MSG(e.getMessage()));
			}

			informVehiclesOfRevocation(
						new SignedCertificateDTO(dto.getCertificate(), rsu.getCertificate(), rsu.getPrivateKey()));
			return new SignedBooleanDTO(true, this.rsu.getCertificate(), this.rsu.getPrivateKey());
		}

		return new SignedBooleanDTO(false, this.rsu.getCertificate(), this.rsu.getPrivateKey());
	}

	// TODO:
	// TESTING
	@Override
	public void receiveRevoked(SignedCertificateDTO dto) throws RemoteException {

		// Verify that sender is trustworthy
		if(!authenticateSender(dto))
			return;

		// Add revoked certificate to cache
		rsu.addCertificateToCache(dto.getCertificate());
	}

	// ------ INTERNAL METHODS --------

	/**
	 * Verifies if certificate was signed by the CA
	 * Verifies if certificate has not expired
	 * Verifies if certificate is not revoked (cached or contact CA)
	 * Verifies if sender signed the dto
	 * If no verification fails returns true
	 */
	private boolean authenticateSender(SignedCertificateDTO dto) throws RemoteException {

		// verify Timestamp freshness
		if(!dto.verifyFreshness(Resources.FRESHNESS_MAX_TIME)) {
			System.out.println(Resources.WARNING_MSG("Sender's communication is not fresh: " + dto));
			return false;  // certificate was not signed by CA, isRevoked  request is dropped
		}

		// verify if certificate was signed by CA
		if (!dto.verifyCertificate(this.rsu.getCACertificate())) {
			System.out.println(Resources.WARNING_MSG("Invalid CA Signature on isRevoked request: " + dto));
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

		// verify if certificate is revoked
		if(rsu.isCertInCache(dto.getSenderCertificate())) {
			System.out.println(Resources.WARNING_MSG("Sender's Certificate is revoked"));
			return false; // certificate was revoked, isRevoked  request is dropped
		}

		// Contact CA to verify if senders certificate is revoked
		if(ca.isRevoked(new SignedCertificateDTO(dto.getSenderCertificate() ,rsu.getCertificate(), rsu.getPrivateKey()))) {
			System.out.println(Resources.WARNING_MSG("Sender's Certificate is revoked"));
			return false; // certificate was revoked, isRevoked  request is dropped
		}

		// verify signature sent
		if (!dto.verifySignature()) {
			System.out.println(Resources.WARNING_MSG("Invalid digital signature on isRevoked request: " + dto));
			return false;  // certificate was not signed by sender
		}

		return true; // Sender is authenticated
	}

	private void informVehiclesOfRevocation(SignedCertificateDTO dto) throws RemoteException {
		vehicle_network.informVehiclesOfRevocation(dto, rsu.getPosition());
	}

	// ------ REGISTRY METHODS --------

	public void publish() throws RemoteException {
		if(isPublished) {
			System.out.println(Resources.WARNING_MSG(rsu.getName() + " already published."));
			return;
		}

		try {
			RemoteRSUInterface stub = (RemoteRSUInterface) UnicastRemoteObject.exportObject(this, 0);
			Registry registry;
			registry = LocateRegistry.getRegistry(Resources.REGISTRY_PORT);
			registry.rebind(rsu.getName(), stub);
			isPublished = true;
			System.out.println(Resources.OK_MSG(rsu.getName()+" published to registry."));

			try {
				addRsuToNetwork();
			}catch (Exception e) {
				System.err.println(Resources.ERROR_MSG("VANET seems dead... "));
			}

		} catch (Exception e) {
			System.err.println(Resources.ERROR_MSG("Failed to publish remote RSU: " + e.getMessage()));
		}

	}

	public void unpublish() {
		if(!isPublished) {
			System.out.println(Resources.WARNING_MSG("Unpublishing "+rsu.getName()+" that was never published."));
			return;
		}

		try {
			Registry registry = LocateRegistry.getRegistry(Resources.REGISTRY_PORT);
			registry.unbind(rsu.getName());
			UnicastRemoteObject.unexportObject(this, true);

			try {
				removeRsuFromNetwork();
			} catch (Exception e) {/* VANET is dead */}

		} catch (Exception e) {
			System.err.println(Resources.ERROR_MSG("Unpublishing RSU: " + e.getMessage()));
		}



	}

	private void addRsuToNetwork() throws RemoteException {
		if(this.vehicle_network.addRSU(rsu.getPosition(), rsu.getName()))
            System.out.println(Resources.OK_MSG(rsu.getName()+" added to network."));
        else
            System.err.println(Resources.ERROR_MSG("Failed to add "+rsu.getName()+" to network."));
	}

	private void removeRsuFromNetwork() throws RemoteException {
		if(this.vehicle_network.removeRSU(rsu.getName()))
			System.out.println(Resources.OK_MSG(rsu.getName()+" removed from network."));
		else
            System.err.println(Resources.ERROR_MSG("Failed to remove "+rsu.getName()+" from network."));
	}

	public RemoteVehicleNetworkInterface getNetwork() {
		return vehicle_network;
	}

}