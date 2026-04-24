package com.unictive.usermanagement.services.implementations;

import com.unictive.usermanagement.dto.requests.RegisterRequest;
import com.unictive.usermanagement.dto.requests.UpdateUserRequest;
import com.unictive.usermanagement.dto.responses.AuditHistoryResponse;
import com.unictive.usermanagement.dto.responses.UserResponse;
import com.unictive.usermanagement.dto.responses.base.PagedResponse;
import com.unictive.usermanagement.entities.CustomRevisionEntity;
import com.unictive.usermanagement.entities.Hobby;
import com.unictive.usermanagement.entities.Role;
import com.unictive.usermanagement.entities.User;
import com.unictive.usermanagement.enums.RoleName;
import com.unictive.usermanagement.exceptions.types.ConflictException;
import com.unictive.usermanagement.exceptions.types.ResourceNotFoundException;
import com.unictive.usermanagement.mappers.UserMapper;
import com.unictive.usermanagement.repositories.HobbyRepository;
import com.unictive.usermanagement.repositories.RoleRepository;
import com.unictive.usermanagement.repositories.UserRepository;
import com.unictive.usermanagement.services.interfaces.ImageProcessingService;
import com.unictive.usermanagement.services.interfaces.StorageService;
import com.unictive.usermanagement.services.interfaces.UserService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private static final String CACHE_USERS = "users";
    private static final String CACHE_USER  = "user";

    private final UserRepository userRepository;
    private final HobbyRepository hobbyRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;
    private final ImageProcessingService imageProcessingService;
    private final UserMapper userMapper;
    private final EntityManager entityManager;

    @Override
    @Transactional
    @CacheEvict(value = CACHE_USERS, allEntries = true)
    public UserResponse createUser(RegisterRequest request, MultipartFile profilePicture) {
        validateUniqueUsername(request.getUsername(), null);
        validateUniqueEmail(request.getEmail(), null);

        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new ResourceNotFoundException("Role USER not found"));

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(userRole);

        if (request.getHobbyIds() != null && !request.getHobbyIds().isEmpty()) {
            List<Hobby> hobbies = hobbyRepository.findByIdIn(request.getHobbyIds());
            user.setHobbies(new HashSet<>(hobbies));
        }

        if (profilePicture != null && !profilePicture.isEmpty()) {
            MultipartFile compressed = imageProcessingService.validateAndCompress(profilePicture);
            String pictureUrl = storageService.store(compressed, imageProcessingService.getProfilePicturesDir());
            user.setProfilePicture(pictureUrl);
        }

        User saved = userRepository.save(user);
        log.info("User created: id={}, username={}", saved.getId(), saved.getUsername());
        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_USER, key = "#id")
    public UserResponse getUserById(Long id) {
        User user = findUserByIdOrThrow(id);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_USERS, key = "'page_' + #pageable.pageNumber + '_size_' + #pageable.pageSize")
    public PagedResponse<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> page = userRepository.findAllWithRole(pageable);
        Page<UserResponse> mapped = page.map(userMapper::toResponse);
        return PagedResponse.from(mapped);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CACHE_USER,  key = "#id"),
            @CacheEvict(value = CACHE_USERS, allEntries = true)
    })
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = findUserByIdOrThrow(id);

        if (StringUtils.hasText(request.getUsername())) {
            validateUniqueUsername(request.getUsername(), id);
            user.setUsername(request.getUsername());
        }
        if (StringUtils.hasText(request.getEmail())) {
            validateUniqueEmail(request.getEmail(), id);
            user.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (StringUtils.hasText(request.getFullName())) {
            user.setFullName(request.getFullName());
        }
        if (request.getHobbyIds() != null) {
            List<Hobby> hobbies = hobbyRepository.findByIdIn(request.getHobbyIds());
            user.setHobbies(new HashSet<>(hobbies));
        }

        User updated = userRepository.save(user);
        log.info("User updated: id={}", updated.getId());
        return userMapper.toResponse(updated);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CACHE_USER,  key = "#id"),
            @CacheEvict(value = CACHE_USERS, allEntries = true)
    })
    public UserResponse updateProfilePicture(Long id, MultipartFile file) {
        User user = findUserByIdOrThrow(id);

        if (StringUtils.hasText(user.getProfilePicture())) {
            storageService.delete(user.getProfilePicture());
        }

        MultipartFile compressed = imageProcessingService.validateAndCompress(file);
        String pictureUrl = storageService.store(compressed, imageProcessingService.getProfilePicturesDir());
        user.setProfilePicture(pictureUrl);

        User updated = userRepository.save(user);
        log.info("Profile picture updated for user id={}", id);
        return userMapper.toResponse(updated);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CACHE_USER,  key = "#id"),
            @CacheEvict(value = CACHE_USERS, allEntries = true)
    })
    public void deleteUser(Long id) {
        User user = findUserByIdOrThrow(id);

        if (StringUtils.hasText(user.getProfilePicture())) {
            storageService.delete(user.getProfilePicture());
        }

        userRepository.delete(user);
        log.info("User deleted: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<AuditHistoryResponse> getUserHistory(Long id) {
        findUserByIdOrThrow(id);

        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        List<Object[]> revisions = auditReader.createQuery()
                .forRevisionsOfEntity(User.class, false, true)
                .add(AuditEntity.id().eq(id))
                .addOrder(AuditEntity.revisionNumber().asc())
                .getResultList();

        return revisions.stream()
                .map(this::buildAuditResponse)
                .toList();
    }

    private User findUserByIdOrThrow(Long id) {
        return userRepository.findByIdWithRole(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private void validateUniqueUsername(String username, Long excludeId) {
        boolean exists = excludeId == null
                ? userRepository.existsByUsername(username)
                : userRepository.existsByUsernameAndIdNot(username, excludeId);
        if (exists) {
            throw new ConflictException("Username already taken: " + username);
        }
    }

    private void validateUniqueEmail(String email, Long excludeId) {
        boolean exists = excludeId == null
                ? userRepository.existsByEmail(email)
                : userRepository.existsByEmailAndIdNot(email, excludeId);
        if (exists) {
            throw new ConflictException("Email already registered: " + email);
        }
    }

    private AuditHistoryResponse buildAuditResponse(Object[] rev) {
        User userSnapshot = (User) rev[0];
        CustomRevisionEntity revEntity = (CustomRevisionEntity) rev[1];
        RevisionType revType = (RevisionType) rev[2];

        String revTypeName = switch (revType) {
            case ADD -> "ADD";
            case MOD -> "MOD";
            case DEL -> "DEL";
        };

        return AuditHistoryResponse.builder()
                .revision(revEntity.getId())
                .revisionType(revTypeName)
                .changedBy(revEntity.getChangedBy())
                .changedAt(Instant.ofEpochMilli(revEntity.getTimestamp()))
                .data(userMapper.toResponse(userSnapshot))
                .build();
    }
}
