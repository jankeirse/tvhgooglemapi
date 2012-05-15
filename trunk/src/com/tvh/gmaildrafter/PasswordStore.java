/**
 * Copyright (C) 2012 TVH Group NV. <kalman.tiboldi@tvh.com>
 *
 * This file is part of the tvhgooglemapi project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tvh.gmaildrafter;

import java.io.*;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.util.Properties;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


public class PasswordStore {

    public static final String AES = "AES";
    private String keyFile;
    private String propsFile;
    private String propsFolder;
    
    /* We hardcode one half of the key in this file and generate one half and put one half in the filesystem to make it at least a little difficult to decode the password */
    /* Notice that this is still insecure, because the .class can be decompiled to find out the salt. */
    private final static String salt = "B9CF54D1B4FD8781"; // This should be a 16 character uppercase HEX string
    
    /**
     * encrypt a value and generate a keyfile if the keyfile is not found then a
     * new one is created
     *
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public static String encrypt(String value, File keyFile)
            throws GeneralSecurityException, IOException {
        if (!keyFile.exists()) {
            KeyGenerator keyGen = KeyGenerator.getInstance(PasswordStore.AES);
            keyGen.init(128);
            SecretKey sk = keyGen.generateKey() ;
            try {
                FileWriter fw = new FileWriter(keyFile);
                fw.write(byteArrayToHexString(sk.getEncoded()));
                fw.flush();
            } catch (Exception e){
                
            }
        }

        SecretKeySpec sks = getSecretKeySpec(keyFile);
        Cipher cipher = Cipher.getInstance(PasswordStore.AES);       
        cipher.init(Cipher.ENCRYPT_MODE, sks, cipher.getParameters());
        byte[] encrypted = cipher.doFinal(value.getBytes());
        return byteArrayToHexString(encrypted);
    }

    /**
     * decrypt a value
     *
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public static String decrypt(String message, File keyFile)
            throws GeneralSecurityException, IOException {
        SecretKeySpec sks = getSecretKeySpec(keyFile);
        Cipher cipher = Cipher.getInstance(PasswordStore.AES);
        cipher.init(Cipher.DECRYPT_MODE, sks);
        byte[] decrypted = cipher.doFinal(hexStringToByteArray(message));
        return new String(decrypted);
    }
    
    private static SecretKeySpec getSecretKeySpec(File keyFile)
            throws NoSuchAlgorithmException, IOException {
        byte[] key = readKeyFile(keyFile);
        SecretKeySpec sks = new SecretKeySpec(key,  PasswordStore.AES);
        return sks;
    }

    private static byte[] readKeyFile(File keyFile)
            throws FileNotFoundException {
        String keyValue = "";
        try  {
            Scanner scanner = new Scanner(keyFile).useDelimiter("\\Z");
            keyValue = scanner.next();
        } catch (Exception e) {
            
        }
        keyValue = keyValue.substring(0,16);
        keyValue+= salt; /* we replace one half of the key with our own key to 
                          * confuse people trying to decode the password.
                          */
                                
        return hexStringToByteArray(keyValue);
    }

    private static String byteArrayToHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            int v = b[i] & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase();
    }

    private static byte[] hexStringToByteArray(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    public PasswordStore() {
        propsFolder = System.getProperty("user.home") + "/.gmaildrafter";
        keyFile = System.getProperty("user.home") + "/.gmaildrafter/key";
        propsFile = System.getProperty("user.home") + "/.gmaildrafter/props";
    }

    public void storeLogin(Credentials credentials) {

        try {
            File propsFolderReference = new File(propsFolder);
            if (!propsFolderReference.exists()) {
                if (!propsFolderReference.mkdir()) {
                    return;
                }
            }
            Properties props = new Properties();
            props.put("username",credentials.getUsername());
            props.put("password", PasswordStore.encrypt(credentials.getPassword(), new File(keyFile)));
            props.store(new FileWriter(propsFile), "");           

        } catch (Exception ex) {
        }

    }

    public Credentials getStoredLogin() {

        try {
            Properties props = new Properties();
            props.load(new FileReader(propsFile));
            String username = props.getProperty("username");
            String password = PasswordStore.decrypt(props.getProperty("password"), new File(keyFile));
            return new Credentials(username, password);

        } catch (Exception ex) {
            return null; // credentials not stored
        }

    }

    public static void main(String[] args) throws Exception {
        final String KEY_FILE = "c:/temp/howto.key";
        final String PWD_FILE = "c:/temp/howto.properties";

        String clearPwd = "my password is hello world";

        Properties p1 = new Properties();

        p1.put("user", "Real");
        String encryptedPwd = PasswordStore.encrypt(clearPwd, new File(KEY_FILE));
        p1.put("pwd", encryptedPwd);
        p1.store(new FileWriter(PWD_FILE), "");

        // ==================
        Properties p2 = new Properties();

        p2.load(new FileReader(PWD_FILE));
        encryptedPwd = p2.getProperty("pwd");
        System.out.println(encryptedPwd);
        System.out.println(PasswordStore.decrypt(encryptedPwd, new File(KEY_FILE)));
    }
}
