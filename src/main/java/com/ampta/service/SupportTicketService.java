package com.ampta.service;

import com.ampta.dto.request.SupportTicketRequest;
import com.ampta.dto.response.SupportTicketResponse;

import java.util.List;

public interface SupportTicketService {
    SupportTicketResponse createTicket(SupportTicketRequest request);
    SupportTicketResponse getTicketById(Long ticketId);
    List<SupportTicketResponse> getCustomerTickets(Long customerId);
    List<SupportTicketResponse> getAllTickets();
    SupportTicketResponse updateTicket(Long ticketId, SupportTicketRequest request);
    SupportTicketResponse updateTicketStatus(Long ticketId, String status);
    void deleteTicket(Long ticketId);
}
