package cloud.heiss.dyndns.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A runnable that executes the update. Never throws an exception as otherwise
 * the executor service would stop executing.
 */
public class DnsUpdaterRunnable implements Runnable {

	private final static Logger logger = LoggerFactory.getLogger(DnsUpdaterRunnable.class);

	private final DnsUpdater updater;

	public DnsUpdaterRunnable(DnsUpdater updater) {
		this.updater = updater;
	}

	@Override
	public void run() {
		try {
			updater.update();
		} catch (Exception ex) {
			logger.error("Failed to update the IP address.", ex);
		}
	}

}