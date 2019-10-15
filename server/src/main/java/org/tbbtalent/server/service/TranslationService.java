package org.tbbtalent.server.service;

import java.util.List;

import org.tbbtalent.server.model.Country;

public interface TranslationService {

    List<Country> translate(List<Country> countries);

}
