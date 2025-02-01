package com.smofs.blockchain;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.smofs.aws.Secrets;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.TransactionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

public class PeerChannel {
    private static final String CHANNEL_NAME = "mychannel";

    private final LambdaLogger logger;

    public Channel channelD;

    public PeerChannel(LambdaLogger logger) {
        this.logger = logger;
    }

    public synchronized Channel open(HFClient client) {
        if (channelD == null) {
            Map<String, String> secretMap = Secrets.getSecretMap("WSFS-Blockchain");
            Properties properties = new Properties();
            properties.put("pemBytes", Secrets.getSecret("WSFS-Blockchain-TLS").getBytes());

            properties.setProperty("sslProvider", "openSSL");
            properties.setProperty("negotiationType", "TLS");

            // Configure Peer
            Peer peer;
            try {
                String grpcURL = "grpcs://" + secretMap.get("PeerEndpoint");
                peer = client.newPeer(secretMap.get("PeerHostname"), grpcURL, properties);
            } catch (InvalidArgumentException oops) {
                logger.log("Failed to create peer.  See a urologist?");
                throw new RuntimeException(oops);
            }

            // Configure Orderer
            Orderer orderer;
            try {
                String grpcURL = "grpcs://" + secretMap.get("OrdererEndpoint");
                orderer = client.newOrderer(secretMap.get("NetworkID"), grpcURL, properties);
            } catch (InvalidArgumentException oops) {
                logger.log("Failed to create orderer.  Hire new CEO?");
                throw new RuntimeException(oops);
            }

            // Open Channel D or cry U.N.C.L.E.
            channelD = client.getChannel(CHANNEL_NAME);
            try {
                if (channelD == null) {
                    channelD = client.newChannel(CHANNEL_NAME);
                }
                channelD.addPeer(peer);
                channelD.addOrderer(orderer);
                channelD.initialize();
            } catch (InvalidArgumentException | TransactionException oops) {
                logger.log("Failed to create channel.  We've been SMERSHed.");
                throw new RuntimeException(oops);
            }
        }
        return channelD;
    }

    public Channel wire() {
        try {
            FabricClient fabricClient = new FabricClient();
            FabricUser user = Enroll.user(logger);
            HFClient client = fabricClient.getClient(logger);
            client.setUserContext(user);
            return open(client);
        } catch (InvalidArgumentException | RuntimeException oops) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos)) {
                oops.printStackTrace(ps);
                logger.log(oops.getMessage() + System.lineSeparator() + baos.toString(StandardCharsets.UTF_8));
            } catch (IOException inconceivable) {
                logger.log(oops.getMessage());
            }
            throw new RuntimeException(oops);
        }

    }
}
