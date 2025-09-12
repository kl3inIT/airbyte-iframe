package com.company.airbyteiframe.entity;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum ProviderUnit implements EnumClass<String> {

    EQUIPMENT_DEPARTMENT("EQUIPMENT_DEPARTMENT"),
    MILITARY_PERSONNEL("MILITARY_PERSONNEL"),
    ENGINEERING("ENGINEERING"),
    POLITICAL_DEPARTMENT("POLITICAL_DEPARTMENT"),
    PERSONNEL_DEPARTMENT("PERSONNEL_DEPARTMENT"),
    TRAINING_DEPARTMENT("TRAINING_DEPARTMENT"),
    MILITARY_BORDERS("MILITARY_BORDERS"),
    RESERVE_DEPARTMENT("RESERVE_DEPARTMENT"),
    MILITARY_ACADEMY("MILITARY_ACADEMY"),
    MEDICAL_DEPARTMENT("MEDICAL_DEPARTMENT"),
    GENERAL_STAFF("GENERAL_STAFF"),
    LOGISTICS_DEPARTMENT("LOGISTICS_DEPARTMENT");

    private final String id;

    ProviderUnit(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static ProviderUnit fromId(String id) {
        for (ProviderUnit at : ProviderUnit.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}