package dev.pollywag.nimbusdrop.entity;

import jakarta.persistence.*;

@Entity
public class Nimbus {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "nimbus_space_id", nullable = false)
    private NimbusSpace nimbusSpace;

}
