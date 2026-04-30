package com.rama.cloudstorage.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @Column(columnDefinition = "uuid", updatable = false)
    UUID id;

    @Column(nullable = false, unique = true)
    String username;

    @Column(nullable = false)
    String password;

    public static User create(String username, String password) {
        return User.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .username(username)
                .password(password)
                .build();
    }
}
