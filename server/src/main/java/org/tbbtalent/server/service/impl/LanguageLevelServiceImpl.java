package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.LanguageLevel;
import org.tbbtalent.server.repository.LanguageLevelRepository;
import org.tbbtalent.server.service.LanguageLevelService;

import java.util.List;

@Service
public class LanguageLevelServiceImpl implements LanguageLevelService {

    private final LanguageLevelRepository languageLevelRepository;

    @Autowired
    public LanguageLevelServiceImpl(LanguageLevelRepository languageLevelRepository) {
        this.languageLevelRepository = languageLevelRepository;
    }

    @Override
    public List<LanguageLevel> listLanguageLevels() {
        return languageLevelRepository.findAll();
    }
}
