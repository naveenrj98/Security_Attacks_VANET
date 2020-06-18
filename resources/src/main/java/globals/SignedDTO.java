package globals;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * Contains the sender Certificate and generic abstract methods for generating and verifiyng signatures
 */
public abstract class SignedDTO implements Serializable {
    public static final long serialVersionUID = 0;
	protected X509Certificate senderCertificate;
	protected byte[] signature = null;

//  ------- GETTERS / SETTER  ------------

	public X509Certificate getSenderCertificate() { return this.senderCertificate; }
	public byte[] getSignature() { return this.signature; }

	public void setSignature(byte[] sig) { this.signature = sig; }

	public void setCertificate(X509Certificate cert) { this.senderCertificate = cert; }

	/**
	 * Returns the serialized value of this DTO
	 */
	public abstract byte[] serialize();

//  ------- SIGNATURE / CERTIFICATE METHODS  ------------

	/**
	 * Generates the signature for the DTO + senderCertificate in the constructor with given PrivateKey
	 * @param	PrivateKey	key used to signed this DTO
	 * @return	returns the signature generated
	 * <p><b>NOTE:</b> This also sets the internal atribute <b>signature</b> that can be acessed with <b>getSignature</b> or <b>setSignature</b></p>
	 */
	public byte[] sign(PrivateKey pKey) {
		byte[] serializedVal = this.serialize();
		byte[] sig = null;
		try {
			sig = Resources.makeDigitalSignature(serializedVal, pKey); }
		catch (Exception e) {
			System.out.println(Resources.ERROR_MSG("Failed to create signature: "+e.getMessage()));
			return null;
		}
		this.signature = sig;
		return this.signature;
	}

	/*
	 * Verify if the senderCertificate was signed by the given entity
	 * @param	X509Certificate	entity used to verify the senderCertificate in this DTO (usually the CA)
	 * @return	returns true if it was signed by given entity
	 */
	public boolean verifyCertificate(X509Certificate otherEntity) {
		return Resources.verifySignedCertificate(this.senderCertificate, otherEntity.getPublicKey());
	}

	/**
	 * Compares the received signature with the calculated DTO signature
	 * @return	returns true if the signature is correct
	 */
	public boolean verifySignature() {
		try {
			return Resources.verifyDigitalSignature(this.signature, this.serialize(), this.senderCertificate.getPublicKey());
		} catch (Exception e) {
			return false;
		} // message was not signed by sender
	}

	public boolean verifyFreshness(int mil) {
		return false; // does nothing, is implemented in its subclasses
	}
}