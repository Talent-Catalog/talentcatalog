package org.tbbtalent.server.service;

import java.util.List;

import org.tbbtalent.server.model.AbstractTranslatableDomainObject;

public interface TranslationService {

    public <T extends AbstractTranslatableDomainObject<Long>> void translate(List<T> items,
                                                                             String type);

}
