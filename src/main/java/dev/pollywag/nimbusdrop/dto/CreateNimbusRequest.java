package dev.pollywag.nimbusdrop.dto;

public class CreateNimbusRequest {
    private String nimbusName;

    public CreateNimbusRequest(String nimbusName) {
        this.nimbusName = nimbusName;
    }

    public String getNimbusName() {
        return nimbusName;
    }
    public void setNimbusName(String nimbusName) {
        this.nimbusName = nimbusName;
    }
}
