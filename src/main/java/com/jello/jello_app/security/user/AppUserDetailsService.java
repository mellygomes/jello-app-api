package com.jello.jello_app.security.user;

import com.jello.jello_app.user.model.User;
import com.jello.jello_app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = Optional.ofNullable(userRepository.findByUsername(username))
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado!"));
        return AppUserDetails.buildUserDetails(user);
    }

    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
       User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado!"));
        return AppUserDetails.buildUserDetails(user);
    }
}
