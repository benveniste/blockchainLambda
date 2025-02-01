package com.smofs.blockchain;

import org.hyperledger.fabric.sdk.Enrollment;

import java.security.PrivateKey;

/**
 * FabricEnrollment class holding the private key and certificate of a Fabric User
 */
public class FabricEnrollment implements Enrollment {
    private final PrivateKey key;
    private final String cert;

    public FabricEnrollment(PrivateKey key, String cert) {
        this.key = key;
        this.cert = cert;
    }

    @Override
    public PrivateKey getKey() {
        return key;
    }

    @Override
    public String getCert() {
        return cert;
    }
}
