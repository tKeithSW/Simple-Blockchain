package simpleBlockchainV2;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import org.bouncycastle.util.encoders.Hex;


public class Utility {
	public static Gson gson = new Gson();

	// Applies Sha256 to a string
	public static String applySha256(String input) {

		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");

			// Applies sha256 to our input,
			byte[] hash = digest.digest(input.getBytes("UTF-8"));

			StringBuffer hexString = new StringBuffer(); // This will contain hash as hexidecimal
			for (byte b : hash) {
				String hex = Integer.toHexString (0xff & b);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// Applies ECDSA Signature and returns the result ( as bytes ).
	public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
		Signature dsa;
		// byte[] output = new byte[0];
		try {
			dsa = Signature.getInstance("ECDSA", "BC");
			dsa.initSign(privateKey);
			byte[] strByte = input.getBytes();
			dsa.update(strByte);
			return dsa.sign();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// Verifies a String signature
	public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
		try {
			Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
			ecdsaVerify.initVerify(publicKey);
			ecdsaVerify.update(data.getBytes());
			return ecdsaVerify.verify(signature);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// Returns difficulty string target, to compare to hash. eg difficulty of 5 will
	// return "00000"
	public static String getDificultyString(int difficulty) {
		return new String(new char[difficulty]).replace('\0', '0');
	}

	public static String getStringFromKey(Key key) {
		//return Base64.getEncoder().encodeToString(key.getEncoded());
		return Hex.toHexString(key.getEncoded());
	}

	// get publicKey from hex String
	public static String getStringFromPublicKey(PublicKey key) {
		KeyFactory factory;
		X509EncodedKeySpec spec = null;
		try {
			factory = KeyFactory.getInstance("ECDSA", "BC");
			spec = factory.getKeySpec(key, X509EncodedKeySpec.class);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();

		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return Hex.toHexString(spec.getEncoded());
	}

	// get privateKey from hex String
	public static String getStringFromPrivateKey(PrivateKey key) {
		KeyFactory factory;
		PKCS8EncodedKeySpec spec = null;
		try {
			factory = KeyFactory.getInstance("DSA");
			spec = factory.getKeySpec(key, PKCS8EncodedKeySpec.class);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return Base64.getEncoder().encodeToString(spec.getEncoded());
	}

	public static PublicKey getPublicKeyFromString(final String publicKeyStr) {
		try {
			X509EncodedKeySpec spec = new X509EncodedKeySpec(Hex.decode(publicKeyStr));
			KeyFactory factory = KeyFactory.getInstance("ECDSA", "BC");
			return factory.generatePublic(spec);
		} catch (Exception e) {
			throw new IllegalStateException("Can't transform [" + publicKeyStr + "] to PublicKey", e);
		}
	}

	public static PrivateKey getPrivateKeyFromString(final String privateKeyStr) {
		try {
			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Hex.decode(privateKeyStr));
			KeyFactory factory = KeyFactory.getInstance("ECDSA", "BC");
			return factory.generatePrivate(spec);
		} catch (Exception e) {
			throw new IllegalStateException("Can't transform [" + privateKeyStr + "] to PrivateKey", e);
		}
	}

	public static String serialiseToJson(Object o) {
		return new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(o);
	}

	// Deserialise JSON to Wallet
	public static Wallet parseJsonToWallet(String json){
		Wallet wallet = null;
		try {
			wallet = gson.fromJson(new FileReader(json), Wallet.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		wallet.generateKeysFromStrings(); 
		return wallet;
	}

	// Deserialise JSON to blockchain arraylist
	public static ArrayList<Block> parseJsonToBlockChain(String json){
		ArrayList<Block> blockchain = null;
		Block[] blockChainArray = null;
		try {
			blockChainArray = gson.fromJson(new FileReader(json), Block[].class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		blockchain = new ArrayList<Block>(Arrays.asList(blockChainArray));
		return blockchain;
	}

	// Serialise String to File
	public static void jsonStringToFile (String json, String filename){
		try {
			FileWriter writer = new FileWriter(filename);
			writer.write(json);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			System.err.println("Write fail!");
			e.printStackTrace();
		}
		System.out.println("Write to file successful!");
	}
}