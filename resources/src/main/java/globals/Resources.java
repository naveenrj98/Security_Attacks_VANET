package globals;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.apache.commons.codec.binary.Base64;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.security.MessageDigest;
import static javax.xml.bind.DatatypeConverter.printHexBinary;


import java.lang.Thread;

public class Resources {

//  ------- NAMES ------------
	public static final String CA_NAME = "ca";
	public static final String VANET_NAME = "vanet";
	public static final String RSU_NAME = "rsu"; //used only for certificate Dir
	public static final String RSU_NAME_1 = "rsu1";
	public static final String RSU_NAME_2 = "rsu2";
	public static final String RSU_NAME_3 = "rsu3";

//  ------- PORTS ------------
	public static final int REGISTRY_PORT = 1099;

//  -------- RANGES ---------
	public static final int MAX_BEACON_RANGE = 500;
	public static final int TOO_DANGEROUS_RANGE = 100;
	public static final int ACCEPTABLE_DATA_TRUST_VARIANCE = 1000;

//  -------- RSU-RANGE ---------
	public static final int MAX_RSU_RANGE = 5000;

//  -------- TIMERS ---------
	public static final int BEACON_INTERVAL = 2000; // miliseconds
	public static final int DANGER_RESET_INTERVAL = 1000; // miliseconds
	public static final int CHECK_RSU_RANGE_INTERVAL = 2000; // miliseconds
	public static final int MAX_INTERVAL_VICINITY_IN_CACHE = 60000; // miliseconds
	public static final int NETWORK_POSITION_UPDATE_INTERVAL = BEACON_INTERVAL; // milisseconds
	public static final int FRESHNESS_MAX_TIME = BEACON_INTERVAL; //miliseconds

// --------  MAP  ------------
	public static final int DEFAULT_MAP_WIDTH = 1800;
	public static final int DEFAULT_MAP_HEIGHT = 1800;

//  ------- SCORE ------------
	public static final int MAX_REVOKE_SCORE = 2;

//  ------- PATHS ------------
	public static final String CA_REVOKED = "cert/revoked/";
	public static final String CERT_DIR = "cert/";
	public static final String CA_PK = "cert/ca-certificate.pem.txt";
	public static final String CA_CERT = "cert/ca-key.pem.txt";

//  ------- ATTACKS ------------
	public static final int HELD_BEACONS_FOR_REPLAY_ATTACKS = 20;
	public static final double ATACKER_POSITION_RANGE = 5000;

//  ------- OTHER ------------
	public static final String CA_DIGEST = "SHA-256";

//  ------- PASSWORDS ------------
	public static final char[] STORE_PASS = "f4ncyP455WORd".toCharArray();
	public static final char[] KEY_PASS = "Y3tAn0th3rF4ncyPa5sW00rd".toCharArray();
	public static final char[] CA_CERTIFICATE_PASS="Th1sC4antB3.0neMorePa55?".toCharArray();

//  ------- OUTPUT METHODS ------------
	public static String ERROR_MSG(String msg) {
		StackTraceElement st = Thread.currentThread().getStackTrace()[2]; // caller stack element
		return "[\033[0;31mERROR\033[0m] [\033[1;35m"+st.getClassName()+"."+st.getMethodName()+"\033[0m] "+ msg; }
	public static String WARNING_MSG(String msg) {
		StackTraceElement st = Thread.currentThread().getStackTrace()[2]; // caller stack element
		return "[\033[0;33mWARN\033[0m] [\033[1;35m"+st.getClassName()+"."+st.getMethodName()+"\033[0m] "+msg; }
	public static String NOTIFY_MSG(String msg) {
		StackTraceElement st = Thread.currentThread().getStackTrace()[2]; // caller stack element
		return "[\033[0;34mNOTE\033[0m] [\033[1;35m"+st.getClassName()+"."+st.getMethodName()+"\033[0m] "+msg; }
	public static String OK_MSG(String msg) {
		StackTraceElement st = Thread.currentThread().getStackTrace()[2]; // caller stack element
		return "[\033[0;32m OK \033[0m] [\033[1;35m"+st.getClassName()+"."+st.getMethodName()+"\033[0m] "+msg; }
//  -----------------------------------


//  ------- KEYSTORES ------------

	public static KeyStore readKeystoreFile(String keyStoreFilePath, char[] keyStorePassword) throws Exception {
		FileInputStream fis;
		try { fis = new FileInputStream(keyStoreFilePath); }
		catch (FileNotFoundException e) {
			System.err.println(ERROR_MSG("Keystore File not found: " + keyStoreFilePath));
			return null;
		}
		KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		keystore.load(fis, keyStorePassword);
		return keystore;
	}

	// OVERLOADED METHOD
	public static PrivateKey getPrivateKeyFromKeystore(String keyStoreFilePath, char[] keyStorePassword, String keyAlias, char[] keyPassword) throws Exception {
		KeyStore keystore = readKeystoreFile(keyStoreFilePath, keyStorePassword);
		return getPrivateKeyFromKeystore(keystore, keyAlias, keyPassword);
	}
	public static PrivateKey getPrivateKeyFromKeystore(KeyStore keystore, String keyAlias, char[] keyPassword) throws Exception {
		return (PrivateKey) keystore.getKey(keyAlias, keyPassword);
	}


	// OVERLOADED METHOD
	public static Certificate getCertificateFromKeystore(String keyStoreFilePath, char[] keyStorePassword, String certAlias) throws Exception {
		KeyStore keystore = readKeystoreFile(keyStoreFilePath, keyStorePassword);
		return getCertificateFromKeystore(keystore, certAlias);
	}
	public static Certificate getCertificateFromKeystore(KeyStore keystore, String certAlias) throws Exception {
		return keystore.getCertificate(certAlias);
	}

//  -----------------------------------



//  ------- CERTIFICATES ------------

	public static Certificate readCertificateFile(String certificateFilePath) throws Exception {
		FileInputStream fis;

		try { fis = new FileInputStream(certificateFilePath); }
		catch (FileNotFoundException e) {
			System.err.println(ERROR_MSG("Certificate File not found: " + certificateFilePath));
			return null;
		}
		BufferedInputStream bis = new BufferedInputStream(fis);
		CertificateFactory cf = CertificateFactory.getInstance("X.509");

		if (bis.available() > 0) { return cf.generateCertificate(bis); }
		bis.close();
		fis.close();
		return null;
	}

	public static boolean verifySignedCertificate(Certificate certificate, PublicKey caPublicKey) {
		try { certificate.verify(caPublicKey); }
		catch (Exception e) {
			return false;
		}
		return true;
	}

	public static String convertToPemCertificate(X509Certificate certificate) {
		Base64 encoder = new Base64(64, "\n".getBytes());
		String cert_begin = "-----BEGIN CERTIFICATE-----\n";
		String end_cert = "-----END CERTIFICATE-----\n";

		byte[] derCert = null;
		try {
			derCert = certificate.getEncoded();
		} catch (CertificateEncodingException e) {
			System.err.println(WARNING_MSG("Error in certificate conversion :" + e.getMessage()));
			return null;
		}
		String pemCertPre = new String(encoder.encode(derCert));
		String pemCert = cert_begin + pemCertPre + end_cert;
		return pemCert;
	}

//  -----------------------------------


//  ------- SIGNATURES ------------

	public static byte[] makeDigitalSignature(byte[] bytes, PrivateKey privateKey) throws Exception {
		Signature sig = Signature.getInstance("SHA1WithRSA");
		sig.initSign(privateKey);
		sig.update(bytes);
		byte[] signature = sig.sign();
		return signature;
	}

	public static boolean verifyDigitalSignature(byte[] cipherDigest, byte[] bytes, PublicKey publicKey) throws Exception {
		Signature sig = Signature.getInstance("SHA1WithRSA");
		sig.initVerify(publicKey);
		sig.update(bytes);
		try {
			return sig.verify(cipherDigest);
		} catch (SignatureException se) {
			System.err.println(WARNING_MSG("Invalid Signature :" + se.getMessage()));
			return false;
		}
	}

	/**
	 * Returns string given hashed with Resources.CA_DIGEST
	 */
	public static String genHashedName(String valToHash) {
		MessageDigest digest = null;
		try { digest = MessageDigest.getInstance(Resources.CA_DIGEST); }
		catch (java.security.NoSuchAlgorithmException e) { return null; } // im confident it wont hapen
		return printHexBinary(digest.digest(valToHash.getBytes())).toLowerCase();
	}

//  -------------- TIMESTAMPS ---------------------
	public static boolean timestampInRange(Timestamp timestamp, int milisseconds) {
		return (timestamp.getTime() + milisseconds) >= System.currentTimeMillis();
	}

}
