package org.cnes.jstore.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Identifier implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@JsonProperty("value")
	private String value;
	
	@JsonCreator
	private Identifier(@JsonProperty("value") String value) {
		this.value = value; 
	}
	
	static Identifier create(LocalDateTime created, String data, Optional<Identifier> predecessor) {
		StringBuilder contentToHash = new StringBuilder();
		contentToHash.append(Timestamp.valueOf(created));
		contentToHash.append(";");
		contentToHash.append(data);
		contentToHash.append(";");
		contentToHash.append(predecessor.orElse(null));
		return new Identifier(DigestUtils.sha1Hex(contentToHash.toString()));
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
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
