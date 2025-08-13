package com.betul.productmanagement.service;

//user service: kullanıcı kayıt, email var mı yok mu kontrolü, şifrenin hashlenmesi




import com.betul.productmanagement.entity.User;
import com.betul.productmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        // email zaten db de var mı yok mu kontrolü
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use"); //zaten varsa hata verir
        }

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setRole(User.Role.USER); //yeni ilk defa kayıt yaptırıan herkes standart user oluyor
        return userRepository.save(user); //kayıt yapılınca en son burada
        //kullanıcı db ye kaydediliyor
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}

