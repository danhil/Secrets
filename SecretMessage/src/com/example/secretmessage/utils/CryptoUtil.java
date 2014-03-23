package com.example.secretmessage.utils;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import android.util.Base64;

public class CryptoUtil
{
	static final String TAG = CryptoUtil.class.getSimpleName();

	
	// Working variables
	private static final String CIPHER_ALGORITHM = "AES";			// Type of encryption
	private static final String RANDOM_GENERATOR_TYPE = "SHA1PRNG";	// Type of random key
	private static final int RANDOM_KEY_SIZE = 128;					// Size of key

	// Encrypts a string using 128-bit AES encryption
	public static String encrypt(String data, String key) throws Exception
	{
		byte[] myKey = generateKey(key.getBytes());
		byte[] dataCopy = data.getBytes();
		
		// Grab secret key
		SecretKeySpec spec = new SecretKeySpec(myKey, CIPHER_ALGORITHM);
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, spec);
		
		// Encrypt data
		byte[] encryptedData = cipher.doFinal(dataCopy);
		String encryptedString = Base64.encodeToString(encryptedData, Base64.DEFAULT);
		
		return encryptedString;
	}
	
	// Decryptes a string encoded in 128-bit AES
	public static String decrypt(String data, String key) throws Exception
	{
		byte[] myKey = generateKey(key.getBytes());
		
		// Grab secret key
		SecretKeySpec spec = new SecretKeySpec(myKey, CIPHER_ALGORITHM);
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, spec);
		
		// Decrypt data
		byte[] encryptedData = Base64.decode(data, Base64.DEFAULT);
		byte[] decryptedData = cipher.doFinal(encryptedData);
		
		// Convert to string
		String result = new String(decryptedData);
		
		return result;
	}
	
	public static byte[] generateKey(byte[] randomSeed) throws Exception
	{
		// Set up generators
		KeyGenerator gen = KeyGenerator.getInstance(CIPHER_ALGORITHM);
		SecureRandom rand = SecureRandom.getInstance(RANDOM_GENERATOR_TYPE);
		rand.setSeed(randomSeed);
		
		// Generate key
		gen.init(RANDOM_KEY_SIZE, rand);
		SecretKey myKey = gen.generateKey();
		
		return myKey.getEncoded();
	}
	
};

