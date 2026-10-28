package com.sigevet.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sigevet.entity.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.secret:SigevetClaveSecretaSuperSeguraParaFirmaDigitalConHMACSHA256Veterinaria2026!}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}") // 24 horas en ms por defecto
    private long expirationMillis;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Genera un token JWT firmado para el usuario con sus claims (id, username, rol, nombre, etc.)
     */
    public String generarToken(Usuario usuario) {
        try {
            long now = System.currentTimeMillis();
            long exp = now + expirationMillis;

            // 1. Header
            Map<String, Object> header = new HashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");
            String headerJson = objectMapper.writeValueAsString(header);
            String headerB64 = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));

            // 2. Payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("sub", usuario.getUsername());
            payload.put("userId", usuario.getId());
            payload.put("nombre", usuario.getNombre());
            payload.put("apellido", usuario.getApellido());
            payload.put("rol", usuario.getRol());
            payload.put("correo", usuario.getCorreo());
            payload.put("iat", now / 1000);
            payload.put("exp", exp / 1000);

            String payloadJson = objectMapper.writeValueAsString(payload);
            String payloadB64 = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));

            // 3. Firma
            String content = headerB64 + "." + payloadB64;
            String signatureB64 = firmar(content, secretKey);

            return content + "." + signatureB64;
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el token JWT", e);
        }
    }

    /**
     * Valida la firma del token y su vigencia
     */
    public boolean esTokenValido(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }

        try {
            String content = parts[0] + "." + parts[1];
            String expectedSignature = firmar(content, secretKey);

            // Verificación en tiempo constante contra timing attacks
            if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8),
                                       parts[2].getBytes(StandardCharsets.UTF_8))) {
                return false;
            }

            Map<String, Object> claims = obtenerClaims(token);
            if (claims == null || !claims.containsKey("exp")) {
                return false;
            }

            long exp = ((Number) claims.get("exp")).longValue();
            long now = System.currentTimeMillis() / 1000;
            return now <= exp;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extrae el username (claim 'sub') del token
     */
    public String obtenerUsername(String token) {
        Map<String, Object> claims = obtenerClaims(token);
        return claims != null ? (String) claims.get("sub") : null;
    }

    /**
     * Extrae los claims decodificados del token
     */
    public Map<String, Object> obtenerClaims(String token) {
        if (token == null) return null;
        String[] parts = token.split("\\.");
        if (parts.length != 3) return null;

        try {
            byte[] bytes = base64UrlDecode(parts[1]);
            String json = new String(bytes, StandardCharsets.UTF_8);
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return null;
        }
    }

    private String firmar(String data, String secret) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmac.init(secretKeySpec);
        byte[] rawHmac = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return base64UrlEncode(rawHmac);
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private byte[] base64UrlDecode(String str) {
        return Base64.getUrlDecoder().decode(str);
    }
}
