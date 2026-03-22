package com.lucas.lexcontrol.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import com.lucas.lexcontrol.common.ApiException;
import com.lucas.lexcontrol.common.ApiErrorCode;
import com.lucas.lexcontrol.common.InputSanitizer;
import com.lucas.lexcontrol.dto.client.ClientRequest;
import com.lucas.lexcontrol.dto.client.ClientResponse;
import com.lucas.lexcontrol.entities.Client;
import com.lucas.lexcontrol.repositories.ClientRepository;
import com.lucas.lexcontrol.security.CurrentUser;

import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ClientService {

    private static final Logger LOG = Logger.getLogger(ClientService.class);

    @Inject
    ClientRepository clientRepository;

    @Inject
    CurrentUser currentUser;

    @Inject
    InputSanitizer sanitizer;

    public List<ClientResponse> list(String search, String status, String sort) {
        UUID userId = currentUser.getUserId();
        return clientRepository.listByUser(userId, search, status, sort)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ClientResponse get(UUID id) {
        Client client = clientRepository.findByIdAndUser(id, currentUser.getUserId())
                .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, "Client not found"));
        return toResponse(client);
    }

    @Transactional
    public ClientResponse create(ClientRequest request) {
        Client client = new Client();
        client.user = getCurrentUserEntity();
        applyRequest(client, request);
        validateAmounts(client);
        clientRepository.persist(client);
        LOG.infof("Client created: %s by user: %s", client.id, currentUser.getUserId());
        return toResponse(client);
    }

    @Transactional
    public ClientResponse update(UUID id, ClientRequest request) {
        Client client = clientRepository.findByIdAndUser(id, currentUser.getUserId())
                .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, "Client not found"));

        applyRequest(client, request);
        validateAmounts(client);
        LOG.infof("Client updated: %s by user: %s", id, currentUser.getUserId());
        return toResponse(client);
    }

    @Transactional
    public void delete(UUID id) {
        Client client = clientRepository.findByIdAndUser(id, currentUser.getUserId())
                .orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, "Client not found"));
        clientRepository.delete(client);
        LOG.infof("Client deleted: %s by user: %s", id, currentUser.getUserId());
    }

    private void applyRequest(Client client, ClientRequest request) {
        client.name = sanitizer.clean(request.name);
        client.description = sanitizer.clean(request.description);
        client.totalHonorarios = normalize(request.totalHonorarios);
        client.valorRecebido = normalize(request.valorRecebido);
        client.valorPrevistoSentenca = normalize(request.valorPrevistoSentenca);
        client.valorPagoSentenca = normalize(request.valorPagoSentenca);
    }

    private void validateAmounts(Client client) {
        if (client.totalHonorarios.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException(ApiErrorCode.BAD_REQUEST, "Total honorarios must be non-negative");
        }
        if (client.valorRecebido.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException(ApiErrorCode.BAD_REQUEST, "Valor recebido must be non-negative");
        }
        if (client.valorPrevistoSentenca.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException(ApiErrorCode.BAD_REQUEST, "Valor previsto sentenca must be non-negative");
        }
        if (client.valorPagoSentenca.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException(ApiErrorCode.BAD_REQUEST, "Valor pago sentenca must be non-negative");
        }
        if (client.valorRecebido.compareTo(client.totalHonorarios) > 0) {
            throw new ApiException(ApiErrorCode.BAD_REQUEST, "Valor recebido cannot exceed total honorarios");
        }
        if (client.valorPagoSentenca.compareTo(client.valorPrevistoSentenca) > 0) {
            throw new ApiException(ApiErrorCode.BAD_REQUEST, "Valor pago sentenca cannot exceed valor previsto");
        }
    }

    private ClientResponse toResponse(Client client) {
        BigDecimal valorPendente = client.totalHonorarios.subtract(client.valorRecebido);
        if (valorPendente.compareTo(BigDecimal.ZERO) < 0) {
            valorPendente = BigDecimal.ZERO;
        }
        BigDecimal valorPendenteSentenca = client.valorPrevistoSentenca.subtract(client.valorPagoSentenca);
        if (valorPendenteSentenca.compareTo(BigDecimal.ZERO) < 0) {
            valorPendenteSentenca = BigDecimal.ZERO;
        }
        return new ClientResponse(
                client.id,
                client.name,
                client.description,
                client.totalHonorarios,
                client.valorRecebido,
                valorPendente,
                client.valorPrevistoSentenca,
                client.valorPagoSentenca,
                valorPendenteSentenca,
                client.createdAt
        );
    }

    private BigDecimal normalize(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private com.lucas.lexcontrol.entities.User getCurrentUserEntity() {
        return clientRepository.getEntityManager().getReference(
                com.lucas.lexcontrol.entities.User.class, currentUser.getUserId());
    }
}
