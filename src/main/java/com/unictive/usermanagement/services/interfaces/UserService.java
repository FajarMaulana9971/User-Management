package com.unictive.usermanagement.services.interfaces;

import com.unictive.usermanagement.dto.requests.RegisterRequest;
import com.unictive.usermanagement.dto.requests.UpdateUserRequest;
import com.unictive.usermanagement.dto.responses.AuditHistoryResponse;
import com.unictive.usermanagement.dto.responses.UserResponse;
import com.unictive.usermanagement.dto.responses.base.PagedResponse;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    UserResponse createUser(RegisterRequest request, MultipartFile profilePicture);

    UserResponse getUserById(Long id);

    PagedResponse<UserResponse> getAllUsers(Pageable pageable);

    UserResponse updateUser(Long id, UpdateUserRequest request);

    UserResponse updateProfilePicture(Long id, MultipartFile file);

    void deleteUser(Long id);

    List<AuditHistoryResponse> getUserHistory(Long id);
}
