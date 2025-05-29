package tech.buildrun.unihub.secutiry;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.buildrun.unihub.entity.User;
import tech.buildrun.unihub.repository.UserRepository;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Implementação de UserDetailsService para carregar detalhes do usuário do banco de dados.
 * Usado pelo Spring Security para autenticação.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Carrega os detalhes do usuário pelo nome de usuário.
     * @param username O nome de usuário.
     * @return UserDetails contendo informações do usuário e suas autoridades (roles).
     * @throws UsernameNotFoundException se o usuário não for encontrado.
     */
    @Override
    @Transactional(readOnly = true) // Garante que as roles sejam carregadas dentro da mesma transação
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        // Mapeia as roles do usuário para objetos GrantedAuthority do Spring Security
        Collection<? extends GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        // Retorna um objeto UserDetails do Spring Security
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}
