
package com.ampta.controller;

import com.ampta.dto.request.ClosureCreateRequest;
import com.ampta.dto.response.ClosureResponse;
import com.ampta.service.ClosureService;
import com.ampta.utils.Endpoints;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(Endpoints.V1_CLOSURE)
public class ClosureController {

    private final ClosureService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClosureResponse create(
            @Valid @RequestBody ClosureCreateRequest req) {
        return service.create(req);
    }

    @GetMapping("/{requestId}")
    public ClosureResponse get(
            @PathVariable Long requestId) {
        return service.get(requestId);
    }

    @GetMapping
    public List<ClosureResponse> all() {
        return service.all();
    }

    @GetMapping("/loan-account/{loanAccountId}")
    public List<ClosureResponse> byLoan(
            @PathVariable Long loanAccountId) {
        return service.byLoan(loanAccountId);
    }

    @PatchMapping("/{requestId}/approve")
    public ClosureResponse approve(
            @PathVariable Long requestId) {
        return service.approve(requestId);
    }

    @PatchMapping("/{requestId}/reject")
    public ClosureResponse reject(
            @PathVariable Long requestId,
            @Valid @RequestBody RejectBody body) {
        return service.reject(requestId, body.reason());
    }

    @PatchMapping("/{requestId}/pay")
    public ClosureResponse pay(
            @PathVariable Long requestId,
            @Valid @RequestBody PayBody body) {
        return service.pay(requestId, body.paymentReference());
    }

    @PatchMapping("/{requestId}/complete")
    public ClosureResponse complete(
            @PathVariable Long requestId) {
        return service.complete(requestId);
    }

    public record RejectBody(
            @NotBlank String reason) {
    }

    public record PayBody(
            @NotBlank String paymentReference) {
    }
}
