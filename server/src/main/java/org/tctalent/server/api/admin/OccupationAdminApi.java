/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.api.admin;

import jakarta.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.api.dto.SystemLanguageDtoBuilder;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Occupation;
import org.tctalent.server.model.db.SystemLanguage;
import org.tctalent.server.request.occupation.CreateOccupationRequest;
import org.tctalent.server.request.occupation.SearchOccupationRequest;
import org.tctalent.server.request.occupation.UpdateOccupationRequest;
import org.tctalent.server.service.db.LanguageService;
import org.tctalent.server.service.db.OccupationService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/occupation")
public class OccupationAdminApi {

    private final OccupationService occupationService;
    private final LanguageService languageService;
    private final DtoBuilder systemLanguageDtoBuilder = new SystemLanguageDtoBuilder();

    @Autowired
    public OccupationAdminApi(OccupationService occupationService,
        LanguageService languageService) {
        this.occupationService = occupationService;
        this.languageService = languageService;
    }

    @PostMapping("system/{langCode}")
    public Map<String, Object> addSystemLanguageTranslations(
        @PathVariable("langCode") String langCode, @RequestParam("file") MultipartFile file)
        throws EntityExistsException, IOException, NoSuchObjectException {
        try (InputStream translations = file.getInputStream()) {
            SystemLanguage systemLanguage =
                languageService.addSystemLanguageTranslations(
                    langCode, "occupation", translations);

            return systemLanguageDtoBuilder.build(systemLanguage);
        }
    }

    @GetMapping()
    public List<Map<String, Object>> listAllOccupations() {
        List<Occupation> occupations = occupationService.listOccupations();
        return occupationDto().buildList(occupations);
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchOccupationRequest request) {
        Page<Occupation> occupations = this.occupationService.searchOccupations(request);
        return occupationDto().buildPage(occupations);
    }

    @GetMapping("{id}")
    public Map<String, Object> get(@PathVariable("id") long id) {
        Occupation occupation = this.occupationService.getOccupation(id);
        return occupationDto().build(occupation);
    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody CreateOccupationRequest request) throws EntityExistsException {
        Occupation occupation = this.occupationService.createOccupation(request);
        return occupationDto().build(occupation);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @Valid @RequestBody UpdateOccupationRequest request) throws EntityExistsException  {

        Occupation occupation = this.occupationService.updateOccupation(id, request);
        return occupationDto().build(occupation);
    }

    @DeleteMapping("{id}")
    public boolean delete(@PathVariable("id") long id) throws EntityReferencedException {
        return this.occupationService.deleteOccupation(id);
    }

    private DtoBuilder occupationDto() {
        DtoBuilder builder = occupationService.selectBuilder();
        return builder.add("status");
    }

}
