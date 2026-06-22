package com.lucas.lexcontrol.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.lucas.lexcontrol.common.ApiException;
import com.lucas.lexcontrol.common.InputSanitizer;
import com.lucas.lexcontrol.dto.transaction.TransactionRequest;
import com.lucas.lexcontrol.dto.transaction.TransactionResponse;
import com.lucas.lexcontrol.entities.Client;
import com.lucas.lexcontrol.entities.Transaction;
import com.lucas.lexcontrol.entities.TransactionType;
import com.lucas.lexcontrol.repositories.ClientRepository;
import com.lucas.lexcontrol.repositories.TransactionRepository;
import com.lucas.lexcontrol.security.CurrentUser;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class TransactionService {

    @Inject
    TransactionRepository transactionRepository;

    @Inject
    ClientRepository clientRepository;

    @Inject
    CurrentUser currentUser;

    @Inject
    InputSanitizer sanitizer;

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    public List<TransactionResponse> list(TransactionType type, UUID clientId, LocalDate from, LocalDate to,
            String sort, int page, int size) {
        UUID userId = currentUser.getUserId();
        int safePage = Math.max(page, 0);
        int safeSize = clampSize(size);
        return transactionRepository.listByUser(userId, type, clientId, from, to, sort, safePage, safeSize)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private int clampSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.user = getCurrentUserEntity();
        transaction.type = request.type;
        transaction.amount = normalize(request.amount);
        transaction.date = request.date;
        transaction.description = sanitizer.clean(request.description);

        if (request.clientId != null) {
            Client client = getClientForUser(request.clientId);
            if (transaction.type == TransactionType.INCOME) {
                ensureIncomeWithinPending(client, transaction.amount);
                client.valorRecebido = client.valorRecebido.add(transaction.amount);
            }
            transaction.client = client;
        }

        transactionRepository.persist(transaction);
        return toResponse(transaction);
    }

    @Transactional
    public TransactionResponse update(UUID id, TransactionRequest request) {
        Transaction transaction = transactionRepository.findByIdAndUser(id, currentUser.getUserId())
                .orElseThrow(() -> new ApiException(404, "Transaction not found"));

        Client oldClient = transaction.client;
        TransactionType oldType = transaction.type;
        BigDecimal oldAmount = transaction.amount;

        if (oldType == TransactionType.INCOME && oldClient != null) {
            oldClient.valorRecebido = oldClient.valorRecebido.subtract(oldAmount);
            if (oldClient.valorRecebido.compareTo(BigDecimal.ZERO) < 0) {
                oldClient.valorRecebido = BigDecimal.ZERO;
            }
        }

        Client newClient = null;
        if (request.clientId != null) {
            newClient = getClientForUser(request.clientId);
        }

        transaction.type = request.type;
        transaction.amount = normalize(request.amount);
        transaction.date = request.date;
        transaction.description = sanitizer.clean(request.description);
        transaction.client = newClient;

        if (transaction.type == TransactionType.INCOME && newClient != null) {
            ensureIncomeWithinPending(newClient, transaction.amount);
            newClient.valorRecebido = newClient.valorRecebido.add(transaction.amount);
        }

        return toResponse(transaction);
    }

    @Transactional
    public void delete(UUID id) {
        Transaction transaction = transactionRepository.findByIdAndUser(id, currentUser.getUserId())
                .orElseThrow(() -> new ApiException(404, "Transaction not found"));

        if (transaction.type == TransactionType.INCOME && transaction.client != null) {
            Client client = transaction.client;
            client.valorRecebido = client.valorRecebido.subtract(transaction.amount);
            if (client.valorRecebido.compareTo(BigDecimal.ZERO) < 0) {
                client.valorRecebido = BigDecimal.ZERO;
            }
        }

        transactionRepository.delete(transaction);
    }

    private void ensureIncomeWithinPending(Client client, BigDecimal amount) {
        BigDecimal pending = client.totalHonorarios.subtract(client.valorRecebido);
        if (amount.compareTo(pending) > 0) {
            throw new ApiException(400, "Amount exceeds pending honorarios");
        }
    }

    private TransactionResponse toResponse(Transaction transaction) {
        UUID clientId = transaction.client != null ? transaction.client.id : null;
        String clientName = transaction.client != null ? transaction.client.name : null;
        return new TransactionResponse(
                transaction.id,
                transaction.type,
                transaction.amount,
                transaction.description,
                transaction.date,
                clientId,
                clientName,
                transaction.createdAt
        );
    }

    private Client getClientForUser(UUID clientId) {
        return clientRepository.findByIdAndUser(clientId, currentUser.getUserId())
                .orElseThrow(() -> new ApiException(404, "Client not found"));
    }

    private BigDecimal normalize(BigDecimal value) {
        if (value == null) {
            throw new ApiException(400, "Amount is required");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private com.lucas.lexcontrol.entities.User getCurrentUserEntity() {
        return transactionRepository.getEntityManager().getReference(
                com.lucas.lexcontrol.entities.User.class, currentUser.getUserId());
    }
}
