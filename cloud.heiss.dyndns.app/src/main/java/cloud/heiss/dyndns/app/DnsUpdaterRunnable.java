package cloud.heiss.dyndns.app;

/**
 * A runnable that executes the update. Never throws an exception as otherwise
 * the executor service would stop executing.
 */
public class DnsUpdaterRunnable implements Runnable {

	private final DnsUpdater updater;

	public DnsUpdaterRunnable(DnsUpdater updater) {
		this.updater = updater;
	}

	@Override
	public void run() {
		try {
			updater.update();
		} catch (Exception ex) {
			System.err.println("Failed to update the IP address:" + ex.getMessage());
		}
	}

}