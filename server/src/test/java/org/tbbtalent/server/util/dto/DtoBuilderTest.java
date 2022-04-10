/*
 * Copyright (c) 2022 Talent Beyond Boundaries.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DtoBuilderTest {

    DtoBuilder builder;

    @BeforeEach
    void setUp() {
    }

    static public class DiscriminatorPropertyFilter implements DtoPropertyFilter {
        private final String discriminatorProperty;
        private final Set<String> ignorableProperties =
            new HashSet<>(Arrays.asList("prop2", "prop3"));

        public DiscriminatorPropertyFilter(String discriminatorProperty) {
            this.discriminatorProperty = discriminatorProperty;
        }

        public boolean ignoreProperty(Object o, String property) {
            boolean ignore = false;
            //Only type 2 discriminators have property prop2
            if (ignorableProperties.contains(property)) {
                Object discriminatorValue = getProperty(o, discriminatorProperty);
                ignore = !DiscriminatorValues.type2.equals(discriminatorValue);
            }
            return ignore;
        }
    };

    @Test
    void propertyFilter() {
        List<ClassWithDiscriminator> cwdList = new ArrayList<>();

        ClassWithDiscriminator cwd = new ClassWithDiscriminator();
        cwd.setDiscriminator(DiscriminatorValues.type1);
        cwdList.add(cwd);

        SubClass cwd2 = new SubClass();
        cwd2.setDiscriminator(DiscriminatorValues.type2);
        cwdList.add(cwd2);

        DtoPropertyFilter propertyFilter = new DiscriminatorPropertyFilter("discriminator");
        builder = new DtoBuilder(propertyFilter);
        builder.add("prop1");
        //Note that we don't make the property optional here - that happens in the filter
        builder.add("prop2");

        doCommonTest(cwd, cwd2, cwdList);


    }

    private void doCommonTest(
        ClassWithDiscriminator cwd, SubClass cwd2,
        List<ClassWithDiscriminator> cwdList) {
        Map<String, Object> map;

        map = builder.build(cwd);
        assertNotNull(map);
        assertEquals(0, map.entrySet().size());

        cwd.setProp1("value1");

        map = builder.build(cwd);
        assertNotNull(map);
        assertEquals(1, map.entrySet().size());

        map = builder.build(cwd2);
        assertNotNull(map);
        assertEquals(0, map.entrySet().size());

        cwd2.setProp1("value1");

        map = builder.build(cwd2);
        assertNotNull(map);
        assertEquals(1, map.entrySet().size());

        cwd2.setProp2("value2");

        map = builder.build(cwd2);
        assertNotNull(map);
        assertEquals(2, map.entrySet().size());

        List<Map<String, Object>> mapList;

        mapList = builder.buildList(cwdList);
        assertNotNull(mapList);
        assertEquals(2, mapList.size());

        cwd2.setProp2("value2");

        mapList = builder.buildList(cwdList);
        assertNotNull(mapList);
        assertEquals(2, mapList.size());
    }

    enum DiscriminatorValues {
        type1,
        type2
    }
    static public class SubClass extends ClassWithDiscriminator {
        private String prop2;

        public String getProp2() {
            return prop2;
        }

        public void setProp2(String prop2) {
            this.prop2 = prop2;
        }
    }
    static public class ClassWithDiscriminator {
        private DiscriminatorValues discriminator;
        private String prop1;

        public DiscriminatorValues getDiscriminator() {
            return discriminator;
        }

        public void setDiscriminator(
            DiscriminatorValues discriminator) {
            this.discriminator = discriminator;
        }

        public String getProp1() {
            return prop1;
        }

        public void setProp1(String prop1) {
            this.prop1 = prop1;
        }
    }
}
