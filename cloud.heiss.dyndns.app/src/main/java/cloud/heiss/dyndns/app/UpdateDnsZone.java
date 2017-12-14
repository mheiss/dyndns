package cloud.heiss.dyndns.app;

import java.io.File;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Updates the root IPv4 address of a given DNS zone.
 */
public class UpdateDnsZone {

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println("Usage: java -jar dyndns-app-x.x.x-jar-with-dependencies.jar config.json");
			System.exit(-1);
			return;
		}

		// Support the case that just the name is given without a full path
		// File is then expected to be relative to the JAR location
		File file = new File(args[0]);
		if (!file.isAbsolute()) {
			URL location = UpdateDnsZone.class.getProtectionDomain().getCodeSource().getLocation();
			File jarFile = new File(location.toURI().getPath());
			file = new File(jarFile.getParentFile(), args[0]);
		}

		// Exit if we cannot read the configuration
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
