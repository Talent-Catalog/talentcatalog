/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.util.dto;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.StatReport;
import org.tctalent.server.model.db.Translatable;

/**
 * Converts objects to a Map of Strings to values, where the values can
 * be Strings or other Maps - ie Map<String, String|Map>
 * This hierarchical structure maps well on to Json which is used to
 * return responses over HTTP.
 */
public class DtoBuilder {

    private final List<MappedProperty> mappedProperties;
    private final Boolean skipTranslation;
    private final DtoPropertyFilter propertyFilter;
    private final DtoCollectionItemFilter collectionItemFilter;

    /**
     * Creates a builder which can optionally skip translation and support a discriminator property.
     *
     * @param skipTranslation       If true, the built in translation capability will be deactivated
     * @param propertyFilter        If not null, this object will determine which properties should be
     *                              ignored.
     * @param collectionItemFilter  If not null, this object will determine which items should be
     *                              ignored.
     */
    public DtoBuilder(@Nullable Boolean skipTranslation,
                      @Nullable DtoPropertyFilter propertyFilter,
                      @Nullable DtoCollectionItemFilter collectionItemFilter) {
        this.mappedProperties = new ArrayList<>();
        this.skipTranslation = skipTranslation;
        this.propertyFilter = propertyFilter;
        this.collectionItemFilter = collectionItemFilter;
    }

    public DtoBuilder() {
        this(false, null, null);
    }

    public DtoBuilder(Boolean skipTranslation) {
        this(skipTranslation, null, null);
    }

    public DtoBuilder(DtoPropertyFilter propertyFilter) {
        this(false, propertyFilter, null);
    }

    public DtoBuilder(DtoCollectionItemFilter collectionItemFilter) {
        this(false, null, collectionItemFilter);
    }

    /**
     * Requests that the builder should extract the property with the given name from the object.
     * <p/>
     * If builder is specified, the property will be extracted using the given builder.
     * @param property Property to be extracted
     * @param builder If not null, it specifies that the property should be extracted using the
     *                given builder.
     * @return this builder
     */
    public DtoBuilder add(String property, @Nullable DtoBuilder builder) {
        this.mappedProperties.add(new MappedProperty(property, builder));
        return this;
    }

    public DtoBuilder add(String property) {
        add(property, null);
        return this;
    }

    public DtoBuilder merge(DtoBuilder builder) {
        this.mappedProperties.addAll(builder.mappedProperties);
        return this;
    }

    public @NonNull List<Map<String, Object>> buildList(
            @Nullable Collection<?> sourceList) {
        List<Map<String, Object>> results = new ArrayList<>();
        if (sourceList != null) {
            for (Object source : sourceList) {
                if( collectionItemFilter == null || !collectionItemFilter.ignoreItem(source)) {
                    results.add(build(source));
                }
            }
        }
        return results;
    }

    public @NonNull Map<String, Object> buildPage(@Nullable Page<?> page) {
        Map<String, Object> result = new HashMap<>();
        if (page != null) {
            result.put("totalElements", page.getTotalElements());
            result.put("totalPages", page.getTotalPages());
            result.put("size", page.getSize());
            result.put("number", page.getNumber());
            result.put("numberOfElements", page.getNumberOfElements());
            result.put("hasNext", page.hasNext());
            result.put("hasPrevious", page.hasPrevious());
            result.put("content", buildList(page.getContent()));
        }
        return result;
    }

    public @NonNull Map<String, Object> buildReport(
            @Nullable StatReport statReport) {
        Map<String, Object> result = new HashMap<>();
        if (statReport != null) {
            result.put("name", statReport.getName());
            result.put("chartType", statReport.getChartType());
            result.put("rows", buildList(statReport.getRows()));
        }
        return result;
    }

    private Object getPropertyValue(@NonNull Object source, @NonNull String propertyName)
        throws DtoBuilderException {
        Object value;
        try {
            value = PropertyUtils.getProperty(source, propertyName);
        } catch (IllegalAccessException e) {
            throw new DtoBuilderException("Unable to access property '" + propertyName
                + "' on " + source.getClass().getSimpleName(), e);
        } catch (InvocationTargetException e) {
            throw new DtoBuilderException("Error while accessing property '" + propertyName
                + "' on " + source.getClass().getSimpleName(), e);
        } catch (NoSuchMethodException e) {
            throw new DtoBuilderException("Property '" + propertyName
                + "' does not exist on " + source.getClass().getSimpleName(), e);
        }
        return value;
    }

    /**
     * Converts object to a Map of Strings to values, where the values can
     * be Strings or other Maps.
     * This hierarchical structure maps well on to Json which is used to
     * return responses over HTTP.
     *
     * @param source Object to be converted to a JSon style Map<String, String|Map>
     * @return Empty map if source is null
     */
    public @NonNull Map<String, Object> build(@Nullable Object source) {
        Map<String, Object> map = new HashMap<>();

        if (source == null) {
            //Return empty map
            return map;
        }

        String propertyToTranslate = null;
        String translationContainingTranslation = null;
        if (source.getClass().isAnnotationPresent(Translatable.class)) {
            Translatable translatable = source.getClass().getAnnotation(Translatable.class);
            propertyToTranslate = translatable.value();
            translationContainingTranslation = translatable.translation();
        }

        for (MappedProperty property : mappedProperties) {

            //Skip if property is excluded by a propertyFilter. Property is ignored.
            boolean ignoreProperty = false;
            if (propertyFilter != null) {
                ignoreProperty = propertyFilter.ignoreProperty(source, property.name);
            }

            if (!ignoreProperty) {

                // intercept translations, if needed
                String propertyName = property.name;
                if (BooleanUtils.isFalse(skipTranslation) && propertyToTranslate != null
                    && propertyToTranslate.equals(property.name)) {
                    propertyName = translationContainingTranslation;
                }

                Object value = getPropertyValue(source, propertyName);
                if (value != null) {
                    if (property.builder != null) {
                        if (value instanceof Collection) {
                            map.put(property.name,
                                property.builder.buildList((Collection<?>) value));
                        } else {
                            map.put(property.name, property.builder.build(value));
                        }
                    } else {
                        map.put(property.name, value);
                    }
                }
            }

        }
        return map;
    }

    private static final class MappedProperty {

        String name;
        DtoBuilder builder;

        MappedProperty(String name, DtoBuilder builder) {
            this.name = name;
            this.builder = builder;
        }

        MappedProperty(String name) {
            this(name, null);
        }

    }

}
