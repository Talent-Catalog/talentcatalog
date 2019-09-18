package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.EduType;
import org.tbbtalent.server.repository.EducationTypeRepository;
import org.tbbtalent.server.service.EducationTypeService;

import java.util.List;

@Service
public class EducationTypeServiceImpl implements EducationTypeService {

    private final EducationTypeRepository educationTypeRepository;

    @Autowired
    public EducationTypeServiceImpl(EducationTypeRepository educationTypeRepository) {
        this.educationTypeRepository = educationTypeRepository;
    }

    @Override
    public List<EduType> listEducationTypes() {
        return educationTypeRepository.findAll();
    }
}
