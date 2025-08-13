package dev.pollywag.nimbusdrop.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="nimbus")
public class Nimbus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "name is required")
    private String nimbusName;

    @ManyToOne
    @JoinColumn(name = "nimbus_space_id", nullable = false)
    private NimbusSpace nimbusSpace;

    @OneToMany(mappedBy = "nimbus", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Drop> drops = new ArrayList<Drop>();

    public Nimbus() {}
    public Nimbus(String nimbusName, NimbusSpace nimbusSpace) {
        this.nimbusName = nimbusName;
        this.nimbusSpace = nimbusSpace;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Drop> getDrops() {
        return drops;
    }

    public void setDrops(List<Drop> drops) {
        this.drops = drops;
    }

    public NimbusSpace getNimbusSpace() {
        return nimbusSpace;
    }

    public String getNimbusName() {
        return nimbusName;
    }

    public void setNimbusName(String nimbusName) {
        this.nimbusName = nimbusName;
    }

    public void setNimbusSpace(NimbusSpace nimbusSpace) {
        this.nimbusSpace = nimbusSpace;
    }
}
