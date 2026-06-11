package SecureMiddleware.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    @Test
    void shouldGenerateValidToken() {
        JwtService service = new JwtService();

        String token = service.generateToken("admin");

        assertNotNull(token);
        assertTrue(service.isValid(token));
        assertEquals("admin", service.getUsername(token));
    }

    @Test
    void shouldRejectMalformedToken() {
        JwtService service = new JwtService();

        assertFalse(service.isValid("not-a-real-token"));
    }

    @Test
    void shouldRejectTamperedToken() {
        JwtService service = new JwtService();

        String token = service.generateToken("admin");
        String tamperedToken = token.substring(0, token.length() - 2) + "xx";

        assertFalse(service.isValid(tamperedToken));
    }
}
