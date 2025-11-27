package com.lk.smartleave.SmartLeaveBackend.controller;

import com.lk.smartleave.SmartLeaveBackend.dto.AuthDTO;
import com.lk.smartleave.SmartLeaveBackend.dto.ResponseDTO;
import com.lk.smartleave.SmartLeaveBackend.dto.UserDTO;
import com.lk.smartleave.SmartLeaveBackend.service.UserService;
import com.lk.smartleave.SmartLeaveBackend.util.JwtUtil;
import com.lk.smartleave.SmartLeaveBackend.util.VarList;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping("api/v1/user")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    // REGISTER (public)
    @PostMapping("/register")
    public ResponseEntity<ResponseDTO> registerUser(@Valid @RequestBody UserDTO userDTO) {

        logger.trace("This is TRACE level - very detailed");
        logger.debug("Looking for task with ID: {}", userDTO.getEmail());

        try {
            int res = userService.saveUser(userDTO);


            if (res == VarList.Created) {
                String token = jwtUtil.generateToken(userDTO);

                AuthDTO authDTO = new AuthDTO();
                authDTO.setEmail(userDTO.getEmail());
                authDTO.setToken(token);

                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new ResponseDTO(VarList.Created, "Success", authDTO));
            }

            if (res == VarList.Not_Acceptable) {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                        .body(new ResponseDTO(VarList.Not_Acceptable, "Email Already Used", null));
            }

            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ResponseDTO(VarList.Bad_Gateway, "Error", null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, e.getMessage(), null));
        }
    }

    // GET ALL USERS (Admin only - requires JWT and admin role)
    @GetMapping("/all")
    // pre-authorized can apply
    public ResponseEntity<ResponseDTO> getAllUsers(HttpServletRequest request) {
        try {
            String role = (String) request.getAttribute("role");

            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseDTO(VarList.Forbidden, "Admin access required", null));
            }

            return ResponseEntity.ok(
                    new ResponseDTO(VarList.OK, "Success", userService.getAllUsers())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, e.getMessage(), null));
        }
    }

    // SEARCH (Admin only - requires JWT and admin role)
    @GetMapping("/search/{email}")
    public ResponseEntity<ResponseDTO> searchUser(@PathVariable String email, HttpServletRequest request) {
        try {
            String role = (String) request.getAttribute("role");

            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseDTO(VarList.Forbidden, "Admin access required", null));
            }

            UserDTO user = userService.searchUser(email);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO(VarList.Not_Found, "User Not Found", null));
            }

            return ResponseEntity.ok(
                    new ResponseDTO(VarList.OK, "Success", user)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, e.getMessage(), null));
        }
    }

    // GET MY PROFILE (requires JWT and user-specific validation)
    @GetMapping("/my-profile")
    public ResponseEntity<ResponseDTO> getMyProfile(HttpServletRequest request) {
        try {
            String tokenEmail = (String) request.getAttribute("email");

            if (tokenEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ResponseDTO(VarList.Unauthorized, "Token required", null));
            }

            UserDTO user = userService.searchUser(tokenEmail);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO(VarList.Not_Found, "User Not Found", null));
            }

            return ResponseEntity.ok(
                    new ResponseDTO(VarList.OK, "Success", user)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, e.getMessage(), null));
        }
    }

    // UPDATE (requires JWT and user can only update their own data)
    @PutMapping("/update")
    public ResponseEntity<ResponseDTO> updateUser(@Valid @RequestBody UserDTO userDTO, HttpServletRequest request) {
        try {
            String tokenEmail = (String) request.getAttribute("email");

            if (tokenEmail == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ResponseDTO(VarList.Unauthorized, "Token required", null));
            }

            // Check if user is trying to update their own data
            if (!tokenEmail.equals(userDTO.getEmail())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseDTO(VarList.Forbidden, "You can only update your own profile", null));
            }

            UserDTO updated = userService.updateUser(userDTO);

            if (updated == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ResponseDTO(VarList.Not_Found, "User Not Found", null));
            }

            return ResponseEntity.ok(
                    new ResponseDTO(VarList.OK, "Updated Successfully", updated)
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, e.getMessage(), null));
        }
    }

    // DELETE (Admin only - requires JWT and admin role)
    @DeleteMapping("/delete/{email}")
    public ResponseEntity<ResponseDTO> deleteUser(@PathVariable String email, HttpServletRequest request) {
        try {
            String role = (String) request.getAttribute("role");

            if (!"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ResponseDTO(VarList.Forbidden, "Admin access required", null));
            }

            int res = userService.deleteUser(email);

            if (res == VarList.OK) {
                return ResponseEntity.ok(
                        new ResponseDTO(VarList.OK, "Deleted Successfully", null)
                );
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO(VarList.Not_Found, "User Not Found", null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(VarList.Internal_Server_Error, e.getMessage(), null));
        }
    }
}
