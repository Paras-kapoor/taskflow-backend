package com.paras.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
     Long id;

    @Column(nullable = false)
     String name;

    @Column(unique = true, nullable = false)
     String email;

    @Column(nullable = false)
     String password;  // always stored BCrypt-hashed, never plain text

    @CreationTimestamp
     LocalDateTime createdAt;

    // Bidirectional — lets you do user.getOwnedProjects() if needed
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    @JsonIgnore  // prevent infinite recursion in JSON serialization
     List<Project> ownedProjects;
}