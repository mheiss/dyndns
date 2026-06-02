package cloud.heiss.dyndns.unifi;

import java.io.IOException;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet to handle Dynamic DNS update requests from UniFi devices.
 * <p>
 * This servlet listens on the path "/nic/update" and expects Basic Authentication with credentials defined in the UniFi Network
 * configuration. It also expects a query parameter "myip" containing the new IP address to update in Azure DNS.
 * </p>
 * <p>
 * The return status codes must strictly follow the DynDNS protocol specification:
 * <ul>
 * <li>good <ip> — update succeeded</li>
 * <li>nochg <ip> — IP unchanged</li>
 * <li>badauth — wrong username/password</li>
 * <li>notfqdn — hostname invalid</li>
 * <li>nohost — hostname not registered</li>
 * <li>911 — server error, retry later</li>
 * </ul>
 * <p>
 * The status code must always be 200 OK, and the body must contain the status message. If the status is not 200, inadyn assumes the server
 * rejected the request.
 * </p>
 */
@WebServlet(urlPatterns = "/nic/update")
public class UniFiDnsServlet extends HttpServlet {

    private final static Logger logger = LoggerFactory.getLogger(UniFiDnsServlet.class);

    @Inject
    AzureDnsUpdater dnsUpdater;

    @Inject
    AzureConfigDto azureConfig;

    @Inject
    UniFiConfigDto unifiConfig;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("Received update request: '{}'", req.getQueryString());

        resp.setContentType("text/plain");
        resp.setStatus(HttpServletResponse.SC_OK);

        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Basic ")) {
            resp.getWriter().write("badauth");
            return;
        }
        String decoded = new String(Base64.getDecoder().decode(auth.substring(6)));
        if (!decoded.equals(unifiConfig.username().get() + ":" + unifiConfig.password().get())) {
            resp.getWriter().write("badauth");
            return;
        }

        String ip = req.getParameter("myip");
        if (ip == null) {
            resp.getWriter().write("noip");
            return;
        }

        try {
            boolean updated = dnsUpdater.update(ip);
            if (updated) {
                resp.getWriter().write("good " + ip);
            } else {
                resp.getWriter().write("nochg " + ip);
            }
        } catch (Exception e) {
            logger.error("Error updating DNS record", e);
            resp.getWriter().write("911");
            return;
        }
    }

}
