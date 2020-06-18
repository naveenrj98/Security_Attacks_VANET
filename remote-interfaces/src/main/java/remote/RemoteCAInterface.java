package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

import globals.SignedCertificateDTO;

public interface RemoteCAInterface extends Remote {

	/**
	 * Verifies revoked state of a given certificate
	 * @param	SignedCertificateDTO		certificate to check validity
	 * @return	boolean			true if its valid, false if its revoked
	 */
	public boolean isRevoked(SignedCertificateDTO dto) throws RemoteException;

	/**
	 * Ask CA to revoke certificate
	 * @param	SignedCertificateDTO		certifica to be revoked
	 * @return	boolean			true if sucessfully revoked, false otherwise
	 */
	public boolean tryRevoke(SignedCertificateDTO dto) throws RemoteException;
}