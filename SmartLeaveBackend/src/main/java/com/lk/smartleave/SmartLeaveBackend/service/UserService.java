package com.lk.smartleave.SmartLeaveBackend.service;

import com.lk.smartleave.SmartLeaveBackend.dto.UserDTO;

import java.util.List;

public interface UserService {
    int saveUser(UserDTO userDTO);
    UserDTO searchUser(String username);
    List<UserDTO> getAllUsers();
    UserDTO updateUser(UserDTO userDTO);
    int deleteUser(String email);

}
