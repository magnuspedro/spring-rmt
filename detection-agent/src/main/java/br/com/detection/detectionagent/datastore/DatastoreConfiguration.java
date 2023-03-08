package br.com.detection.detectionagent.datastore;

import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public interface DatastoreConfiguration extends Serializable{

	String getHost();

	int getPort();

	String getDatabaseName();

	default Collection<ServerAddress> getServerAddress() {
		return Arrays.asList(new ServerAddress(this.getHost(), this.getPort()));
	}

	default Collection<MongoCredential> getCredentials() {
		return Collections.emptyList();
	}

}
