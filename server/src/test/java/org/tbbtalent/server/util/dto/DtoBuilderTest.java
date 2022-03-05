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
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DtoBuilderTest {

    DtoBuilder builder;

    @BeforeEach
    void setUp() {
    }

    @Test
    void missingDiscriminatorProperty() {
        builder = new DtoBuilder();

        try {
            builder.add("prop1", DiscriminatorValues.type1);
            fail("Expected DtoBuilderException noting missing discriminator property");
        } catch (Exception ex) {
            assertEquals(DtoBuilderException.class, ex.getClass());
        }
    }

    @Test
    void discriminators() {
        List<ClassWithDiscriminator> cwdList = new ArrayList<>();

        ClassWithDiscriminator cwd = new ClassWithDiscriminator();
        cwd.setDiscriminator(DiscriminatorValues.type1);
        cwdList.add(cwd);

        SubClass cwd2 = new SubClass();
        cwd2.setDiscriminator(DiscriminatorValues.type2);
        cwdList.add(cwd2);

        builder = new DtoBuilder("discriminator");

        builder.add("prop1");
        builder.add("prop2", DiscriminatorValues.type2);

        Map<String, Object> map;

        map = builder.build(cwd);
        assertNotNull(map);
        assertEquals(0, map.entrySet().size());

        cwd.setProp1("value1");

        map = builder.build(cwd);
        assertNotNull(map);
        assertEquals(1, map.entrySet().size());

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
