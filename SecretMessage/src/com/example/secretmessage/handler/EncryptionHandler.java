/**
 * Encapsulates all functions and variables required to pass keys securely (using DH), encrypt/decrypt text (using AES in CTR mode), and store/retrieve secret keys (using Keystore)
 * 
 * Alice should call aliceProducePublicKey() and send the result to Bob.
 * Bob should call bobProducePublicKeyAndSecretKey() with the data received from Alice and then pass the result back to Alice.
 * Alice should call aliceProduceSecretKey().
 * After these three calls, Alice and Bob may call encrypt() and decrypt().
 * 
 */

package com.example.secretmessage.handler;

import javax.crypto.interfaces.*;
import java.util.HashMap;
import java.security.spec.*;
import javax.crypto.spec.*;
import java.util.Random;
import java.security.*;
import javax.crypto.*;

import java.io.*;


public class EncryptionHandler
{	
	public KeyStore keyStore = null;
	public static byte[] passHash;
	public static HashMap<String, KeyAgreement> agreeMap = null;
	private static EncryptionHandler instance = null;
	
	public static EncryptionHandler getInstance(String Password)
	{
		if (instance == null)
			instance = new EncryptionHandler(Password);
		return instance;
	}
	
	private EncryptionHandler(String Password)
	{
		loadKeyStore(Password);
		loadPassword();
		if (agreeMap == null)
			agreeMap = new HashMap<String, KeyAgreement>();
	}
	
	public byte[] aliceProducePublicKey(String recipient)
	{
		try {
			AlgorithmParameterGenerator generator = AlgorithmParameterGenerator.getInstance("DH");
			generator.init(512);
			DHParameterSpec spec = (DHParameterSpec) generator.generateParameters().getParameterSpec(DHParameterSpec.class);
			KeyPairGenerator pairGen = KeyPairGenerator.getInstance("DH");
			pairGen.initialize(spec);
			KeyPair keyPair = pairGen.generateKeyPair();
			KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
			keyAgree.init(keyPair.getPrivate());
			agreeMap.put(recipient, keyAgree);
			return keyPair.getPublic().getEncoded();
		} catch (Exception e) { return null; }
	}
	
	public byte[] bobProducePublicKeyAndSecretKey(byte[] alicePublicKeyBytes, String recipient, String Password)
	{
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("DH");
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(alicePublicKeyBytes);
			PublicKey alicePublicKey = keyFactory.generatePublic(keySpec);
			DHParameterSpec spec = ((DHPublicKey)alicePublicKey).getParams();
			KeyPairGenerator pairGen = KeyPairGenerator.getInstance("DH");
	        pairGen.initialize(spec);
	        KeyPair keyPair = pairGen.generateKeyPair();
	        KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
	        keyAgree.init(keyPair.getPrivate());
	        keyAgree.doPhase(alicePublicKey, true);
	        setKey(recipient, new SecretKeySpec(keyAgree.generateSecret(), "AES"), Password);
	        return keyPair.getPublic().getEncoded();
		} catch (Exception e) { return null; }
	}
	
	public boolean aliceProduceSecretKey(byte[] bobPublicKeyBytes, String recipient, String Password)
	{
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("DH");
			X509EncodedKeySpec spec = new X509EncodedKeySpec(bobPublicKeyBytes);
			PublicKey bobPublicKey = keyFactory.generatePublic(spec);
			agreeMap.get(recipient).doPhase(bobPublicKey, true);
			setKey(recipient, new SecretKeySpec(agreeMap.get(recipient).generateSecret(), "AES"), Password);
			agreeMap.remove(recipient);
			return true;
		} catch (Exception e) { return false; }
	}
	
	public byte[] encrypt(byte[] plainText, String recipient, int[] saltIndex, String Password)
	{
		try {
			byte[] key = getKey(recipient, Password).getEncoded();
			Random randy = new Random();
			saltIndex[0] = Math.abs(randy.nextInt() % 48);
			Cipher cipher = Cipher.getInstance("AES/CTR/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, 0, 16, "AES"), new IvParameterSpec(key, saltIndex[0], 16));
			return cipher.doFinal(plainText);
		} catch (Exception e) { return null; }
	}
	
	public String decrypt(byte[] cipherText, String recipient, int saltIndex, String Password)
	{
		try {
			byte[] key = getKey(recipient, Password).getEncoded();
			Cipher cipher = Cipher.getInstance("AES/CTR/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, 0, 16, "AES"), new IvParameterSpec(key, saltIndex, 16));
			return new String(cipher.doFinal(cipherText), "UTF8");
		} catch (Exception e) { return null; }
	}
	
	public boolean loadKeyStore(String Password)
	{
		FileInputStream fis = null;
		try {
			keyStore = KeyStore.getInstance("JCEKS");
			fis = new java.io.FileInputStream("KeyStore");
			keyStore.load(fis, Password.toCharArray());
		} catch (FileNotFoundException e) { try { keyStore.load(null, Password.toCharArray()); return true; } catch (Exception f) { return false;} }
		  catch (Exception f) { System.out.println(f.getMessage()); return false;}
		  finally { if (fis != null) try { fis.close(); } catch (Exception e) {} }
		return true;
	}
	
	public void saveKeyStore(String Password)
	{
		if (keyStore == null)
			return;
		FileOutputStream fos = null;
	    try {
	        fos = new java.io.FileOutputStream("KeyStore");
	        keyStore.store(fos, Password.toCharArray());
	    } catch (Exception e) {}
	      finally { if (fos != null) try { fos.close(); } catch (Exception e) {} }
	}
	
	public SecretKey getKey(String alias, String Password)
	{
		try {
			return ((KeyStore.SecretKeyEntry) keyStore.getEntry(alias, new KeyStore.PasswordProtection(Password.toCharArray()))).getSecretKey();
		} catch (Exception e) { System.out.println(e.getMessage()); return null; }
	}
	
	public boolean setKey(String alias, SecretKey key, String Password)
	{
		try {
			keyStore.setEntry(alias, new KeyStore.SecretKeyEntry(key), new KeyStore.PasswordProtection(Password.toCharArray()));
		} catch (Exception e) { System.out.println(e.getMessage()); return false; }
		return true;
	}
	
	public static boolean loadPassword()
	{
		try {
			RandomAccessFile file = new RandomAccessFile("password", "r");
			passHash = new byte[(int) file.length()];
			file.read(passHash);
			file.close();
		} catch (Exception e) { return false; }
		return true;
	}
	
	public static boolean savePassword(String Password)
	{
		try {
			MessageDigest hash = MessageDigest.getInstance("MD5");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(hash.digest(Password.getBytes("UTF8")), 0, 16, "AES"));
			passHash = cipher.doFinal(Password.getBytes("UTF8"));
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File("password")));
			bos.write(passHash);
			bos.flush();
			bos.close();
		} catch (Exception e) { return false; }
		return true;
	}
	
	public static boolean isPassword(String pass)
	{
		try {
			MessageDigest hash = MessageDigest.getInstance("MD5");
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(hash.digest(pass.getBytes("UTF8")), 0, 16, "AES"));
			byte[] passEnc = cipher.doFinal(pass.getBytes("UTF8"));
			return equals(passEnc, passHash);
		} catch (Exception e) { return false; }
	}
	
	public static boolean equals(byte[] a, byte[] b)
	{
		if (a.length != b.length)
			return false;
		for (int i = 0; i < a.length; i++)
			if (a[i] != b[i])
				return false;
		return true;
	}	
	
    private static void byte2hex(byte b, StringBuffer buf)
    {
        char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }
    
    private static String toHexString(byte[] block)
    {
        StringBuffer buf = new StringBuffer();
        int len = block.length;
        for (int i = 0; i < len; i++)
        {
             byte2hex(block[i], buf);
             if (i < len-1) {
                 buf.append(":");
             }
        } 
        return buf.toString();
    }
}