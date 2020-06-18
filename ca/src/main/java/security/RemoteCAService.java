package security;

import globals.Resources;
import globals.SignedCertificateDTO;
import remote.RemoteCAInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class RemoteCAService implements RemoteCAInterface {
	private boolean isPublished = false;
	private X509Certificate myCert;
	private Map<X509Certificate, Set<X509Certificate>> revokeRating = new HashMap<>();

	public RemoteCAService(X509Certificate myCert) {
		this.myCert = myCert;
 		// make revoked dir if unexistent
		File revokedDir = new File(Resources.CA_REVOKED);
		if (! revokedDir.exists()) revokedDir.mkdir();

	}

// ------ INTERFACE METHODS --------

	/**
	 * Verifies revoked state of a given certificate
	 * @param	SignedCertificateDTO		certificate to check validity
	 * @return	boolean			true if its valid, false if its revoked
	 */
	@Override
	public boolean isRevoked(SignedCertificateDTO dto) throws RemoteException {
		if (!this.authenticateSender(dto))
			return false;
		return this.findCertificate(dto.getCertificate()) != null;
	}

	/**
	 * Ask CA to revoke certificate
	 * @param	SignedCertificateDTO		certifica to be revoked
	 * @return	boolean			true if sucessfully revoked, false otherwise
	 */
	@Override
	public boolean tryRevoke(SignedCertificateDTO dto) throws RemoteException {
		if (!this.authenticateSender(dto))
			return false;

		X509Certificate certToRevoke = dto.getCertificate();
		String hashedCert = Resources.genHashedName(Resources.convertToPemCertificate(certToRevoke));
		File localCert = this.findCertificate(certToRevoke);
		if (localCert != null) {
			System.out.println(Resources.OK_MSG("Already revoked: "+hashedCert));
			return true; // its already revoked
		}

		if (! this.ponderateRevokeRequest(dto.getCertificate(), dto.getSenderCertificate()))
			return false; // sender not allowed to revoke or score not high enough

		// all else fails so we revoke the certificate
		FileOutputStream out = null;
		try {
 			out = new FileOutputStream(Resources.CA_REVOKED+hashedCert);
			out.write(Resources.convertToPemCertificate(certToRevoke).getBytes());
			out.close();
		} catch (Exception e) {
			// FileOutputStream only throws this if it fails to create the file...
			System.out.println(Resources.WARNING_MSG("Error Creating File: "+hashedCert+". "+e.getMessage()));
			return false;
		}
		System.out.println(Resources.OK_MSG("Sucessfully revoked: "+hashedCert));
		return true;
	}

// -------------------------------



// ------ INTERNAL METHODS --------

	/**
	 * <p>Verifies if certificate was signed by the CA</p>
	 * <p>Verifies if its not revoked</p>
	 * <p>Verfies Signature</p>
	 * If no verification fails returns true
	 */
	private boolean authenticateSender(SignedCertificateDTO dto) {
		// verify if certificate was signed by CA
		// FIXME uncoment this when we are able to correctly read the CA certificate from the file (it has a weird format)
		/*
		if (!dto.verifyCertificate(this.myCert)) {
			System.out.println(Resources.WARNING_MSG("Invalid CA Signature on isRevoked request: " + dto.toString()));
			return false;  // certificate was not signed by CA, isRevoked  request is dropped
		}
		*/

		// verify if certificate has expired
		try { dto.getSenderCertificate().checkValidity();
		} catch (CertificateExpiredException e) {
			System.out.println(Resources.WARNING_MSG("Sender's Certificate has expired: " + dto.toString()));
			return false;  // certificate has expired, isRevoked  request is dropped

		} catch (CertificateNotYetValidException e) {
			System.out.println(Resources.WARNING_MSG("Sender's Certificate is not yet valid: " + dto.toString()));
			return false;  // certificate was not yet valid, isRevoked  request is dropped
		}

		// verify if certificate is revoked
		if(this.findCertificate(dto.getSenderCertificate()) != null) {
			System.out.println(Resources.WARNING_MSG("Sender's Certificate is revoked"));
			return false;
		}

		// verify signature sent
		if (!dto.verifySignature()) {
			System.out.println(Resources.WARNING_MSG("Invalid digital signature on isRevoked request: " + dto.toString()));
			return false;  // certificate was not signed by sender
		}

		return true; // Sender is authenticated
	}
	private boolean ponderateRevokeRequest(X509Certificate toRevoke, X509Certificate senderCert) {
		if (!this.revokeRating.containsKey(toRevoke))
			this.revokeRating.put(toRevoke, new HashSet<X509Certificate>());
		this.revokeRating.get(toRevoke).add(senderCert);

		String hashedCert = Resources.genHashedName(Resources.convertToPemCertificate(senderCert));
		System.out.println(Resources.WARNING_MSG("Request to revoke Certificate: " + hashedCert)
				+ "\n\tNow has a score of " + this.revokeRating.get(toRevoke).size());

		return this.revokeRating.get(toRevoke).size() >= Resources.MAX_REVOKE_SCORE;
	}

	/**
	 * Finds certificate in the revoked directory, if not found returns null
	 */
	private File findCertificate(X509Certificate certToLocate) {
		String pemCertificate = Resources.convertToPemCertificate(certToLocate);
		String hashedCertificate = Resources.genHashedName(pemCertificate);

		File dirObj = new File(Resources.CA_REVOKED);
		if ( ! dirObj.exists() || ! dirObj.isDirectory()) return null;
		for (File iter : dirObj.listFiles())
			if (iter.getName().contains(hashedCertificate))
				return iter;
		return null;
	}

// -------------------------------

// ------ REGISTRY METHODS --------

	public void publish() {
		if (this.isPublished) {
			System.out.println(Resources.WARNING_MSG(Resources.CA_NAME+" already published."));
			return;
		}

		try {
			RemoteCAInterface stub = (RemoteCAInterface) UnicastRemoteObject.exportObject(this,0);
			Registry reg = LocateRegistry.getRegistry(Resources.REGISTRY_PORT);
			reg.rebind(Resources.CA_NAME, stub);
			this.isPublished = true;
			System.out.println(Resources.OK_MSG(Resources.CA_NAME+" published to registry."));
		}
		catch (Exception e) {
            System.err.println(Resources.ERROR_MSG("Failed to publish remote CA: " + e.getMessage()));
		 }
	}

	public void unpublish() {
        if(! isPublished) {
            System.out.println(Resources.WARNING_MSG("Unpublishing "+Resources.VANET_NAME+" that was never published."));
            return;
        }

        try {
            Registry registry = LocateRegistry.getRegistry(Resources.REGISTRY_PORT);
            registry.unbind(Resources.CA_NAME);
            UnicastRemoteObject.unexportObject(this, true);
        } catch (Exception e) {
            System.err.println(Resources.ERROR_MSG("Unpublishing CA: " + e.getMessage()));
        }
	}
}