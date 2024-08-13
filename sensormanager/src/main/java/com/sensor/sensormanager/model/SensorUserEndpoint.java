package com.sensor.sensormanager.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SensorUserEndpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Date date;

    @Column(nullable = false)
    private String sensorId;

//    @ManyToOne
//    private SensorUser userId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private long value;

}
