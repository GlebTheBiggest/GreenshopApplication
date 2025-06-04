package com.sprout_bloom.greenshop_application.entity;

import com.sprout_bloom.greenshop_application.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Username is required")
    @Size(min = 5, message = "Username must be at least 5 characters")
    private String username;

    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    private String password;

    // 🔹 Роль користувача (одна роль)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, length = 10)
    @NotBlank(message = "First name is required")
    private String firstName;

    @Column(nullable = false, length = 20)
    @NotBlank(message = "Last name is required")
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