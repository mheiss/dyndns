package cloud.heiss.dyndns.unifi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.microprofile.config.inject.ConfigProperty;
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

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

/**
 * Performs the update of the DNS zone entry.
 */
@ApplicationScoped
public class AzureDnsUpdater {

    private final static Logger logger = LoggerFactory.getLogger(AzureDnsUpdater.class);

    @Inject
    AzureConfigDto config;

    @ConfigProperty(name = "quarkus.http.port", defaultValue = "8080")
    int httpPort;

    void onStart(@Observes StartupEvent ev) {
        logger.info("#############################");
        logger.info("### UniFi DynDNS Updater  ###");
        logger.info("#############################");
        logger.info("Endpoint: http://localhost:{}/nic/update", httpPort);

        AzureResourceManager manager = getResourceManager();
        DnsZone dnsZone = getDnsZone(manager);
        Stream<ARecordSet> recordSets4 = dnsZone.aRecordSets().list().stream();
        String aRecords = recordSets4.map(ipv4 -> ipv4.name() + " -> " + ipv4.ipv4Addresses()).collect(Collectors.joining(", "));

        logger.info("Successfully authenticated with Azure.");
        logger.info(" Tenant: '{}'", config.tenantId());
        logger.info(" Subscription: '{}'", config.subscriptionId());
        logger.info(" Resource group: '{}'", config.resourceGroup());
        logger.info("DNS zone: '{}'", config.zoneName());
        logger.info(" A-records: {}", aRecords);
    }

    /**
     * Connects to AZURE and updates the DNS entry.
     * 
     * @param iPv4Address the IP address to set in the DNS record
     */
    public void update(String iPv4Address) {
        // Query the configured record names to update
        Set<String> toUpdate4 = new TreeSet<>(config.recordNames4().orElse(new ArrayList<>()));

        // Check if the IP is up-2-date
        DnsZone dnsZone = getDnsZone(getResourceManager());
        ARecordSets recordSets4 = dnsZone.aRecordSets();
        for (ARecordSet set : recordSets4.list()) {
            String name = set.name();
            if (!toUpdate4.contains(name)) {
                continue;
            }
            for (String address : set.ipv4Addresses()) {
                if (address.equalsIgnoreCase(iPv4Address)) {
                    logger.info("{}: IPv4 address and DNS address are matching. Nothing to do.", name);
                    toUpdate4.remove(name);
                    break;
                }
            }
        }
        if (toUpdate4.isEmpty()) {
            logger.info("All DNS entries are up-2-date. Nothing to do.");
            return;
        }

        // Update or create the entry
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        long ttl = TimeUnit.MINUTES.toSeconds(5);
        DnsZone.Update update = dnsZone.update();
        for (String name : toUpdate4) {
            update.defineARecordSet(name).withIPv4Address(iPv4Address).withTimeToLive(ttl).attach();
            update.defineTxtRecordSet(name).withText("lastUpdate=" + format.format(new Date())).attach();
            logger.info("{}: IPv4 address of DNS zone updated.", name);
        }
        update.apply();
    }

    /**
     * Returns the resource manager.
     */
    private AzureResourceManager getResourceManager() {
        TokenCredential credential = new ClientSecretCredentialBuilder().clientId(config.appId()).clientSecret(config.appKey())
                .tenantId(config.tenantId()).build();
        AzureProfile profile = new AzureProfile(config.tenantId(), config.subscriptionId(), AzureEnvironment.AZURE);

        // Connect and authenticate using the provided application and key
        return AzureResourceManager.configure().authenticate(credential, profile).withDefaultSubscription();
    }

    /**
     * Returns the DNS zone to update.
     */
    public DnsZone getDnsZone(AzureResourceManager manager) {
        return manager.dnsZones().getByResourceGroup(config.resourceGroup(), config.zoneName());
    }

}
