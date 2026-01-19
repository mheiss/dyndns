package cloud.heiss.dyndns.unifi;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "unifi")
public interface UniFiConfigDto {

    /**
     * The username configured in the Dynamic DNS form.
     */
    public String username();

    /**
     * The password configured in the Dynamic DNS form.
     */
    public String password();

}
