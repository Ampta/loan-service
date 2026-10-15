package com.ampta.service.impl;

import com.ampta.dto.request.SupportTicketRequest;
import com.ampta.dto.response.SupportTicketResponse;
import com.ampta.entity.Customer;
import com.ampta.entity.LoanAccount;
import com.ampta.entity.SupportTicket;
import com.ampta.exception.ResourceNotFoundException;
import com.ampta.repository.CustomerRepository;
import com.ampta.repository.LoanAccountRepository;
import com.ampta.repository.SupportTicketRepository;
import com.ampta.service.SupportTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final CustomerRepository customerRepository;
    private final LoanAccountRepository loanAccountRepository;

    @Override
    public SupportTicketResponse createTicket(SupportTicketRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        LoanAccount loanAccount = null;
        if (request.getLoanAccountId() != null) {
            loanAccount = loanAccountRepository.findById(request.getLoanAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Loan account not found with id: " + request.getLoanAccountId()));
        }

        SupportTicket ticket = new SupportTicket();
        ticket.setCustomer(customer);
        ticket.setLoanAccount(loanAccount);
        ticket.setSubject(request.getSubject());
        ticket.setDescription(request.getDescription());
        ticket.setStatus("OPEN");

        SupportTicket saved = supportTicketRepository.save(ticket);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SupportTicketResponse getTicketById(Long ticketId) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found with id: " + ticketId));
        return mapToResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getCustomerTickets(Long customerId) {
        return supportTicketRepository.findByCustomer_CustomerId(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getAllTickets() {
        return supportTicketRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SupportTicketResponse updateTicket(Long ticketId, SupportTicketRequest request) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found with id: " + ticketId));

        if (request.getSubject() != null) ticket.setSubject(request.getSubject());
        if (request.getDescription() != null) ticket.setDescription(request.getDescription());
        if (request.getLoanAccountId() != null) {
            LoanAccount loanAccount = loanAccountRepository.findById(request.getLoanAccountId())
                    .orElseThrow(() -> new ResourceNotFoundException("Loan account not found with id: " + request.getLoanAccountId()));
            ticket.setLoanAccount(loanAccount);
        }

        SupportTicket updated = supportTicketRepository.save(ticket);
        return mapToResponse(updated);
    }

    @Override
    public SupportTicketResponse updateTicketStatus(Long ticketId, String status) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found with id: " + ticketId));

        ticket.setStatus(status);
        SupportTicket updated = supportTicketRepository.save(ticket);
        return mapToResponse(updated);
    }

    @Override
    public void deleteTicket(Long ticketId) {
        if (!supportTicketRepository.existsById(ticketId)) {
            throw new ResourceNotFoundException("Support ticket not found with id: " + ticketId);
        }
        supportTicketRepository.deleteById(ticketId);
    }

    private SupportTicketResponse mapToResponse(SupportTicket t) {
        String customerName = t.getCustomer() != null ? t.getCustomer().getFirstName() + (t.getCustomer().getLastName() != null ? " " + t.getCustomer().getLastName() : "") : "";
        return SupportTicketResponse.builder()
                .ticketId(t.getTicketId())
                .customerId(t.getCustomer() != null ? t.getCustomer().getCustomerId() : null)
                .customerName(customerName)
                .loanAccountId(t.getLoanAccount() != null ? t.getLoanAccount().getLoanAccountId() : null)
                .subject(t.getSubject())
                .description(t.getDescription())
                .status(t.getStatus())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
