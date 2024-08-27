package com.sensor.sensormanager.service.impl;

import com.sensor.sensormanager.model.Role;
import com.sensor.sensormanager.model.SensorUser;
import com.sensor.sensormanager.repository.RoleRepository;
import com.sensor.sensormanager.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() throws Exception {

        try (final AutoCloseable autoCloseable = MockitoAnnotations.openMocks(this)) {
            userService = new UserServiceImpl(userRepository, roleRepository, passwordEncoder);
        }
    }

    @Test
    void saveUser() {

        Mockito.when(userRepository.getByUsername(Mockito.any())).thenReturn(null);
        Mockito.when(passwordEncoder.encode(Mockito.any())).thenReturn("");

        SensorUser sensorUser = userService.saveUser(new SensorUser());

        Mockito.verify(userRepository, Mockito.times(1)).getByUsername(Mockito.any());
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(passwordEncoder, Mockito.times(1)).encode(Mockito.any());
        Assertions.assertNull(sensorUser);
    }

    @Test
    void saveUserExistingUser() {

        Mockito.when(userRepository.getByUsername(Mockito.any())).thenReturn(new SensorUser());

        SensorUser sensorUser = userService.saveUser(new SensorUser());

        Mockito.verify(userRepository, Mockito.times(1)).getByUsername(Mockito.any());
        Mockito.verify(passwordEncoder, Mockito.times(0)).encode(Mockito.any());
        Assertions.assertNotNull(sensorUser);
    }

    @Test
    void save() {

        SensorUser sensorUser = userService.saveUser(new SensorUser());

        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());
        Assertions.assertNull(sensorUser);
    }

    @Test
    void saveRole() {

        Mockito.when(roleRepository.findByName(Mockito.any())).thenReturn(null);

        Role role = userService.saveRole(new Role());

        Mockito.verify(roleRepository, Mockito.times(1)).save(Mockito.any());
        Assertions.assertNull(role);
    }

    @Test
    void saveRoleExistingRole() {

        Mockito.when(roleRepository.findByName(Mockito.any())).thenReturn(new Role());

        Role role = userService.saveRole(new Role());

        Mockito.verify(roleRepository, Mockito.times(0)).save(Mockito.any());
        Assertions.assertNotNull(role);
    }

    @Test
    void addRoleToUser() {

        Mockito.when(userRepository.getByUsername(Mockito.any())).thenReturn(new SensorUser());
        Mockito.when(roleRepository.findByName(Mockito.any())).thenReturn(new Role());

        Role role = userService.addRoleToUser("username", "roleName");

        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());
        Assertions.assertNotNull(role);
    }

    @Test
    void addExistingRoleToUser() {

        SensorUser user = new SensorUser();
        Collection<Role> roles = new ArrayList<>();
        Role role = Role.builder().name("roleName").build();
        roles.add(role);
        user.setRoles(roles);

        Mockito.when(userRepository.getByUsername(Mockito.any())).thenReturn(user);
        Mockito.when(roleRepository.findByName(Mockito.any())).thenReturn(role);

        Role returnedRole = userService.addRoleToUser("username", "roleName");

        Mockito.verify(userRepository, Mockito.times(0)).save(Mockito.any());
        Assertions.assertNotNull(returnedRole);
    }

    @Test
    void getByUsername() {

        Mockito.when(userRepository.getByUsername(Mockito.any())).thenReturn(new SensorUser());

        SensorUser sensorUser = userService.getByUsername("username");

        Mockito.verify(userRepository, Mockito.times(1)).getByUsername(Mockito.any());
        Assertions.assertNotNull(sensorUser);
    }

    @Test
    void getUsers() {

        Mockito.when(userRepository.findAll()).thenReturn(new ArrayList<>());

        List<SensorUser> sensorUsers = userService.getUsers();

        Mockito.verify(userRepository, Mockito.times(1)).findAll();
        Assertions.assertNotNull(sensorUsers);
    }

    @Test
    void loadUserByUsername() {

        SensorUser user = SensorUser.builder().username("username").password("password").build();

        Collection<Role> roles = new ArrayList<>();
        Role role = Role.builder().name("roleName").build();
        roles.add(role);
        user.setRoles(roles);

        Mockito.when(userRepository.getByUsername(Mockito.any())).thenReturn(user);

        UserDetails userDetails = userService.loadUserByUsername("username");

        Mockito.verify(userRepository, Mockito.times(1)).getByUsername(Mockito.any());
        Assertions.assertNotNull(userDetails);

    }

    @Test
    void loadUserByUsernameDoesNotExist() {

        Mockito.when(userRepository.getByUsername(Mockito.any())).thenReturn(null);

        Assertions.assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("username"));

    }
}