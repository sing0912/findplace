package com.petpro.e2e.support;

import com.petpro.domain.auth.entity.AuthProvider;
import com.petpro.domain.user.entity.User;
import com.petpro.domain.user.entity.UserRole;
import com.petpro.domain.user.entity.UserStatus;
import com.petpro.domain.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminTestHelper {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTestHelper authTestHelper;

    private static final String DEFAULT_PASSWORD = "Admin1234!";

    public AdminTestHelper(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthTestHelper authTestHelper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authTestHelper = authTestHelper;
    }

    public AuthTestHelper.AuthResult createAdminAndLogin(String baseUrl, String email) {
        createUserDirectly(email, "관리자", "admin_" + email.split("@")[0], UserRole.ADMIN);
        return authTestHelper.login(baseUrl, email, DEFAULT_PASSWORD);
    }

    public AuthTestHelper.AuthResult createSuperAdminAndLogin(String baseUrl, String email) {
        createUserDirectly(email, "최고관리자", "sadmin_" + email.split("@")[0], UserRole.SUPER_ADMIN);
        return authTestHelper.login(baseUrl, email, DEFAULT_PASSWORD);
    }

    public User createUserDirectly(String email, String name, String nickname, UserRole role) {
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .name(name)
                .nickname(nickname)
                .phone(null)
                .provider(AuthProvider.EMAIL)
                .agreeTerms(true)
                .agreePrivacy(true)
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();
        return userRepository.saveAndFlush(user);
    }
}
