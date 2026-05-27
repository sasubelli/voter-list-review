package org.example.voterreview.model;

public record VoterRecord(
        String sourceDocument,
        String sourceReference,
        String voterId,
        String name,
        String fatherOrSpouseName,
        String ageOrDob,
        String gender,
        String address,
        String partNumber,
        String serialNumber
) {
    public String getSourceDocument() {
        return sourceDocument;
    }

    public String getSourceReference() {
        return sourceReference;
    }

    public String getVoterId() {
        return voterId;
    }

    public String getName() {
        return name;
    }

    public String getFatherOrSpouseName() {
        return fatherOrSpouseName;
    }

    public String getAgeOrDob() {
        return ageOrDob;
    }

    public String getGender() {
        return gender;
    }

    public String getAddress() {
        return address;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getDisplayName() {
        return name == null || name.isBlank() ? "(Unnamed record)" : name;
    }
}
