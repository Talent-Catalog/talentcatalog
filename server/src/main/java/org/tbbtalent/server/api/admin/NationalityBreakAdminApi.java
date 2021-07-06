/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.api.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/api/admin/nationality")
public class NationalityBreakAdminApi {

//    private final NationalityService nationalityService;
//
//    @Autowired
//    public NationalityBreakAdminApi(NationalityService nationalityService) {
//        this.nationalityService = nationalityService;
//    }
//
//    @GetMapping()
//    public List<Map<String, Object>> listAllNationalities() {
//        List<Nationality> nationalities = nationalityService.listNationalities();
//        return nationalityDto().buildList(nationalities);
//    }
//
//    @PostMapping("search")
//    public Map<String, Object> search(@RequestBody SearchNationalityRequest request) {
//        Page<Nationality> nationalities = this.nationalityService.searchNationalities(request);
//        return nationalityDto().buildPage(nationalities);
//    }
//
//    @GetMapping("{id}")
//    public Map<String, Object> get(@PathVariable("id") long id) {
//        Nationality nationality = this.nationalityService.getNationality(id);
//        return nationalityDto().build(nationality);
//    }
//
//    @PostMapping
//    public Map<String, Object> create(@Valid @RequestBody CreateNationalityRequest request) throws EntityExistsException {
//        Nationality nationality = this.nationalityService.createNationality(request);
//        return nationalityDto().build(nationality);
//    }
//
//    @PutMapping("{id}")
//    public Map<String, Object> update(@PathVariable("id") long id,
//                                      @Valid @RequestBody UpdateNationalityRequest request) throws EntityExistsException  {
//
//        Nationality nationality = this.nationalityService.updateNationality(id, request);
//        return nationalityDto().build(nationality);
//    }
//
//    @DeleteMapping("{id}")
//    public boolean delete(@PathVariable("id") long id) throws EntityReferencedException {
//        return this.nationalityService.deleteNationality(id);
//    }
//
//
//    private DtoBuilder nationalityDto() {
//        return new DtoBuilder()
//                .add("id")
//                .add("name")
//                .add("status")
//                ;
//    }

}
