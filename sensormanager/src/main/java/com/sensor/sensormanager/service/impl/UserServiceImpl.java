package com.sensor.sensormanager.service.impl;

import com.sensor.sensormanager.model.Role;
import com.sensor.sensormanager.model.SensorUser;
import com.sensor.sensormanager.repository.RoleRepository;
import com.sensor.sensormanager.repository.UserRepository;
import com.sensor.sensormanager.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public SensorUser saveUser(SensorUser sensorUser) {
        log.info("saveUser {}", sensorUser.getUsername());

        SensorUser existingUSer = userRepository.findUserByUsername(sensorUser.getUsername());
        if(existingUSer == null) {
            sensorUser.setPassword(passwordEncoder.encode(sensorUser.getPassword()));
            return userRepository.save(sensorUser);
        }
        return existingUSer;

    }

    @Override
    public Role saveRole(Role role) {
        log.info("saveRole {}", role.getName());

        Role existingRole = roleRepository.findByName(role.getName());
        if(existingRole == null) {
            return roleRepository.save(role);
        }
        return existingRole;

    }

    @Override
    public void addRoleToUser(String username, String roleName) {
        SensorUser sensorUser = userRepository.findUserByUsername(username);
        Role role = roleRepository.findByName(roleName);
        log.info("addRole {} ToUser {}", roleName, sensorUser.getUsername());
        Optional<Role> currentRule = sensorUser.getRoles().stream().filter(currentRole -> role.getName().equals(currentRole.getName())).findAny();

        if(currentRule.isEmpty()) {
            sensorUser.getRoles().add(role);
            userRepository.save(sensorUser);
        }

    }

    @Override
    public SensorUser getUser(String username) {
        log.info("getUser {}", username);
        return userRepository.findUserByUsername(username);
    }

    @Override
    public List<SensorUser> getUsers() {
        List<SensorUser> sensorUsers = userRepository.findAll();
        log.info("getUsers {}", sensorUsers);
        return userRepository.findAll();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SensorUser sensorUser = userRepository.findUserByUsername(username);

        if(sensorUser == null) {
            log.error("User {} not found", username);
            throw  new UsernameNotFoundException(String.format("User %s not found", username));
        } else {
            log.info("User {} found", username);
        }

        Collection<SimpleGrantedAuthority> authorities = sensorUser.getRoles().stream().
                map(role -> new SimpleGrantedAuthority(role.getName())).toList();

        return new User(sensorUser.getUsername(), sensorUser.getPassword(), authorities);
    }
}
