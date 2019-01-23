package com.example.pc.byebyecash.utils;

import com.example.pc.byebyecash.MainActivity;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;

public class Encrypytion {

    String encryptedPassword;
    String decryptedPassword;
    private static final String SECRETKEY = "midokawaspazzword";

   public String encrypt (String toBeEncrypted){

       try {
           encryptedPassword = AESCrypt.encrypt(SECRETKEY,toBeEncrypted);
       } catch (GeneralSecurityException e) {
           e.printStackTrace();
       }

       return encryptedPassword;

   }

   public String decrypt (String toBeDecrypted){

       try {
           decryptedPassword = AESCrypt.decrypt(SECRETKEY,toBeDecrypted);
       } catch (GeneralSecurityException e) {
           e.printStackTrace();
       }

       return decryptedPassword;

   }

}
