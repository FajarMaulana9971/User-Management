package com.unictive.usermanagement.services.implementations;

import com.unictive.usermanagement.dto.requests.HobbyRequest;
import com.unictive.usermanagement.dto.responses.HobbyResponse;
import com.unictive.usermanagement.dto.responses.base.PagedResponse;
import com.unictive.usermanagement.entities.Hobby;
import com.unictive.usermanagement.exceptions.types.ConflictException;
import com.unictive.usermanagement.exceptions.types.ResourceNotFoundException;
import com.unictive.usermanagement.mappers.HobbyMapper;
import com.unictive.usermanagement.repositories.HobbyRepository;
import com.unictive.usermanagement.services.interfaces.HobbyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HobbyServiceImpl implements HobbyService {

    private static final String CACHE_HOBBIES = "hobbies";
    private static final String CACHE_HOBBY   = "hobby";

    private final HobbyRepository hobbyRepository;
    private final HobbyMapper hobbyMapper;

    @Override
    @Transactional
    @CacheEvict(value = CACHE_HOBBIES, allEntries = true)
    public HobbyResponse createHobby(HobbyRequest request) {
        if (hobbyRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ConflictException("Hobby already exists: " + request.getName());
        }

        Hobby hobby = hobbyMapper.toEntity(request);
        Hobby saved = hobbyRepository.save(hobby);
        log.info("Hobby created: id={}, name={}", saved.getId(), saved.getName());
        return hobbyMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_HOBBY, key = "#id")
    public HobbyResponse getHobbyById(Long id) {
        return hobbyMapper.toResponse(findByIdOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_HOBBIES, key = "'page_' + #pageable.pageNumber + '_size_' + #pageable.pageSize")
    public PagedResponse<HobbyResponse> getAllHobbies(Pageable pageable) {
        Page<Hobby> page = hobbyRepository.findAll(pageable);
        return PagedResponse.from(page.map(hobbyMapper::toResponse));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CACHE_HOBBY,   key = "#id"),
            @CacheEvict(value = CACHE_HOBBIES, allEntries = true)
    })
    public HobbyResponse updateHobby(Long id, HobbyRequest request) {
        Hobby hobby = findByIdOrThrow(id);

        if (hobbyRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new ConflictException("Hobby name already taken: " + request.getName());
        }

        hobby.setName(request.getName());
        hobby.setDescription(request.getDescription());

        Hobby updated = hobbyRepository.save(hobby);
        log.info("Hobby updated: id={}", updated.getId());
        return hobbyMapper.toResponse(updated);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CACHE_HOBBY,   key = "#id"),
            @CacheEvict(value = CACHE_HOBBIES, allEntries = true)
    })
    public void deleteHobby(Long id) {
        Hobby hobby = findByIdOrThrow(id);
        hobbyRepository.delete(hobby);
        log.info("Hobby deleted: id={}", id);
    }

    private Hobby findByIdOrThrow(Long id) {
        return hobbyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hobby not found with id: " + id));
    }
}