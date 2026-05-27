package org.example.voterreview.model;

import jakarta.validation.constraints.NotBlank;

public class DeceasedSearchRequest {
    @NotBlank(message = "Name is required")
    private String name;
    private String voterId;
    private String ageOrDob;
    private String address;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVoterId() {
        return voterId;
    }

    public void setVoterId(String voterId) {
        this.voterId = voterId;
    }

    public String getAgeOrDob() {
        return ageOrDob;
    }

    public void setAgeOrDob(String ageOrDob) {
        this.ageOrDob = ageOrDob;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
