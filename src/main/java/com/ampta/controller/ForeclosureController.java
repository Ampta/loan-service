package com.ampta.controller;

import com.ampta.dto.request.ForeclosureCreateRequest;
import com.ampta.dto.response.ForeclosureResponse;
import com.ampta.service.ForeclosureService;
import com.ampta.utils.Endpoints;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
//@RequestMapping(Endpoints.V1_FORECLOSURE)
@RequestMapping("/api/v1/foreclosures")
public class ForeclosureController {

    private final ForeclosureService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ForeclosureResponse create(
            @Valid @RequestBody ForeclosureCreateRequest req) {

        return service.create(req);
    }

    @GetMapping("/{requestId}")
    public ForeclosureResponse get(
            @PathVariable Long requestId) {

        return service.get(requestId);
    }

    @GetMapping
    public List<ForeclosureResponse> all() {

        return service.all();
    }

    @GetMapping("/loan-account/{loanAccountId}")
    public List<ForeclosureResponse> byLoan(
            @PathVariable Long loanAccountId) {

        return service.byLoan(loanAccountId);
    }

    @PatchMapping("/{requestId}/approve")
    public ForeclosureResponse approve(
            @PathVariable Long requestId) {

        return service.approve(requestId);
    }

    @PatchMapping("/{requestId}/reject")
    public ForeclosureResponse reject(
            @PathVariable Long requestId,
            @RequestBody RejectBody body) {

        return service.reject(requestId, body.reason());
    }

    @PatchMapping("/{requestId}/pay")
    public ForeclosureResponse pay(
            @PathVariable Long requestId,
            @Valid @RequestBody PayBody body) {

        return service.pay(
                requestId,
                body.paymentReference()
        );
    }

    @PatchMapping("/{requestId}/close")
    public ForeclosureResponse complete(
            @PathVariable Long requestId) {

        return service.complete(requestId);
    }

    public record RejectBody(
            @NotBlank String reason
    ) {
    }

    public record PayBody(
            @NotBlank String paymentReference
    ) {
    }
}
