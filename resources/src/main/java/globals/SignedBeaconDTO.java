package globals;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;

/**
 * Extension of the BeaconDTO but with signature
 */
public class SignedBeaconDTO extends SignedDTO {
    public static final long serialVersionUID = 0;
	private BeaconDTO beaconDTO;

	/**
	 * Builds a signed BeaconDTO
	 * @param	Vector2D			position vector
	 * @param	Vector2D			velocity vector
	 * @param	Timestamp			timestamp of this beacon
	 * @param	X509Certificate		certificate the entity sending this beacon
	 */
	public SignedBeaconDTO(Vector2D pos, Vector2D vel, Timestamp timestamp, X509Certificate senderCert, PrivateKey pKey) {
		this.setCertificate(senderCert);
		this.beaconDTO = new BeaconDTO(pos, vel, timestamp);
		this.sign(pKey);
	}

	/**
	 * Builds a signed BeaconDTO
	 * @param	Vector2D			position vector
	 * @param	Vector2D			velocity vector
	 * @param	X509Certificate		certificate the entity sending this beacon
	 * <p><b>NOTE:</b> since timestamp is omited a new one is created with current time</p>
	 */
	public SignedBeaconDTO(Vector2D pos, Vector2D vel, X509Certificate senderCert, PrivateKey pKey) {
		this.setCertificate(senderCert);
		this.beaconDTO = new BeaconDTO(pos, vel);
		this.sign(pKey);
	}

//  ------- GETTERS  ------------
	public BeaconDTO beaconDTO() { return this.beaconDTO; }
	public Vector2D getPosition() { return this.beaconDTO.getPosition(); }
	public Vector2D getVelocity() { return this.beaconDTO.getVelocity(); }
	public Timestamp getTimestamp() { return this.beaconDTO.getTimestamp(); }
	public String toString() { return this.beaconDTO.toString(); }

	/**
	 * Returns the serialized value of this DTO
	 */
	@Override
	public byte[] serialize() {
		// Join serializations
		byte[] serializedDTO = this.beaconDTO.serialize();
		byte[] serializedCert = this.senderCertificate.toString().getBytes();
		byte[] newSerialization = new byte[serializedDTO.length + serializedCert.length];
		System.arraycopy(serializedDTO, 0, newSerialization, 0, serializedDTO.length);
		System.arraycopy(serializedCert, 0, newSerialization, serializedDTO.length, serializedCert.length);
		return newSerialization;
	}

	@Override
	public boolean verifyFreshness(int miliseconds) {
		return beaconDTO.verifyFreshness(miliseconds);
	}
}

