package com.ampta.controller;


import com.ampta.dto.request.SupportTicketRequest;
import com.ampta.dto.response.ApiResponse;
import com.ampta.dto.response.SupportTicketResponse;
import com.ampta.service.SupportTicketService;
import com.ampta.utils.Endpoints;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(Endpoints.V1_TICKET)
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    @PostMapping
    public ResponseEntity<ApiResponse<SupportTicketResponse>> createTicket(@Valid @RequestBody SupportTicketRequest request) {
        log.info("REST request to create support ticket for customer: {}", request.getCustomerId());
        SupportTicketResponse response = supportTicketService.createTicket(request);
        return new ResponseEntity<>(new ApiResponse<>(true, "Support ticket created successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<ApiResponse<SupportTicketResponse>> getTicketById(@PathVariable Long ticketId) {
        log.info("REST request to get support ticket: {}", ticketId);
        SupportTicketResponse response = supportTicketService.getTicketById(ticketId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Support ticket retrieved successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SupportTicketResponse>>> getAllTickets() {
        log.info("REST request to get all support tickets");
        List<SupportTicketResponse> list = supportTicketService.getAllTickets();
        return ResponseEntity.ok(new ApiResponse<>(true, "All support tickets retrieved successfully", list));
    }

    @PutMapping("/{ticketId}")
    public ResponseEntity<ApiResponse<SupportTicketResponse>> updateTicket(@PathVariable Long ticketId, @RequestBody SupportTicketRequest request) {
        log.info("REST request to update support ticket: {}", ticketId);
        SupportTicketResponse response = supportTicketService.updateTicket(ticketId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Support ticket updated successfully", response));
    }

    @PatchMapping("/{ticketId}/status")
    public ResponseEntity<ApiResponse<SupportTicketResponse>> updateTicketStatus(@PathVariable Long ticketId, @Valid @RequestParam String request) {
        log.info("REST request to update status for ticket: {}", ticketId);
        SupportTicketResponse response = supportTicketService.updateTicketStatus(ticketId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Support ticket status updated successfully", response));
    }

    @DeleteMapping("/{ticketId}")
    public ResponseEntity<ApiResponse<Void>> deleteTicket(@PathVariable Long ticketId) {
        log.info("REST request to delete support ticket: {}", ticketId);
        supportTicketService.deleteTicket(ticketId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Support ticket deleted successfully", null));
    }
}
