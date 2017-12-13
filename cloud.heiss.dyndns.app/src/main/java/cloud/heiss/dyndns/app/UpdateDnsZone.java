package cloud.heiss.dyndns.app;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Updates the root IPv4 address of a given DNS zone.
 */
public class UpdateDnsZone {

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println("Usage: java -jar dyndns-app-x.x.x-SNAPSHOT-jar-with-dependencies.jar config.json");
			System.exit(-1);
			return;
		}
		File file = new File(args[0]);
		if (!file.exists() || !file.isFile()) {
			System.err.println("Unable to read config file: " + file.getAbsolutePath());
			System.exit(-1);
			return;
		}

		DnsConfig config = new DnsConfig(file);
		DnsUpdater updater = new DnsUpdater(config);

		// Update once and then exit
		int interval = config.updateIntervalInMinutes;
		if (interval == -1) {
			updater.update();
			return;
		}

		// Schedule to run at a fixed rate (will prevent exit of the JVM)
		System.out.println("Scheduling update to run every " + interval + " minutes.");
		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
		service.scheduleAtFixedRate(new DnsUpdaterRunnable(updater), 0, interval, TimeUnit.MINUTES);
	}

}
