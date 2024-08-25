package edu.tcu.cs.hogwartsartifactsonline.hogwartsuser;

import edu.tcu.cs.hogwartsartifactsonline.client.rediscache.RedisCacheClient;
import edu.tcu.cs.hogwartsartifactsonline.system.exception.ObjectNotFoundException;
import edu.tcu.cs.hogwartsartifactsonline.system.exception.PasswordChangeIllegalArgumentException;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final RedisCacheClient redisCacheClient;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RedisCacheClient redisCacheClient) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.redisCacheClient = redisCacheClient;
    }

    public List<HogwartsUser> findAll() {
        return userRepository.findAll();
    }

    public HogwartsUser findById(Integer userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException("user", userId));
    }

    public HogwartsUser save(HogwartsUser newUser) {
        newUser.setPassword(this.passwordEncoder.encode(newUser.getPassword()));
        return userRepository.save(newUser);
    }

    public HogwartsUser update(Integer userId, HogwartsUser user) {
        HogwartsUser oldHogwartsUser = userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException("user", userId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // If the user is not an admin, then the user can only update her username
        if (authentication.getAuthorities().stream().noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_admin"))) {
            oldHogwartsUser.setUsername(user.getUsername());
        } else {  // If the user is an admin, then the user can update username, enabled, and roles.
            oldHogwartsUser.setUsername(user.getUsername());
            oldHogwartsUser.setEnabled(user.isEnabled());
            oldHogwartsUser.setRoles(user.getRoles());

            // Revoke this user's current JWT by deleting it from Redis
            this.redisCacheClient.delete("whitelist:" + userId);
        }
        return userRepository.save(oldHogwartsUser);
    }

    public void delete(Integer userId) {
        this.userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException("user", userId));
        this.userRepository.deleteById(userId);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.userRepository.findByUsername(username) // First, we need to find this user from database.
                .map(MyUserPrincipal::new)  // If found, wrap the returned user instance in a MyUserPrinciple instance.
                .orElseThrow(() -> new UsernameNotFoundException("username " + username + " is not found.")); // Otherwise, throw an exception.
    }

    public void changePassword(Integer userId, String oldPassword, String newPassword, String confirmNewPassword) {
        HogwartsUser hogwartsUser = this.userRepository.findById(userId).orElseThrow(() -> new ObjectNotFoundException("user", userId));

        // If the old password is not correct, throw an exception.
        if (!passwordEncoder.matches(oldPassword, hogwartsUser.getPassword())) {
            throw new BadCredentialsException("Old password is incorrect.");
        }

        // If the new password and confirm new password do not match, throw an exception.
        if (!newPassword.equals(confirmNewPassword)) {
            throw new PasswordChangeIllegalArgumentException("New password and confirm new password do not match.");
        }

        // The new password must contain at least one digit, one lowercase letter, one uppercase letter, and be at least 8 characters long.
        String passwordPolicy = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
        if (!newPassword.matches(passwordPolicy)) {
            throw new PasswordChangeIllegalArgumentException("New password does not conform to password policy.");
        }

        // Encode and save the new password.
        hogwartsUser.setPassword(this.passwordEncoder.encode(newPassword));

        // Revoke this user's current JWT by deleting it from Redis
        this.redisCacheClient.delete("whitelist:" + userId);
        this.userRepository.save(hogwartsUser);
    }
}
