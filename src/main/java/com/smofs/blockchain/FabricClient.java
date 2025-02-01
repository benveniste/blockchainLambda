package com.smofs.blockchain;

import com.smofs.aws.Secrets;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Properties;

public class FabricClient {
    private HFCAClient caClient;
    private HFClient client;

    public synchronized HFClient getClient(LambdaLogger logger) {
        if (client == null) {
            logger.log("Setting up new client");
            String tlsCertAsString = Secrets.getSecret("WSFS-Blockchain-TLS");
            Properties caProperties = new Properties();
            Map<String, String> secretMap = Secrets.getSecretMap("WSFS-Blockchain");
            caProperties.put("pemBytes", tlsCertAsString.getBytes());
            caClient = createHFCAClient(caProperties, secretMap);
            client = createHFClient();
        }
        return client;
    }

    public HFCAClient getCaClient() {
        return caClient;
    }

    private synchronized HFCAClient createHFCAClient(Properties caClientProperties, Map<String, String> secretMap) {
        if (caClient == null) {
            try {
                CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
                caClient = HFCAClient.createNewInstance(secretMap.get("CAEndpoint"), caClientProperties);
                caClient.setCryptoSuite(cryptoSuite);
            } catch (IllegalAccessException | InstantiationException | ClassNotFoundException | CryptoException |
                     InvalidArgumentException | NoSuchMethodException | InvocationTargetException |
                     MalformedURLException oops) {
                throw new RuntimeException("Error creating Fabric CA Client", oops);
            }
        }
        return caClient;
    }

    private static HFClient createHFClient() {
        try {
            CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
            HFClient client = HFClient.createNewInstance();
            client.setCryptoSuite(cryptoSuite);
            return client;
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException | CryptoException
                 | InvalidArgumentException | NoSuchMethodException | InvocationTargetException oops) {
            throw new RuntimeException("Error creating Fabric CA Client", oops);
        }
    }
}
