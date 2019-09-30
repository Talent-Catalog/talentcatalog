package org.tbbtalent.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.Occupation;
import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.repository.OccupationRepository;
import org.tbbtalent.server.service.OccupationService;

import java.util.List;

@Service
public class OccupationServiceImpl implements OccupationService {

    private final OccupationRepository occupationRepository;

    @Autowired
    public OccupationServiceImpl(OccupationRepository occupationRepository) {
        this.occupationRepository = occupationRepository;
    }

    @Override
    public List<Occupation> listOccupations() {
        return occupationRepository.findByStatus(Status.active);
    }
}
