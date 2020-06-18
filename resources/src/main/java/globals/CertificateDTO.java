package globals;

import java.security.cert.X509Certificate;
import java.sql.Timestamp;

public class CertificateDTO extends DTO {
    public static final long serialVersionUID = 0;
	protected X509Certificate certificate;

	/**
	 * Builds a CertificateDTO
	 * @param	X509Certificate		certificate to send
	 * @param	Timestamp			timestamp of this beacon
	 */
	public CertificateDTO(X509Certificate certificate, Timestamp timestamp) {
		this.certificate = certificate;
		this.timestamp = timestamp;
	}

	/**
	 * Builds a CertificateDTO
	 * @param	X509Certificate		certificate to send
	 * <p><b>NOTE:</b> since timestamp is omited a new one is created with current time</p>
	 */
	public CertificateDTO(X509Certificate certificate) {
		this(certificate, new Timestamp(System.currentTimeMillis()));
	}

//  ------- GETTERS  ------------

	public X509Certificate getCertificate() { return this.certificate; }

	@Override
	public String toString() {
		return Resources.genHashedName(Resources.convertToPemCertificate(this.certificate))+"\n"+this.timestamp.toString();
	}

	@Override
	public byte[] serialize() {
		return this.toString().getBytes();
	}
}
