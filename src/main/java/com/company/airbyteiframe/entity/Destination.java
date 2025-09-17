package com.company.airbyteiframe.entity;

import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@JmixEntity
@Table(name = "DESTINATION")
@Entity
public class Destination extends BaseEntity {
    @Column(name = "DESTINATION_ID")
    private String destinationId;

    @Column(name = "DESTINATION_TYPE")
    private String destinationType;


    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public String getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(String destinationType) {
        this.destinationType = destinationType;
    }

}
