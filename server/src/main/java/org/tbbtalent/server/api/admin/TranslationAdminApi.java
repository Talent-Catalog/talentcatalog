package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.model.Translation;
import org.tbbtalent.server.request.translation.SearchTranslationRequest;
import org.tbbtalent.server.service.TranslationService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/translation")
public class TranslationAdminApi {

    private final TranslationService translationService;

    @Autowired
    public TranslationAdminApi(TranslationService translationService) {
        this.translationService = translationService;
    }

//    @GetMapping()
//    public List<Map<String, Object>> listAllTranslations() {
//        List<Translation> translations = translationService.listTranslations();
//        return translationDto().buildList(translations);
//    }

    @PostMapping("search/{type}")
    public Map<String, Object> search(@PathVariable ("type") String type, @RequestBody SearchTranslationRequest request) {
        List<Translation> translations = this.translationService.search(request);
        return translationDto().build(translations);

    }

//    @GetMapping("{id}")
//    public Map<String, Object> get(@PathVariable("id") long id) {
//        Translation translation = this.translationService.getTranslation(id);
//        return translationDto().build(translation);
//    }
//
//    @PostMapping
//    public Map<String, Object> create(@Valid @RequestBody CreateTranslationRequest request) throws EntityExistsException {
//        Translation translation = this.translationService.createTranslation(request);
//        return translationDto().build(translation);
//    }
//
//    @PutMapping("{id}")
//    public Map<String, Object> update(@PathVariable("id") long id,
//                                      @Valid @RequestBody UpdateTranslationRequest request) throws EntityExistsException  {
//
//        Translation translation = this.translationService.updateTranslation(id, request);
//        return translationDto().build(translation);
//    }
//
//    @DeleteMapping("{id}")
//    public boolean delete(@PathVariable("id") long id) throws EntityReferencedException {
//        return this.translationService.deleteTranslation(id);
//    }


    private DtoBuilder translationDto() {
        return new DtoBuilder()
                .add("id")
                .add("objectId")
                .add("objectType")
                .add("language")
                .add("value")
                ;
    }

}
