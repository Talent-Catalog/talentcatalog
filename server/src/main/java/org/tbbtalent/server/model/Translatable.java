package org.tbbtalent.server.model;

import java.io.Serializable;

public interface Translatable<IdType extends Serializable> {

    IdType getId();
    
    String getTranslatedName();
    
    void setTranslatedName(String translatedName);

}
