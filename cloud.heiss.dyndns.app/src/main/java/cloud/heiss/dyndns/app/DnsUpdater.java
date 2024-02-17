package cloud.heiss.dyndns.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.dns.models.ARecordSet;
import com.azure.resourcemanager.dns.models.ARecordSets;
import com.azure.resourcemanager.dns.models.DnsZone;
import com.azure.identity.ClientSecretCredentialBuilder;

/**
 * Performs the update of the DNS zone entry.
 */
public class DnsUpdater {

	private final static Logger logger = LoggerFactory.getLogger(DnsUpdater.class);

	private final DnsConfigDto config;

	public DnsUpdater(DnsConfigDto config) {
		this.config = config;
	}

	/**
	 * Connects to AZURE and updates the DNS entry.
	 * 
	 * @return {@code true} if the IP has been updated and {@code false} if no
	 *         action was required
	 */
	public boolean update() throws Exception {
		logger.info("Connecting to AZURE.");

		TokenCredential credential = new ClientSecretCredentialBuilder()
				.clientId(config.clientId)
				.clientSecret(config.key)
				.tenantId(config.tenantId)
				.build();
		AzureProfile profile = new AzureProfile(config.tenantId, config.subscriptionId, AzureEnvironment.AZURE);

		// Connect and authenticate using the provided application and key
		AzureResourceManager azure = AzureResourceManager.configure()
				.authenticate(credential, profile)
				.withDefaultSubscription();

		// Determine the current external IP
		String iPv4Address = determineIPv4Address(config.ipServiceUrl);
		logger.info("Resolved address: {} ", iPv4Address);

		// Contains record names with an old IP-address
		Set<String> toUpdate = new TreeSet<>(config.recordNames);

		// Check if the IP is up-2-date
		DnsZone dnsZone = azure.dnsZones().getByResourceGroup(config.resourceGroup, config.zoneName);
		ARecordSets recordSets = dnsZone.aRecordSets();
		for (ARecordSet set : recordSets.list()) {
			String name = set.name();
			if (!toUpdate.contains(name)) {
				continue;
			}
			for (String address : set.ipv4Addresses()) {
				if (address.equalsIgnoreCase(iPv4Address)) {
					logger.info("{}: IPv4 address and DNS address are matching. Nothing to do.", name);
					toUpdate.remove(name);
					break;
				}
			}
		}
		if (toUpdate.isEmpty()) {
			return false;
		}

		// Update or create the entry
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		long ttl = TimeUnit.MINUTES.toSeconds(5);
		for (String name : toUpdate) {
			DnsZone.Update update = dnsZone.update();
			update.defineARecordSet(name).withIPv4Address(iPv4Address).withTimeToLive(ttl).attach().apply();
			update.defineTxtRecordSet(name).withText("lastUpdate=" + format.format(new Date())).attach().apply();
			logger.info("{}: IPv4 address of DNS zone successfully updated.",name);
		}
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
