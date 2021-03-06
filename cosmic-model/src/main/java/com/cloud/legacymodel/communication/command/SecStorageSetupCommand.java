package com.cloud.legacymodel.communication.command;

import com.cloud.legacymodel.auth.Certificates;
import com.cloud.legacymodel.to.DataStoreTO;

public class SecStorageSetupCommand extends Command {
    private DataStoreTO store;
    private String secUrl;
    private Certificates certs;
    private String postUploadKey;

    public SecStorageSetupCommand() {
        super();
    }

    public SecStorageSetupCommand(final DataStoreTO store, final String secUrl, final Certificates certs) {
        super();
        this.secUrl = secUrl;
        this.certs = certs;
        this.store = store;
    }

    @Override
    public boolean executeInSequence() {
        return true;
    }

    public String getSecUrl() {
        return secUrl;
    }

    public void setSecUrl(final String secUrl) {
        this.secUrl = secUrl;
    }

    public Certificates getCerts() {
        return certs;
    }

    public DataStoreTO getDataStore() {
        return store;
    }

    public void setDataStore(final DataStoreTO store) {
        this.store = store;
    }

    public String getPostUploadKey() {
        return postUploadKey;
    }

    public void setPostUploadKey(final String postUploadKey) {
        this.postUploadKey = postUploadKey;
    }
}
