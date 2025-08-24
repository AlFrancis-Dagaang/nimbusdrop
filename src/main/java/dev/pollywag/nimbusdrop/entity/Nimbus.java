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
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "nimbus", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Drop> drops = new ArrayList<Drop>();

    public Nimbus() {}
    public Nimbus(String nimbusName, User user) {
        this.nimbusName = nimbusName;
        this.user = user;
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

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public String getNimbusName() {
        return nimbusName;
    }

    public void setNimbusName(String nimbusName) {
        this.nimbusName = nimbusName;
    }

}
