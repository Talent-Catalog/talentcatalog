package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.EducationLevel;
import org.tbbtalent.server.repository.EducationLevelRepository;
import org.tbbtalent.server.service.EducationLevelService;

import java.util.List;

@Service
public class EducationLevelServiceImpl implements EducationLevelService {

    private final EducationLevelRepository educationLevelRepository;

    @Autowired
    public EducationLevelServiceImpl(EducationLevelRepository educationLevelRepository) {
        this.educationLevelRepository = educationLevelRepository;
    }

    @Override
    public List<EducationLevel> listEducationLevels() {
        return educationLevelRepository.findAll();
    }
}
