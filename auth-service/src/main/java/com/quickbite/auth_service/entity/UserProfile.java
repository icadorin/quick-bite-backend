package com.quickbite.auth_service.entity;

import com.quickbite.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@Table(name = "user_profiles")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class UserProfile extends BaseEntity {

    @Id
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 20)
    private String phone;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column
    private String address;

    @Column(name = "preferred_language", length = 10, nullable = false)
    @Builder.Default
    private String preferredLanguage = "pt_BR";

    @Column(name = "notification_preferences", columnDefinition = "TEXT")
    @Builder.Default
    private String notificationPreferences = "{\"email\": true, \"sms\": false}";
}
