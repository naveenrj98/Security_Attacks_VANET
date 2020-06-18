package globals;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;

/**
 * Extension of the CertificateDTO but with signature
 */
public class SignedBooleanDTO extends SignedDTO {
    public static final long serialVersionUID = 0;
	private BooleanDTO bool;

	/**
	 * Builds a signed CertificateDTO
	 * @param	boolean				bool to send
	 * @param	Timestamp			timestamp of this beacon
	 * @param	X509Certificate		certificate the entity sending this beacon
	 */
	public SignedBooleanDTO(boolean bool, Timestamp timestamp, X509Certificate senderCert, PrivateKey pKey) {
		this.setCertificate(senderCert);
		this.bool = new BooleanDTO(bool, timestamp);
		this.sign(pKey);
	}

	/**
	 * Builds a signed CertificateDTO
	 * @param	boolean				bool to send
	 * @param	X509Certificate		certificate the entity sending this beacon
	 * <p><b>NOTE:</b> since timestamp is omited a new one is created with current time</p>
	 */
	public SignedBooleanDTO(boolean bool, X509Certificate senderCert,PrivateKey pKey) {
		this.setCertificate(senderCert);
		this.bool = new BooleanDTO(bool);
		this.sign(pKey);
	}

//  ------- GETTERS  ------------

	public Timestamp getTimestamp() { return this.bool.getTimestamp(); }
	public boolean getValue() { return this.bool.getValue(); }
	public String toString() { return this.bool.toString(); }

	/**
	 * Returns the serialized value of this DTO
	 */
	@Override
	public byte[] serialize() {
		// Join serializations
		byte[] serializedDTO = this.bool.serialize();
		byte[] serializedCert = this.senderCertificate.toString().getBytes();
		byte[] newSerialization = new byte[serializedDTO.length + serializedCert.length];
		System.arraycopy(serializedDTO, 0, newSerialization, 0, serializedDTO.length);
		System.arraycopy(serializedCert, 0, newSerialization, serializedDTO.length, serializedCert.length);
		return newSerialization;
	}

	@Override
	public boolean verifyFreshness(int miliseconds) {
		return bool.verifyFreshness(miliseconds);
	}
}

