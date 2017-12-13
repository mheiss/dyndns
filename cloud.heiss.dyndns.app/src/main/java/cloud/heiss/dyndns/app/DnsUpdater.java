package cloud.heiss.dyndns.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.dns.ARecordSet;
import com.microsoft.azure.management.dns.ARecordSets;
import com.microsoft.azure.management.dns.DnsZone;

/**
 * Performs the update of the DNS zone entry.
 */
public class DnsUpdater {

	private final DnsConfig config;

	public DnsUpdater(DnsConfig config) {
		this.config = config;
	}

	/**
	 * Connects to AZURE and updates the DNS entry.
	 * 
	 * @return {@code true} if the IP has been updated and {@code false} if no
	 *         action was required
	 */
	public boolean update() throws Exception {
		System.out.println("Connecting to AZURE.");

		// Connect and authenticate using the provided application and key
		ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(config.clientId, config.tenantId,
				config.key, AzureEnvironment.AZURE);
		Azure azure = Azure.authenticate(credentials).withDefaultSubscription();

		// Determine the current external IP
		String iPv4Address = determineIPv4Address(config.ipServiceUrl);
		System.out.println("Resolved address: " + iPv4Address);

		// Check if the IP is up-2-date
		DnsZone dnsZone = azure.dnsZones().getByResourceGroup(config.resourceGroup, config.zoneName);
		ARecordSets recordSets = dnsZone.aRecordSets();
		for (ARecordSet set : recordSets.list()) {
			if (!set.name().equals("@")) {
				continue;
			}
			for (String address : set.ipv4Addresses()) {
				if (address.equalsIgnoreCase(iPv4Address)) {
					System.out.println("IPv4 address and DNS address are matching. Nothing to do.");
					return false;
				}
			}
		}

		// Update or create the entry
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		long ttl = TimeUnit.MINUTES.toSeconds(5);
		dnsZone.update().defineARecordSet("@").withIPv4Address(iPv4Address).withTimeToLive(ttl).attach().apply();
		dnsZone.update().defineTxtRecordSet("@").withText("lastUpdate=" + format.format(new Date())).attach().apply();
		System.out.println("IPv4 address of DNS zone successfully updated.");
		return true;
	}

	/** Call our own service to determine the IP */
	private String determineIPv4Address(String serviceUrl) throws Exception {
		URL myIp = new URL(serviceUrl);
		try (BufferedReader in = new BufferedReader(new InputStreamReader(myIp.openStream()))) {
			return in.readLine();
		}
	}

}
