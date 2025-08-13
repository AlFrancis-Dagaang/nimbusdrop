package dev.pollywag.nimbusdrop.dto.respondeDTO;

public class NimbusResponse {
    private Long id;
    private String nimbusName;

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
