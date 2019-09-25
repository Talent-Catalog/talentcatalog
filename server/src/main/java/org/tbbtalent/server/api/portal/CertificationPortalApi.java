package org.tbbtalent.server.api.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.model.CandidateCertification;
import org.tbbtalent.server.request.certification.CreateCertificationRequest;
import org.tbbtalent.server.service.CertificationService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import java.util.Map;

@RestController()
@RequestMapping("/api/portal/certification")
public class CertificationPortalApi {

    private final CertificationService certificationService;

    @Autowired
    public CertificationPortalApi(CertificationService certificationService) {
        this.certificationService = certificationService;
    }

    @PostMapping()
    public Map<String, Object> createCertification(@Valid @RequestBody CreateCertificationRequest request) {
        CandidateCertification candidateCertification = certificationService.createCertification(request);
        return certificationDto().build(candidateCertification);
    }

    @DeleteMapping("{id}")
    public ResponseEntity deleteCertification(@PathVariable("id") Long id) {
        certificationService.deleteCertification(id);
        return ResponseEntity.ok().build();
    }



    private DtoBuilder certificationDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("institution")
                .add("dateCompleted")
                ;
    }

}
