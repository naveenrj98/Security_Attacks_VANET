package globals;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;

/**
 * Extension of the CertificateDTO but with signature
 */
public class SignedCertificateDTO extends SignedDTO {
    public static final long serialVersionUID = 0;
	private CertificateDTO certDTO;

	/**
	 * Builds a signed CertificateDTO
	 * @param	X509Certificate		certificate to send
	 * @param	Timestamp			timestamp of this beacon
	 * @param	X509Certificate		certificate the entity sending this beacon
	 */
	public SignedCertificateDTO(X509Certificate certificate, Timestamp timestamp, X509Certificate senderCert, PrivateKey pKey) {
		this.setCertificate(senderCert);
		this.certDTO = new CertificateDTO(certificate, timestamp);
		this.sign(pKey);
	}

	/**
	 * Builds a signed CertificateDTO
	 * @param	X509Certificate		certificate to send
	 * @param	X509Certificate		certificate the entity sending this beacon
	 * <p><b>NOTE:</b> since timestamp is omited a new one is created with current time</p>
	 */
	public SignedCertificateDTO(X509Certificate certificate, X509Certificate senderCert,PrivateKey pKey) {
		this.setCertificate(senderCert);
		this.certDTO = new CertificateDTO(certificate);
		this.sign(pKey);
	}

//  ------- GETTERS  ------------

	public Timestamp getTimestamp() { return this.certDTO.getTimestamp(); }
	public X509Certificate getCertificate() { return this.certDTO.certificate; }
	public String toString() { return this.certDTO.toString(); }

	/**
	 * Returns the serialized value of this DTO
	 */
	@Override
	public byte[] serialize() {
		// Join serializations
		byte[] serializedDTO = this.certDTO.serialize();
		byte[] serializedCert = this.senderCertificate.toString().getBytes();
		byte[] newSerialization = new byte[serializedDTO.length + serializedCert.length];
		System.arraycopy(serializedDTO, 0, newSerialization, 0, serializedDTO.length);
		System.arraycopy(serializedCert, 0, newSerialization, serializedDTO.length, serializedCert.length);
		return newSerialization;
	}

	@Override
	public boolean verifyFreshness(int miliseconds) {
		return certDTO.verifyFreshness(miliseconds);
	}
}

