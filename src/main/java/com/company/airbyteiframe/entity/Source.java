package com.company.airbyteiframe.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@JmixEntity
@Table(name = "SOURCE")
@Entity
public class Source extends BaseEntity {
    @Column(name = "SOURCE_ID")
    private UUID sourceId;

    @Column(name = "PROVIDER_UNIT")
    private String providerUnit;

    public ProviderUnit getProviderUnit() {
        return providerUnit == null ? null : ProviderUnit.fromId(providerUnit);
    }

    public void setProviderUnit(ProviderUnit providerUnit) {
        this.providerUnit = providerUnit == null ? null : providerUnit.getId();
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public void setSourceId(UUID sourceId) {
        this.sourceId = sourceId;
    }

}