package dev.pollywag.nimbusdrop.dto.respondeDTO;

public class NimbusResponse {
    private Long id;
    private Long nimbusSpaceId;
    private String nimbusName;


    public Long getNimbusSpaceId() {
        return nimbusSpaceId;
    }

    public void setNimbusSpaceId(Long nimbusSpaceId) {
        this.nimbusSpaceId = nimbusSpaceId;
    }

    public NimbusResponse(){
        super();
    }

    public String getNimbusName() {
        return nimbusName;
    }

    public void setNimbusName(String nimbusName) {
        this.nimbusName = nimbusName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


}
