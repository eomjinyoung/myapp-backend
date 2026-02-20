package com.example.vibeapp.user;

import com.example.vibeapp.user.dto.UserSignupDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void signup(UserSignupDto signupDto) {
        if (userRepository.existsByEmail(signupDto.email())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        if (!signupDto.isPasswordMatching()) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        User user = new User();
        user.setName(signupDto.name());
        user.setEmail(signupDto.email());
        // BCrypt를 사용하여 비밀번호 암호화 후 저장
        user.setPassword(passwordEncoder.encode(signupDto.password()));

        userRepository.save(user);
    }

    @Transactional
    public void changePassword(String email, com.example.vibeapp.user.dto.PasswordChangeDto passwordChangeDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(passwordChangeDto.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (!passwordChangeDto.isNewPasswordMatching()) {
            throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(passwordChangeDto.newPassword()));
        userRepository.save(user);
    }
}
