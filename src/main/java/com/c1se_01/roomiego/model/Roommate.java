package com.c1se_01.roomiego.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roommates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Roommate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String gender;

    @Column
    private String hometown;

    @Column
    private String city;

    @Column
    private String district;

    @Column
    private Integer rateImage;

    @Column
    private Integer yob;

    @Column(length = 20)
    private String phone;
    @Column
    private String job;

    @Column
    private String hobbies;

    @Column(columnDefinition = "TEXT")
    private String more;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
}
