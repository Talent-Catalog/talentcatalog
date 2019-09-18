package org.tbbtalent.server.api.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.Certification;
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
        Certification certification = certificationService.createCertification(request);
        return certificationDto().build(certification);
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
