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
        resp.setContentType("text/plain");
        logger.info("Received update request: '{}'", req.getRequestURL() + "?" + req.getQueryString());

        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Basic ")) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "badauth");
            return;
        }
        String decoded = new String(Base64.getDecoder().decode(auth.substring(6)));
        if (!decoded.equals(unifiConfig.username().get() + ":" + unifiConfig.password().get())) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "badauth");
            return;
        }

        String ip = req.getParameter("myip");
        if (ip == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No IP provided");
            return;
        }

        try {
            dnsUpdater.update(ip);
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            logger.error("Error updating DNS record", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "azure update error");
            return;
        }
    }

}
