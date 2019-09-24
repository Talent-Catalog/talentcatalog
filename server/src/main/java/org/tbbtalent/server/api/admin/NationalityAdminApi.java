package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.Nationality;
import org.tbbtalent.server.service.NationalityService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/nationality")
public class NationalityAdminApi {

    private final NationalityService nationalityService;

    @Autowired
    public NationalityAdminApi(NationalityService nationalityService) {
        this.nationalityService = nationalityService;
    }

    @GetMapping()
    public List<Map<String, Object>> listAllNationalities() {
        List<Nationality> nationalities = nationalityService.listNationalities();
        return nationalityDto().buildList(nationalities);
    }

    private DtoBuilder nationalityDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

}
