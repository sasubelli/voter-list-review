package org.example.voterreview.model;

public record RemovedRecord(
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
    public static RemovedRecord fromVoterRecord(VoterRecord record) {
        return new RemovedRecord(
                record.sourceDocument(),
                record.sourceReference(),
                record.voterId(),
                record.name(),
                record.fatherOrSpouseName(),
                record.ageOrDob(),
                record.gender(),
                record.address(),
                record.partNumber(),
                record.serialNumber()
        );
    }

    public VoterRecord asVoterRecord() {
        return new VoterRecord(
                sourceDocument,
                sourceReference,
                voterId,
                name,
                fatherOrSpouseName,
                ageOrDob,
                gender,
                address,
                partNumber,
                serialNumber
        );
    }

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
}
