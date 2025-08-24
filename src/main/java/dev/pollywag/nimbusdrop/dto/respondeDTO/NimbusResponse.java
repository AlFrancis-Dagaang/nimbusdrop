package dev.pollywag.nimbusdrop.dto.respondeDTO;

public class NimbusResponse {
    private Long id;
    private Long userId;
    private String nimbusName;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
