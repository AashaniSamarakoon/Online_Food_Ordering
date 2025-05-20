package com.example.restaurantauth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "restaurant_admins")
public class RestaurantAdmin implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String restaurantName;

    @Column(nullable = false)
    private String ownerName;

    @Column(nullable = false, unique = true)
    private String nic;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private String bankAccountOwner;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private String branchName;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Column(nullable = false)
    private boolean isVerified;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.RESTAURANT_ADMIN; // Default value // ADMIN, RESTAURANT_ADMIN, SUPER_ADMIN, etc.

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isVerified;
    }

    // Corrected method to actually set the isVerified field
    public void setIsVerified(boolean verified) {
        this.isVerified = verified;
    }

    public enum Role {
        SUPER_ADMIN,
        RESTAURANT_ADMIN
    }
}