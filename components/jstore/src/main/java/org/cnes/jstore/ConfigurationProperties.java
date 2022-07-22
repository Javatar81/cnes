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
	@ConfigProperty(name = "org.cnes.jstore.log.async.enabled", defaultValue = "false")
	boolean logAsync;
	@ConfigProperty(name = "org.cnes.jstore.log.async.flush-time", defaultValue = "1000")
	int asyncFlushTime;

	@Generated("SparkTools")
	private ConfigurationProperties(Builder builder) {
		this.storeDir = builder.storeDir;
		this.storeArchivePattern = builder.storeArchivePattern;
		this.logAsync = builder.logAsync;
		this.asyncFlushTime = builder.asyncFlushTime;
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
	
	public boolean isLogAsync() {
		return logAsync;
	}

	public int getAsyncFlushTime() {
		return asyncFlushTime;
	}

	@Override
	public String toString() {
		return String.format(
				"ConfigurationProperties [storeDir=%s, storeArchivePattern=%s, logAsync=%s, asyncFlushTime=%s]",
				storeDir, storeArchivePattern, logAsync, asyncFlushTime);
	}

	@Generated("SparkTools")
	public static Builder builder() {
		return new Builder();
	}

	@Generated("SparkTools")
	public static final class Builder {
		private String storeDir;
		private String storeArchivePattern;
		private boolean logAsync;
		private int asyncFlushTime;

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

		public Builder withLogAsync(boolean logAsync) {
			this.logAsync = logAsync;
			return this;
		}

		public Builder withAsyncFlushTime(int asyncFlushTime) {
			this.asyncFlushTime = asyncFlushTime;
			return this;
		}

		public ConfigurationProperties build() {
			return new ConfigurationProperties(this);
		}
	}

	
	
}
