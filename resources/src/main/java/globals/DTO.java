package globals;

import java.io.Serializable;
import java.sql.Timestamp;

public abstract class DTO implements Serializable {
    public static final long serialVersionUID = 0;
	protected Timestamp timestamp;

	public Timestamp getTimestamp() { return timestamp; }

	/**
	 * Returns the serialized value of this DTO
	 */
	public abstract byte[] serialize();

	public boolean verifyFreshness(int milisseconds) {
		return Resources.timestampInRange(this.timestamp, milisseconds);
	}
}