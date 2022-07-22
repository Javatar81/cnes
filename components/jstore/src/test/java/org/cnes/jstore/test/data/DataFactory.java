package org.cnes.jstore.test.data;

public class DataFactory {

	public DataFactory() {
		
	}
	
	public String generateMediumPayload() {
		return "{\n"
				+ "  \"kind\": \"List\",\n"
				+ "  \"items\":[\n"
				+ "    {\n"
				+ "      \"kind\":\"None\",\n"
				+ "      \"metadata\":{\"name\":\"127.0.0.1\"},\n"
				+ "      \"status\":{\n"
				+ "        \"capacity\":{\"cpu\":\"4\"},\n"
				+ "        \"addresses\":[{\"type\": \"LegacyHostIP\", \"address\":\"127.0.0.1\"}]\n"
				+ "      }\n"
				+ "    },\n"
				+ "    {\n"
				+ "      \"kind\":\"None\",\n"
				+ "      \"metadata\":{\"name\":\"127.0.0.2\"},\n"
				+ "      \"status\":{\n"
				+ "        \"capacity\":{\"cpu\":\"8\"},\n"
				+ "        \"addresses\":[\n"
				+ "          {\"type\": \"LegacyHostIP\", \"address\":\"127.0.0.2\"},\n"
				+ "          {\"type\": \"another\", \"address\":\"127.0.0.3\"}\n"
				+ "        ]\n"
				+ "      }\n"
				+ "    }\n"
				+ "  ],\n"
				+ "  \"users\":[\n"
				+ "    {\n"
				+ "      \"name\": \"myself\",\n"
				+ "      \"user\": {}\n"
				+ "    },\n"
				+ "    {\n"
				+ "      \"name\": \"e2e\",\n"
				+ "      \"user\": {\"username\": \"admin\", \"password\": \"secret\"}\n"
				+ "    }\n"
				+ "  ]\n"
				+ "}";
	}

}
