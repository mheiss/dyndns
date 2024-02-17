package cloud.heiss.dyndns.app;

import java.util.Collections;
import java.util.List;

public class DnsConfigDto {

	/**
	 * The ID of the Azure Subscription.
	 */
	public String subscriptionId;

	/**
	 * The ID of the Azure Active Directory.
	 * <p>
	 * Azure Portal -> Azure Active Directory -> Properties -> Directory ID
	 * </p>
	 */
	public String tenantId;

	/**
	 * The ID of the application that is allowed to connect and update the DNS
	 * <p>
	 * Azure Portal -> Azure Active Directory -> App registrations
	 * </p>
	 */
	public String clientId;

	/**
	 * The KEY that has been used to create the application.
	 */
	public String key;

	/**
	 * The name of the resource group containing the DNS Zone.
	 */
	public String resourceGroup;

	/**
	 * The name of the DNS zone to update.
	 */
	public String zoneName;

	/**
	 * List of record names to update.
	 */
	public List<String> recordNames = Collections.emptyList();

	/**
	 * The URL to call in order to get the IP address.
	 */
	public String ipServiceUrl;

	/**
	 * Schedules the update to run every xx minutes
	 */
	public int updateIntervalInMinutes;
}