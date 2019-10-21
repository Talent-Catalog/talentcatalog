package org.tbbtalent.server.service;

import java.util.List;

import org.tbbtalent.server.model.Translatable;

public interface TranslationService {

    public <T extends Translatable> void translate(List<T> items,
                                                   String type);

}
