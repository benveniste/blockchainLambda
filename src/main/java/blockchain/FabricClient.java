package blockchain;

import aws.Secrets;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Properties;

public class FabricClient {
    public HFCAClient client;

    public synchronized HFCAClient getClient(LambdaLogger logger) {
        if (client != null) {
            return client;
        }
        logger.log("Setting up new client");
        String tlsCertAsString = Secrets.getSecret("WSFS-Blockchain-TLS");
        Properties caProperties = new Properties();
        Map<String, String> secretMap = Secrets.getSecretMap("WSFS-Blockchain");
        caProperties.put("pemBytes", tlsCertAsString.getBytes());
        HFCAClient caClient = createHFCAClient(caProperties, secretMap);
        return client;
    }

    private HFCAClient createHFCAClient(Properties caClientProperties, Map<String, String> secretMap) {
        try {
            CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
            HFCAClient caClient = HFCAClient.createNewInstance(secretMap.get("CAEndpoint"), caClientProperties);
            caClient.setCryptoSuite(cryptoSuite);
            return caClient;
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException | CryptoException |
                 InvalidArgumentException | NoSuchMethodException | InvocationTargetException |
                 MalformedURLException oops) {
            throw new RuntimeException("Error creating Fabric CA Client", oops);
        }
    }
}
