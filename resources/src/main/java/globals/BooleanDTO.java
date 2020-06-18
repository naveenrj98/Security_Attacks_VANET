package globals;

import java.sql.Timestamp;

public class BooleanDTO extends DTO {
    public static final long serialVersionUID = 0;
	protected boolean bool;

	/**
	 * Builds a BooleanDTO
	 * @param	boolean				bool to send
	 * @param	Timestamp			timestamp of this beacon
	 */
	public BooleanDTO(boolean bool, Timestamp timestamp) {
		this.bool = bool;
		this.timestamp = timestamp;
	}

	/**
	 * Builds a BooleanDTO
	 * @param	boolean				bool to send
	 * <p><b>NOTE:</b> since timestamp is omited a new one is created with current time</p>
	 */
	public BooleanDTO(boolean bool) {
		this(bool, new Timestamp(System.currentTimeMillis()));
	}

//  ------- GETTERS  ------------

	public boolean getValue() { return this.bool; }

	@Override
	public String toString() {
		return this.bool ? "true" : "false";
	}

	@Override
	public byte[] serialize() {
		return this.toString().getBytes();
	}
}
