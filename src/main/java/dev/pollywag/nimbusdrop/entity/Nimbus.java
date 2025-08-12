package dev.pollywag.nimbusdrop.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Nimbus {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "nimbus_space_id", nullable = false)
    private NimbusSpace nimbusSpace;

    @OneToMany(mappedBy = "nimbus", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Drop> drops = new ArrayList<Drop>();

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

    public void setNimbusSpace(NimbusSpace nimbusSpace) {
        this.nimbusSpace = nimbusSpace;
    }
}
