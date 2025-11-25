package com.lk.smartleave.SmartLeaveBackend.service.impl;

import com.lk.smartleave.SmartLeaveBackend.dto.UserDTO;
import com.lk.smartleave.SmartLeaveBackend.entity.User;
import com.lk.smartleave.SmartLeaveBackend.repo.UserRepository;
import com.lk.smartleave.SmartLeaveBackend.service.UserService;
import com.lk.smartleave.SmartLeaveBackend.util.VarList;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
@Transactional
public class UserServiceImpl implements UserDetailsService, UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                getAuthority(user));
    }

    public UserDTO loadUserDetailsByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);
        return modelMapper.map(user, UserDTO.class);
    }

    private Set<SimpleGrantedAuthority> getAuthority(User user) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole()));
        return authorities;
    }

    @Override
    public UserDTO searchUser(String username) {
        if (userRepository.existsByEmail(username)) {
            User user = userRepository.findByEmail(username);
            return modelMapper.map(user, UserDTO.class);
        } else {
            return null;
        }
    }

    @Override
    public int saveUser(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            return VarList.Not_Acceptable;
        } else {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            userRepository.save(modelMapper.map(userDTO, User.class));
            return VarList.Created;
        }
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return modelMapper.map(users, new TypeToken<List<UserDTO>>(){}.getType());
    }

    @Override
    public UserDTO updateUser(UserDTO userDTO) {

        // check user exists by email
        if (userRepository.existsByEmail(userDTO.getEmail())) {

            User existingUser = userRepository.findByEmail(userDTO.getEmail());

            // update only allowed fields
            existingUser.setName(userDTO.getName());
            existingUser.setRole(userDTO.getRole());

            // update password only if user sends new one
            if (userDTO.getPassword() != null && !userDTO.getPassword().isBlank()) {
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                existingUser.setPassword(encoder.encode(userDTO.getPassword()));
            }

            userRepository.save(existingUser);

            return modelMapper.map(existingUser, UserDTO.class);
        }

        return null;
    }


    @Override
    public int deleteUser(String email) {
        if (userRepository.existsByEmail(email)) {
            userRepository.deleteByEmail(email);
            return VarList.OK;
        } else {
            return VarList.Not_Found;
        }
    }

}
