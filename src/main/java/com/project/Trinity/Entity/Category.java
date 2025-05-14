
package com.project.Trinity.Entity;

import jakarta.persistence.*;
import lombok.Data;



@Entity
@Table(name = "category")
@Data
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;


    @Column(columnDefinition = "TEXT")
    private String logo; // Base64 string olarak saklanacak

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
}