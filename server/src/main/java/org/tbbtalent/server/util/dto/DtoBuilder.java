package org.tbbtalent.server.util.dto;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.domain.Page;
import org.tbbtalent.server.model.StatReport;
import org.tbbtalent.server.model.Translatable;

public class DtoBuilder {

    private List<MappedProperty> mappedProperties;
    private Boolean skipTranslation  = false;


    public DtoBuilder() {
        this.mappedProperties = new ArrayList<>();
    }

    public DtoBuilder(Boolean skipTranslation) {
        this.skipTranslation = skipTranslation;
        this.mappedProperties = new ArrayList<>();

    }

    public DtoBuilder add(String property) {
        this.mappedProperties.add(new MappedProperty(property));
        return this;
    }

    public DtoBuilder add(String property, DtoBuilder builder) {
        this.mappedProperties.add(new MappedProperty(property, builder));
        return this;
    }

    public DtoBuilder merge(DtoBuilder builder) {
        this.mappedProperties.addAll(builder.mappedProperties);
        return this;
    }

    public List<Map<String, Object>> buildList(Collection<?> sourceList) {
        if (sourceList != null) {
            List<Map<String, Object>> results = new ArrayList<>();
            for (Object source : sourceList) {
                results.add(build(source));
            }
            return results;
        } else {
            return null;
        }
    }

    public Map<String, Object> buildPage(Page<?> page) {
        if (page != null) {
            Map<String, Object> result = new HashMap<>();
            result.put("totalElements", page.getTotalElements());
            result.put("totalPages", page.getTotalPages());
            result.put("number", page.getNumber());
            result.put("numberOfElements", page.getNumberOfElements());
            result.put("hasNext", page.hasNext());
            result.put("hasPrevious", page.hasPrevious());
            result.put("content", buildList(page.getContent()));
            return result;
        } else {
            return null;
        }
    }

    public Map<String, Object> buildReport(StatReport statReport) {
        if (statReport != null) {
            Map<String, Object> result = new HashMap<>();
            result.put("name", statReport.getName());
            result.put("chartType", statReport.getChartType());
            result.put("rows", buildList(statReport.getRows()));
            return result;
        } else {
            return null;
        }
    }

    public Map<String, Object> build(Object source) {

        if (source == null) {
            return null;
        }

        String propertyToTranslate = null;
        String translationContainingTranslation = null;
        if (source.getClass().isAnnotationPresent(Translatable.class)) {
            Translatable translatable = source.getClass().getAnnotation(Translatable.class);
            propertyToTranslate = translatable.value();
            translationContainingTranslation = translatable.translation();
        }

        Map<String, Object> map = new HashMap<>();
        for (MappedProperty property : mappedProperties) {
            
            // intercept translations, if needed
            String propertyName = property.name;
            if (BooleanUtils.isFalse(skipTranslation) && propertyToTranslate != null && propertyToTranslate.equals(property.name)) {
                propertyName = translationContainingTranslation;
            }
            
            Object value = null;
            try {
                value = PropertyUtils.getProperty(source, propertyName);
            } catch (IllegalAccessException e) {
                throw new DtoBuilderException("Unable to access property '" + property.name
                        + "' on " + source.getClass().getSimpleName(), e);
            } catch (InvocationTargetException e) {
                throw new DtoBuilderException("Error while accessing property '" + property.name
                        + "' on " + source.getClass().getSimpleName(), e);
            } catch (NoSuchMethodException e) {
                throw new DtoBuilderException("Property '" + property.name
                        + "' does not exist on " + source.getClass().getSimpleName(), e);
            }

            if (value != null) {
                if (property.builder != null) {
                    if (value instanceof Collection) {
                        map.put(property.name, property.builder.buildList((Collection<?>) value));
                    } else {
                        map.put(property.name, property.builder.build(value));
                    }
                } else {
                    map.put(property.name, value);
                }
            }

        }
        return map;
    }

    private static final class MappedProperty {

        String name;
        DtoBuilder builder;

        MappedProperty(String name) {
            this.name = name;
        }

        MappedProperty(String name, DtoBuilder builder) {
            this.name = name;
            this.builder = builder;
        }
    }

}
