package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.EducationMajor;
import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.repository.EducationMajorRepository;
import org.tbbtalent.server.service.EducationMajorService;

import java.util.List;

@Service
public class EducationMajorServiceImpl implements EducationMajorService {

    private final EducationMajorRepository educationMajorRepository;

    @Autowired
    public EducationMajorServiceImpl(EducationMajorRepository educationMajorRepository) {
        this.educationMajorRepository = educationMajorRepository;
    }

    @Override
    public List<EducationMajor> listActiveEducationMajors() {
        return educationMajorRepository.findByStatus(Status.active);
    }
}
