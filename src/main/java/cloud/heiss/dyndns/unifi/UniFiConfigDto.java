package cloud.heiss.dyndns.unifi;

import java.util.Optional;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "unifi")
public interface UniFiConfigDto {

    /**
     * The username configured in the Dynamic DNS form.
     */
    public Optional<String> username();

    /**
     * The password configured in the Dynamic DNS form.
     */
    public Optional<String> password();

}
