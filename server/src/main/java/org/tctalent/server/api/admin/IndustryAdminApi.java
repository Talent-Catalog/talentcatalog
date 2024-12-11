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

import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.model.db.Industry;
import org.tctalent.server.request.industry.CreateIndustryRequest;
import org.tctalent.server.request.industry.SearchIndustryRequest;
import org.tctalent.server.request.industry.UpdateIndustryRequest;
import org.tctalent.server.service.db.IndustryService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController
@RequestMapping("/api/admin/industry")
@RequiredArgsConstructor
public class IndustryAdminApi {

    private final IndustryService industryService;

    @GetMapping()
    public List<Map<String, Object>> listAllIndustries() {
        List<Industry> industries = industryService.listIndustries();
        return industryDto().buildList(industries);
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchIndustryRequest request) {
        Page<Industry> industries = industryService.searchIndustries(request);
        return industryDto().buildPage(industries);
    }

    @GetMapping("{id}")
    public Map<String, Object> get(@PathVariable("id") long id) {
        Industry industry = industryService.getIndustry(id);
        return industryDto().build(industry);
    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody CreateIndustryRequest request) throws EntityExistsException {
        Industry industry = industryService.createIndustry(request);
        return industryDto().build(industry);
    }

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @Valid @RequestBody UpdateIndustryRequest request) throws EntityExistsException  {

        Industry industry = industryService.updateIndustry(id, request);
        return industryDto().build(industry);
    }

    @DeleteMapping("{id}")
    public boolean delete(@PathVariable("id") long id) throws EntityReferencedException {
        return industryService.deleteIndustry(id);
    }


    private DtoBuilder industryDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("status")
                ;
    }

}
