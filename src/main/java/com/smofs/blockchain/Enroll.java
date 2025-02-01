package com.smofs.blockchain;

import com.smofs.aws.Secrets;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.hyperledger.fabric.sdk.Enrollment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

public class Enroll {
    public static FabricUser user(LambdaLogger logger) {
        String pkAsString = Secrets.getSecret("WSFS-Blockchain-Signer");
        pkAsString = pkAsString.replace("-----BEGIN PRIVATE KEY-----", "");
        pkAsString = pkAsString.replace("-----END PRIVATE KEY-----", "");
        pkAsString = pkAsString.replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(pkAsString);
        PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(keyBytes);

        KeyFactory keyFactory;
        PrivateKey privateKey;
        try {
            keyFactory = KeyFactory.getInstance("EC");
            privateKey = keyFactory.generatePrivate(priPKCS8);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException oops) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();  PrintStream ps = new PrintStream(baos)) {
                oops.printStackTrace(ps);
                logger.log(oops.getMessage() + System.lineSeparator() + baos.toString(StandardCharsets.UTF_8));
            } catch (IOException inconceivable) {
                logger.log(oops.getMessage());
            }
            throw new RuntimeException(oops);
        }

        String certString = Secrets.getSecret("WSFS-Blockchain-Identity");
        Map<String, String> secretMap = Secrets.getSecretMap("WSFS-Blockchain");
        Enrollment enrollment = new FabricEnrollment(privateKey, certString);
        return new FabricUser(secretMap.get("AdminName"), secretMap.get("MSP_Name"), secretMap.get("MSP_ID"), enrollment);
    }
}
