package org.cnes.jstore.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.codec.digest.DigestUtils;
import org.cnes.jstore.store.LogbackFileStoreWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Identifier implements Serializable {
	private static final Logger LOGGER = LoggerFactory.getLogger(LogbackFileStoreWriter.class);
	private static final long serialVersionUID = 1L;
	
	@JsonProperty("value")
	private final String value;
	
	@JsonCreator
	private Identifier(@JsonProperty("value") String value) {
		this.value = value; 
	}
	
	static Identifier create(long created, String data, Optional<Identifier> predecessor) {
		StringBuilder contentToHash = new StringBuilder();
		contentToHash.append(created);
		contentToHash.append(";");
		contentToHash.append(data);
		contentToHash.append(";");
		contentToHash.append(predecessor.orElse(null));
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Calculating hash with %d;%s;%s", created, data, predecessor.orElse(null)));
		}
		return new Identifier(DigestUtils.sha1Hex(contentToHash.toString()));
	}
	
	public String getValue() {
		return value;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Identifier other = (Identifier) obj;
		return Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		return value;
	}

}
