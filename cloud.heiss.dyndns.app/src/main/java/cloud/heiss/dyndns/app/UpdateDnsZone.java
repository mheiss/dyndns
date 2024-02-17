package cloud.heiss.dyndns.app;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Updates the root IPv4 address of a given DNS zone.
 */
public class UpdateDnsZone {

	private final static Logger logger = LoggerFactory.getLogger(UpdateDnsZone.class);

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			logger.error("Usage: java -jar dyndns-app-x.x.x-jar-with-dependencies.jar config.json");
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
			logger.error("Unable to read config file: " + file.getAbsolutePath());
			System.exit(-1);
			return;
		}

		ObjectMapper mapper = new ObjectMapper();
		DnsConfigDto config = mapper.readValue(Files.readString(file.toPath()), DnsConfigDto.class);
		DnsUpdater updater = new DnsUpdater(config);

		// Update once and then exit
		int interval = config.updateIntervalInMinutes;
		if (interval == -1) {
			updater.update();
			return;
		}

		// Schedule to run at a fixed rate (will prevent exit of the JVM)
		logger.info("Scheduling update to run every {} minutes.", interval);
		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
		service.scheduleAtFixedRate(new DnsUpdaterRunnable(updater), 0, interval, TimeUnit.MINUTES);
	}

}
