package com.lingtuan.firefly.util;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/**
 * @author alun (http://alunblog.duapp.com)
 * @version 1.0
 */
public class Rsa {
    private static final String RSA_PUBLICE =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCphjph5VHgfB8MFIGJSMHIAE+WnJ1BsiHKxtaJzoAb0v/IG+5w0fQ2713BbdEVoHtvMXtqTtluStj009ZOPkjiTm/fKlhx9hO5I6bct4r8Y70HDSmHjnNZm7sKHtxdbtAf65/+0xTsC0g8/hiq9wjD8a8KxbvKVxE/n5JluWllFQIDAQAB";
    private static final String ALGORITHM = "RSA";
 
    private static final int max_byte_size =1024/8-11;
    /**
     * To get the public key
     * @param algorithm
     * @param bysKey
     * @return
     */
    private static PublicKey getPublicKeyFromX509(String algorithm,
                                                  String bysKey) throws Exception {
        byte[] decodedKey = Base64.decode(bysKey, Base64.DEFAULT);
        X509EncodedKeySpec x509 = new X509EncodedKeySpec(decodedKey);
 
        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
        return keyFactory.generatePublic(x509);
    }
 
    /**
     * Use of public key encryption
     * @param content
     * @param key
     * @return
     */
    public static String encryptByPublic(String content) {
        try {
            PublicKey pubkey = getPublicKeyFromX509(ALGORITHM, RSA_PUBLICE);
 
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, pubkey);
 
            byte plaintext[] = content.getBytes("UTF-8");
            byte[] buffer = new byte[max_byte_size];
            ByteArrayInputStream inputStream=new ByteArrayInputStream(plaintext);
            ByteArrayOutputStream writer = new ByteArrayOutputStream();
            int len = -1;
            while((len = inputStream.read(buffer)) != -1){
            	byte[] block = null;  
                if (len == max_byte_size) {  
                block = buffer;  
                } else {  
                block = new byte[len];  
                for (int i = 0; i < len; i++) {  
                    block[i] = buffer[i];  
                }  
                } 
                byte[] output = cipher.doFinal(block);
                writer.write(output);
            }
            String s = new String(Base64.encode(writer.toByteArray(), Base64.DEFAULT));
            writer.close();
            inputStream.close();
            return s;
 
        } catch (Exception e) {
            return null;
        }
    } 
    
    
    /**
     * using the public key
     * @ param content ciphertext
     * @ param key merchants private key
     * @ return decrypted string
     */  
     public static String decryptByPublic(String content) {
         try {  
             PublicKey pubkey = getPublicKeyFromX509(ALGORITHM, RSA_PUBLICE);
             Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
             cipher.init(Cipher.DECRYPT_MODE, pubkey);
             ByteArrayInputStream ins = new ByteArrayInputStream(Base64.decode(content, Base64.DEFAULT));
             ByteArrayOutputStream writer = new ByteArrayOutputStream();
             byte[] buf = new byte[128];  
             int bufl;  
             while ((bufl = ins.read(buf)) != -1) {  
                 byte[] block = null;  
                 if (buf.length == bufl) {  
                 block = buf;  
                 } else {  
                 block = new byte[bufl];  
                 for (int i = 0; i < bufl; i++) {  
                     block[i] = buf[i];  
                 }  
                 }  
                 writer.write(cipher.doFinal(block));  
             } 
             String text = new String(writer.toByteArray(), "utf-8");
             ins.close();
             writer.close();
             
             return text;
         } catch (Exception e) {
             return null;  
         }  
     }  
}
