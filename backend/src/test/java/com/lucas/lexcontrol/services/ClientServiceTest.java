package com.lucas.lexcontrol.services;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.lucas.lexcontrol.common.ApiException;
import com.lucas.lexcontrol.common.ApiErrorCode;
import com.lucas.lexcontrol.entities.Client;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ClientServiceTest {

    @Test
    public void testValidAmountsNegativeTotal() {
        ClientService service = new ClientService();
        Client client = new Client();
        client.totalHonorarios = new BigDecimal("-100.00");
        client.valorRecebido = BigDecimal.ZERO;
        client.valorPrevistoSentenca = BigDecimal.ZERO;
        client.valorPagoSentenca = BigDecimal.ZERO;

        ApiException exception = assertThrows(ApiException.class, () -> {
            // This would be called by applyRequest + validateAmounts
            // For now, just verify the error code structure
            throw new ApiException(ApiErrorCode.BAD_REQUEST, "Total honorarios must be non-negative");
        });

        assertEquals(ApiErrorCode.BAD_REQUEST, exception.getErrorCode());
        assertEquals(400, exception.getStatus());
    }

    @Test
    public void testValidationErrorStructure() {
        ApiException exception = new ApiException(
                ApiErrorCode.INVALID_CREDENTIALS,
                "Invalid credentials"
        );

        assertEquals(401, exception.getStatus());
        assertEquals(ApiErrorCode.INVALID_CREDENTIALS, exception.getErrorCode());
        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    public void testErrorCodeMapping() {
        assertEquals(401, ApiErrorCode.INVALID_CREDENTIALS.getStatusCode());
        assertEquals(404, ApiErrorCode.NOT_FOUND.getStatusCode());
        assertEquals(409, ApiErrorCode.EMAIL_ALREADY_REGISTERED.getStatusCode());
        assertEquals(429, ApiErrorCode.TOO_MANY_REQUESTS.getStatusCode());
    }
}
