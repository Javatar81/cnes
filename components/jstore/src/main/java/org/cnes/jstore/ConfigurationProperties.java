package org.cnes.jstore;

import javax.annotation.Generated;
import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class ConfigurationProperties {
	@ConfigProperty(name = "org.cnes.jstore.store-path", defaultValue = "src/test/resources/")
	String storeDir;
	@ConfigProperty(name = "org.cnes.jstore.store-archive-pattern", defaultValue = "%d{yyyy-MM-dd}")
	String storeArchivePattern;

	private ConfigurationProperties(Builder builder) {
		this.storeDir = builder.storeDir;
		this.storeArchivePattern = builder.storeArchivePattern;
	}
	
	public ConfigurationProperties() {
		super();
	}
	
	public String getStoreDir() {
		return storeDir;
	}

	public String getStoreArchivePattern() {
		return storeArchivePattern;
	}

	@Override
	public String toString() {
		return String.format("ConfigurationProperties [storeDir=%s, storeArchivePattern=%s]", storeDir,
				storeArchivePattern);
	}

	@Generated("SparkTools")
	public static Builder builder() {
		return new Builder();
	}

	@Generated("SparkTools")
	public static final class Builder {
		private String storeDir;
		private String storeArchivePattern;

		private Builder() {
		}

		public Builder withStoreDir(String storeDir) {
			this.storeDir = storeDir;
			return this;
		}

		public Builder withStoreArchivePattern(String storeArchivePattern) {
			this.storeArchivePattern = storeArchivePattern;
			return this;
		}

		public ConfigurationProperties build() {
			return new ConfigurationProperties(this);
		}
	}

	
	
}
