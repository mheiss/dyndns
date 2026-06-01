package cloud.heiss.dyndns.unifi;

import java.io.IOException;
import java.util.Base64;
import java.util.stream.Stream;

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

    @Inject
    AzureConfigDto azureConfig;

    @Inject
    UniFiConfigDto unifiConfig;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");

        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Basic ")) {
            resp.getWriter().write("badauth");
            return;
        }
        String decoded = new String(Base64.getDecoder().decode(auth.substring(6)));
        if (!decoded.equals(unifiConfig.username() + ":" + unifiConfig.password())) {
            resp.getWriter().write("badauth");
            return;
        }

        String ip = req.getParameter("myip");
        if (ip == null) {
            resp.getWriter().write("nohost");
            return;
        }

        AzureDnsUpdater updater = new AzureDnsUpdater(azureConfig);
        boolean changed = updater.update(ip);
        if (changed) {
            resp.getWriter().write("good " + ip);
        } else {
            resp.getWriter().write("nochg " + ip);
        }
    }

}
