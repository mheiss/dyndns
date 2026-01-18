package cloud.heiss.dyndns.unifi;

import java.io.IOException;
import java.util.Base64;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/nic/update")
public class DynDnsServlet extends HttpServlet {
    private static final String USER = "routeruser";
    private static final String PASS = "routerpass";

    @Inject
    DnsConfigDto configDto;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");

        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Basic ")) {
            resp.getWriter().write("badauth");
            return;
        }
        String decoded = new String(Base64.getDecoder().decode(auth.substring(6)));
        if (!decoded.equals(USER + ":" + PASS)) {
            resp.getWriter().write("badauth");
            return;
        }

        String ip = req.getParameter("myip");
        if (ip == null) {
            resp.getWriter().write("nohost");
            return;
        }

        boolean changed = updateDnsRecord(ip);
        if (changed) {
            resp.getWriter().write("good " + ip);
        } else {
            resp.getWriter().write("nochg " + ip);
        }
    }

    private boolean updateDnsRecord(String ip) {
        DnsUpdater updater = new DnsUpdater(configDto);
        updater.update(ip);

        return true;
    }

}
