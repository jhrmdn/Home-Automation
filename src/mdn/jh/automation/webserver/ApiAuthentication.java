package mdn.jh.automation.webserver;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import jakarta.servlet.http.HttpServletRequest;
import mdn.jh.automation.security.WebUserStore;

final class ApiAuthentication {
    private ApiAuthentication() { }
    static boolean allowed(HttpServletRequest request, WebUserStore users, boolean write) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.regionMatches(true, 0, "Basic ", 0, 6)) return false;
        try {
            String credentials = new String(Base64.getDecoder().decode(header.substring(6).trim()), StandardCharsets.UTF_8);
            int colon = credentials.indexOf(':');
            if (colon < 0) return false;
            var user = users.authenticate(credentials.substring(0, colon), credentials.substring(colon + 1));
            return user != null && (write ? user.canWrite() : user.canRead());
        } catch (IllegalArgumentException e) { return false; }
    }
}
