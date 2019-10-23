package org.tbbtalent.server.service;

import java.util.List;

import org.tbbtalent.server.model.AbstractTranslatableDomainObject;

public interface TranslationService {

    <T extends AbstractTranslatableDomainObject<Long>> void translate(List<T> items,  String type);

    <T extends AbstractTranslatableDomainObject<Long>> void translate(List<T> items, String type, String language);
}
