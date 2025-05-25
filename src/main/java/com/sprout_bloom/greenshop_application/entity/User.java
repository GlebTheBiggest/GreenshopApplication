package com.sprout_bloom.greenshop_application.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
@Embeddable
@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email", name = "users_email_key"),
                @UniqueConstraint(columnNames = "username", name = "users_username_key"),
                @UniqueConstraint(columnNames = "phone", name = "users_phone_key")
        })
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @NotBlank
    @Email
    private String email;

    @Column(unique = true, nullable = false)
    @NotBlank
    @Size(min = 5)
    private String username;

    @Column(nullable = false)
    @NotBlank
    private String password;

    // 🔹 Роль користувача (ТІЛЬКИ одна роль)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(nullable = false, length = 10)
    @NotBlank
    private String firstName;

    @Column(nullable = false, length = 20)
    @NotBlank
    private String lastName;

    @Column(nullable = false, unique = true)
    @Pattern(regexp = "^\\+48\\s\\d{3}\\s\\d{3}\\s\\d{3}$", message = "Phone must be in format +48 XXX XXX XXX")
    private String phone;

    @Column(length = 254)
    private String address;

    // 🔹 Статус облікового запису (активний чи ні)
    @Column(nullable = false)
    private boolean enabled = true;

    // 🔹 Замовлення, прив'язані до користувача
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Order> orders;

    // 🔹 Автоматично встановлює enabled = true при створенні нового запису
    @PrePersist
    protected void onCreate() {
        this.enabled = true;
    }
}