package org.tbbtalent.server.model;

import java.math.BigDecimal;
import java.math.BigInteger;

public class DataRow {
    String label;
    BigDecimal value;

    public DataRow(String label, BigInteger value) {
        this.label = label;
        this.value = new BigDecimal(value);
    }

    public DataRow(String label, Long value) {
        this.label = label;
        this.value = new BigDecimal(value);
    }

    public DataRow(String label, BigDecimal value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
