
package com.ampta.service;

import com.ampta.dto.response.SanctionLetterResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface SanctionLetterService {

    SanctionLetterResponse generateSanctionLetter(Long dealId);

    SanctionLetterResponse getSanctionLetter(Long dealId);

    public ResponseEntity<Resource> downloadSanctionLetter(Long dealId);

}
