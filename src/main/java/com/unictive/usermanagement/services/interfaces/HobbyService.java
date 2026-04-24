package com.unictive.usermanagement.services.interfaces;

import com.unictive.usermanagement.dto.requests.HobbyRequest;
import com.unictive.usermanagement.dto.responses.HobbyResponse;
import com.unictive.usermanagement.dto.responses.base.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface HobbyService {

    HobbyResponse createHobby(HobbyRequest request);

    HobbyResponse getHobbyById(Long id);

    PagedResponse<HobbyResponse> getAllHobbies(Pageable pageable);

    HobbyResponse updateHobby(Long id, HobbyRequest request);

    void deleteHobby(Long id);
}
