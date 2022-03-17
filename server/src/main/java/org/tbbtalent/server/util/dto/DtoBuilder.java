/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.util.dto;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.model.db.StatReport;
import org.tbbtalent.server.model.db.Translatable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Converts objects to a Map of Strings to values, where the values can
 * be Strings or other Maps - ie Map<String, String|Map>
 * This hierarchical structure maps well on to Json which is used to
 * return responses over HTTP.
 */
public class DtoBuilder {

    private final List<MappedProperty> mappedProperties;
    private final String discriminatorProperty;
    private final Boolean skipTranslation;

    /**
     * Creates a builder which can optionally skip translation and support a discriminator property.
     * @param skipTranslation If true, the built in translation capability will be deactivated
     * @param discriminatorProperty If not null, properties can be ignored if their object does
     *                              contain a property with this name which has a specific value.
     *                              See {@link #add(String, Object, DtoBuilder)}. This is useful
     *                              for conditionally processing properties of sub classes where
     *                              subclass types are identified by means of a discriminator
     *                              property. This is useful because otherwise class information
     *                              is lost in the conversion to Maps of String, Values.
     */
    public DtoBuilder(@Nullable Boolean skipTranslation, @Nullable String discriminatorProperty) {
        this.mappedProperties = new ArrayList<>();
        this.skipTranslation = skipTranslation;
        this.discriminatorProperty = discriminatorProperty;
    }

    public DtoBuilder() {
        this(false, null);
    }

    public DtoBuilder(Boolean skipTranslation) {
        this(skipTranslation, null);
    }

    public DtoBuilder(String discriminatorProperty) {
        this(false, discriminatorProperty);
    }

    /**
     * Requests that the builder should extract the property with the given name from the object.
     * <p/>
     * If a discriminator value is specified and this builder has been created specifying a
     * discriminator property (see {@link #DtoBuilder(String)}), the property will only be extracted
     * if the object's discriminator property has this value.
     * <p/>
     * If builder is specified, the property will be extracted using the given builder.
     * @param property Property to be extracted
     * @param discriminatorValue If not null, the property will be ignored unless the object's
     *                           discriminator property value equals this value.
     * @param builder If not null, it specifies that the property should be extracted using the
     *                given builder.
     * @return this builder
     * @throws DtoBuilderException if a discriminatorValue is specified but no discriminator
     * property has been specified for this builder.
     */
    public DtoBuilder add(String property,
        @Nullable Object discriminatorValue, @Nullable DtoBuilder builder) {
        if (discriminatorValue != null && discriminatorProperty == null) {
            throw new DtoBuilderException("No discriminator property has been defined for this builder");
        }
        this.mappedProperties.add(new MappedProperty(property, discriminatorValue, builder));
        return this;
    }

    public DtoBuilder add(String property) {
        add(property, null, null);
        return this;
    }

    public DtoBuilder add(String property, @Nullable Object discriminatorValue) {
        add(property, discriminatorValue, null);
        return this;
    }

    public DtoBuilder add(String property, @Nullable DtoBuilder builder) {
        add(property, null, builder);
        return this;
    }

    public DtoBuilder merge(DtoBuilder builder) {
        this.mappedProperties.addAll(builder.mappedProperties);
        return this;
    }

    public @Nullable List<Map<String, Object>> buildList(
            @Nullable Collection<?> sourceList) {
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

    public @Nullable Map<String, Object> buildPage(@Nullable Page<?> page) {
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

    public @Nullable Map<String, Object> buildReport(
            @Nullable StatReport statReport) {
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
     * @param source Object to be converted to a JSon style Map<String, String|Map>
     * @return Null if source is null
     */
    public @Nullable Map<String, Object> build(@Nullable Object source) {

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

            //Skip if property is dependent on discriminatorProperty but discriminator value does
            //not match. Property can be ignored.
            boolean ignoreProperty = false;
            if (discriminatorProperty != null && property.discriminatorValue != null) {
                Object discriminatorValue = getPropertyValue(source, discriminatorProperty);
                ignoreProperty = !property.discriminatorValue.equals(discriminatorValue);
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
        Object discriminatorValue;
        DtoBuilder builder;

        MappedProperty(String name, Object discriminatorValue, DtoBuilder builder) {
            this.name = name;
            this.builder = builder;
            this.discriminatorValue = discriminatorValue;
        }

        MappedProperty(String name) {
            this(name, null, null);
        }

        MappedProperty(String name, DtoBuilder builder) {
            this(name, null, builder);
        }

        MappedProperty(String name, Object discriminatorValue) {
            this(name, discriminatorValue, null);
        }
    }

}
