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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

class DtoBuilderTest {

    DtoBuilder builder;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Issue #1607: Demonstrates bug and fix for bug where Page.size was not being serialized.")
    void buildPage() {
        builder = new DtoBuilder();
        Page<String> page = new PageImpl<>(Arrays.asList("a", "b", "c"), Pageable.ofSize(25), 100);

        final Map<String, Object> map = builder.buildPage(page);
        assertEquals(25, map.get("size"));
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

    private Order createStandardOrder(){
        Customer tim = Customer.builder().
                name("Tim").
                id("T1").
                build();

        OrderItem item1 = OrderItem.builder().
                stockCode("SC1").
                price(1.0).
                quantity(2).
                build()
                ;

        OrderItem item2 = OrderItem.builder().
                stockCode("SC2").
                price(2.0).
                quantity(1).
                build()
                ;

        OrderItem item3 = OrderItem.builder().
                stockCode("SC3").
                price(3.0).
                quantity(10).
                build()
                ;

        Order o = Order.builder().
                id("O1").
                customer(tim).
                items(List.of(item1, item2, item3)).
                build()
                ;

        return o;
    }

    private DtoBuilder createStandardOrderItemDto(DtoCollectionItemFilter<OrderItem> collectionFilter){
        return new DtoBuilder(collectionFilter).
                add(OrderItem.Fields.stockCode).
                add(OrderItem.Fields.price).
                add(OrderItem.Fields.quantity)
                ;
    }

    private DtoBuilder createOrderDto(DtoBuilder itemDtoBuilder){
        return new DtoBuilder().
                add(Order.Fields.id).
                add(Order.Fields.items, itemDtoBuilder)
                ;
    }

    @Test
    void nonFilteredCollection_ReturnsEverything() {
        Order o = createStandardOrder();
        DtoBuilder itemDtoBuilder  = createStandardOrderItemDto(null);
        DtoBuilder orderDtoBuilder  = createOrderDto(itemDtoBuilder);

        Map<String,Object> dto =  orderDtoBuilder.build(o);

        assertNotNull(dto);
        assertEquals(2, dto.size());
        assertEquals(o.id, dto.get(Order.Fields.id));
        assertNotNull(dto.get(Order.Fields.items));
        List<Map<String,Object>> dtoItems = (List<Map<String, Object>>) dto.get(Order.Fields.items);

        List<OrderItem> orderItems = o.getItems();
        assertEquals(orderItems.size(), dtoItems.size());

        for (int i = 0; i < orderItems.size(); i++) {
            OrderItem item = orderItems.get(i);
            Map<String,Object> dtoItem = dtoItems.get(i);
            assertEquals(3, dtoItem.size());
            assertEquals(item.getStockCode(), dtoItem.get(OrderItem.Fields.stockCode));
            assertEquals(item.getPrice(), dtoItem.get(OrderItem.Fields.price));
            assertEquals(item.getQuantity(), dtoItem.get(OrderItem.Fields.quantity));
        }
    }

    @Test
    void filteredCollection_RemovesUnwantedItems() {
        Order o = createStandardOrder();
        DtoBuilder itemDtoBuilder  = createStandardOrderItemDto(i-> i.getPrice() > 1.0);
        DtoBuilder orderDtoBuilder  = createOrderDto(itemDtoBuilder);

        Map<String,Object> dto =  orderDtoBuilder.build(o);

        assertNotNull(dto);
        assertEquals(2, dto.size());
        assertEquals(o.id, dto.get(Order.Fields.id));
        assertNotNull(dto.get(Order.Fields.items));
        List<Map<String,Object>> dtoItems = (List<Map<String, Object>>) dto.get(Order.Fields.items);

        List<OrderItem> orderItems = o.getItems();
        assertEquals(1, dtoItems.size());

        OrderItem item = orderItems.get(0);
        Map<String,Object> dtoItem = dtoItems.get(0);
        assertEquals(3, dtoItem.size());
        assertEquals(item.getStockCode(), dtoItem.get(OrderItem.Fields.stockCode));
        assertEquals(item.getPrice(), dtoItem.get(OrderItem.Fields.price));
        assertEquals(item.getQuantity(), dtoItem.get(OrderItem.Fields.quantity));

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

    @Data @Builder @FieldNameConstants static public class Order{
        private final String id;
        private final Customer customer;
        private final List<OrderItem> items;
    }
    @Data @Builder @FieldNameConstants static public class Customer {
        private final String name;
        private final String id;
    }
    @Data @Builder @FieldNameConstants static public class OrderItem{
        private final String stockCode;
        private final double price;
        private final int quantity;
    }
}
