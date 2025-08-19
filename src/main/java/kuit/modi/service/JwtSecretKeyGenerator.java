package kuit.modi.service;

import io.jsonwebtoken.io.Encoders;
import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;

public class JwtSecretKeyGenerator {
    public static void main(String[] args) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("HmacSha256");
        keyGen.init(256); // 256비트 키 생성
        SecretKey secretKey = keyGen.generateKey();

        String base64Key = Encoders.BASE64.encode(secretKey.getEncoded());
        System.out.println("Generated Secret Key: " + base64Key);
    }
}
