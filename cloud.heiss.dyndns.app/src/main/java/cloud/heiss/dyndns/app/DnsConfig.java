package cloud.heiss.dyndns.app;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Simple object holding the configuration.
 */
public class DnsConfig {

	/**
	 * The ID of the Azure Active Directory.
	 * <p>
	 * Azure Portal -> Azure Active Directory -> Properties -> Directory ID
	 * </p>
	 */
	public final String tenantId;

	/**
	 * The ID of the application that is allowed to connect and update the DNS
	 * <p>
	 * Azure Portal -> Azure Active Directory -> App registrations
	 * </p>
	 */
	public final String clientId;

	/**
	 * The KEY that has been used to create the application.
	 */
	public final String key;

	/**
	 * The name of the resource group containing the DNS Zone.
	 */
	public final String resourceGroup;

	/**
	 * The name of the DNS zone to update.
	 */
	public final String zoneName;

	/**
	 * Comma separated list of record names to update.
	 */
	public final Set<String> recordNames;

	/**
	 * The URL to call in order to get the IP address.
	 */
	public final String ipServiceUrl;

	/**
	 * Schedules the update to run every xx minutes
	 */
	public final int updateIntervalInMinutes;

	/**
	 * Creates a new instance by reading the given JSON file
	 */
	public DnsConfig(File file) throws IOException {
		JsonParser parser = new JsonParser();
		try (FileReader reader = new FileReader(file)) {
			JsonObject object = parser.parse(reader).getAsJsonObject();
			tenantId = object.get("tenantId").getAsString();
			clientId = object.get("clientId").getAsString();
			key = object.get("key").getAsString();
			resourceGroup = object.get("resourceGroup").getAsString();
			zoneName = object.get("zoneName").getAsString();
			recordNames = new TreeSet<>(Arrays.asList(object.get("recordNames").getAsString().split(",")));
			ipServiceUrl = object.get("ipServiceUrl").getAsString();
			if (object.has("updateIntervalInMinutes")) {
				updateIntervalInMinutes = object.get("updateIntervalInMinutes").getAsInt();
			} else {
				updateIntervalInMinutes = -1;
			}
		}
	}

}
