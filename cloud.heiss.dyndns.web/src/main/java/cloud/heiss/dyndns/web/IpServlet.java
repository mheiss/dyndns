package cloud.heiss.dyndns.web;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@WebServlet("/")
public class IpServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// First one is always the client
		String[] forwardedFor = request.getHeader("X-Forwarded-For").split(",");
		String clientIp = forwardedFor[0];

		// Might contain IP and port
		int separator = clientIp.indexOf(":");
		if (separator != -1) {
			clientIp = clientIp.substring(0, separator);
		}

		// Send response
		response.getWriter().println(clientIp);
	}

}