package com.company.airbyteiframe.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@JmixEntity
@Table(name = "SOURCE")
@Entity
public class Source extends BaseEntity {
    @Column(name = "SOURCE_ID")
    private String sourceId;

    @Column(name = "SOURCE_TYPE")
    private String sourceType;

    @Column(name = "PROVIDER_UNIT")
    private String providerUnit;

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public ProviderUnit getProviderUnit() {
        return providerUnit == null ? null : ProviderUnit.fromId(providerUnit);
    }

    public void setProviderUnit(ProviderUnit providerUnit) {
        this.providerUnit = providerUnit == null ? null : providerUnit.getId();
    }

}