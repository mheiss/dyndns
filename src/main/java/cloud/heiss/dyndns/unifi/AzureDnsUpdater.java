package cloud.heiss.dyndns.unifi;

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
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.dns.models.ARecordSet;
import com.azure.resourcemanager.dns.models.ARecordSets;
import com.azure.resourcemanager.dns.models.DnsZone;

/**
 * Performs the update of the DNS zone entry.
 */
public class AzureDnsUpdater {

    private final static Logger logger = LoggerFactory.getLogger(AzureDnsUpdater.class);

    private final AzureConfigDto config;

    public AzureDnsUpdater(AzureConfigDto config) {
        this.config = config;
    }

    /**
     * Connects to AZURE and updates the DNS entry.
     * 
     * @param iPv4Address the IP address to set in the DNS record
     * @return {@code true} if the IP has been updated and {@code false} if no action was required
     */
    public boolean update(String iPv4Address) {
        logger.info("Connecting to AZURE.");

        TokenCredential credential = new ClientSecretCredentialBuilder().clientId(config.appId()).clientSecret(config.appKey())
                .tenantId(config.tenantId()).build();
        AzureProfile profile = new AzureProfile(config.tenantId(), config.subscriptionId(), AzureEnvironment.AZURE);

        // Connect and authenticate using the provided application and key
        AzureResourceManager azure = AzureResourceManager.configure().authenticate(credential, profile).withDefaultSubscription();

        // Contains record names with an old IP-address
        Set<String> toUpdate = new TreeSet<>(config.recordNames());

        // Check if the IP is up-2-date
        DnsZone dnsZone = azure.dnsZones().getByResourceGroup(config.resourceGroup(), config.zoneName());
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
            logger.info("{}: IPv4 address of DNS zone successfully updated.", name);
        }
        return true;
    }

}
