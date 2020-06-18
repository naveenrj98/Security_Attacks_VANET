package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

import globals.SignedBooleanDTO;
import globals.SignedCertificateDTO;


public interface RemoteRSUInterface extends Remote {

	/**
	 * Verifies revoked state of a given certificate ( uses RSU cache, if not found asks the CA )
	 * @param	SignedCertificateDTO	DTO containing certificate to be checked
	 * @return		 true if its valid, false if its revoked (signed)
	 */
	public SignedBooleanDTO isRevoked(SignedCertificateDTO dto) throws RemoteException;


	/**
	 * Ask RSU to revoke certificate, reroutes request to CA.
	 * CA will handle certificate revocation, RSU will only maintain a cache if result
	 * of revocation is true.
	 * @param	SignedCertificateDTO	DTO containing certificate to be revoked
	 * @return		 true if it was revoked, false otherwise (signed)
	 */
	public SignedBooleanDTO tryRevoke(SignedCertificateDTO dto) throws RemoteException;

	/**
	 * Receives a new revoked certificate from the RSU's in the vicinity
	 * @param	SignedCertificateDTO	DTO containing certificate to be shared
	 */
	public void receiveRevoked(SignedCertificateDTO dto) throws RemoteException;

}
