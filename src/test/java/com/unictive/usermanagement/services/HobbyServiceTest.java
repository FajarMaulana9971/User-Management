package com.unictive.usermanagement.services;

import com.unictive.usermanagement.dto.requests.HobbyRequest;
import com.unictive.usermanagement.dto.responses.HobbyResponse;
import com.unictive.usermanagement.dto.responses.base.PagedResponse;
import com.unictive.usermanagement.entities.Hobby;
import com.unictive.usermanagement.exceptions.types.ConflictException;
import com.unictive.usermanagement.exceptions.types.ResourceNotFoundException;
import com.unictive.usermanagement.mappers.HobbyMapper;
import com.unictive.usermanagement.repositories.HobbyRepository;
import com.unictive.usermanagement.services.implementations.HobbyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HobbyService Tests")
class HobbyServiceTest {

    @Mock private HobbyRepository hobbyRepository;
    @Mock private HobbyMapper hobbyMapper;

    @InjectMocks
    private HobbyServiceImpl hobbyService;

    private Hobby sampleHobby;
    private HobbyResponse sampleHobbyResponse;

    @BeforeEach
    void setUp() {
        sampleHobby = Hobby.builder()
                .id(1L)
                .name("Photography")
                .description("Capturing moments")
                .build();

        sampleHobbyResponse = HobbyResponse.builder()
                .id(1L)
                .name("Photography")
                .description("Capturing moments")
                .build();
    }

    @Nested
    @DisplayName("createHobby()")
    class CreateHobby {

        @Test
        @DisplayName("should create hobby successfully")
        void shouldCreateHobby() {
            HobbyRequest request = new HobbyRequest();
            request.setName("Photography");
            request.setDescription("Capturing moments");

            when(hobbyRepository.existsByNameIgnoreCase("Photography")).thenReturn(false);
            when(hobbyMapper.toEntity(request)).thenReturn(sampleHobby);
            when(hobbyRepository.save(sampleHobby)).thenReturn(sampleHobby);
            when(hobbyMapper.toResponse(sampleHobby)).thenReturn(sampleHobbyResponse);

            HobbyResponse result = hobbyService.createHobby(request);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Photography");
            verify(hobbyRepository).save(sampleHobby);
        }

        @Test
        @DisplayName("should throw ConflictException when hobby name exists")
        void shouldThrowWhenNameExists() {
            HobbyRequest request = new HobbyRequest();
            request.setName("Photography");

            when(hobbyRepository.existsByNameIgnoreCase("Photography")).thenReturn(true);

            assertThatThrownBy(() -> hobbyService.createHobby(request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("Photography");

            verify(hobbyRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getHobbyById()")
    class GetHobbyById {

        @Test
        @DisplayName("should return hobby when found")
        void shouldReturnHobby() {
            when(hobbyRepository.findById(1L)).thenReturn(Optional.of(sampleHobby));
            when(hobbyMapper.toResponse(sampleHobby)).thenReturn(sampleHobbyResponse);

            HobbyResponse result = hobbyService.getHobbyById(1L);

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(hobbyRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> hobbyService.getHobbyById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAllHobbies()")
    class GetAllHobbies {

        @Test
        @DisplayName("should return paged hobbies")
        void shouldReturnPagedHobbies() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Hobby> page = new PageImpl<>(List.of(sampleHobby), pageable, 1);

            when(hobbyRepository.findAll(pageable)).thenReturn(page);
            when(hobbyMapper.toResponse(sampleHobby)).thenReturn(sampleHobbyResponse);

            PagedResponse<HobbyResponse> result = hobbyService.getAllHobbies(pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("updateHobby()")
    class UpdateHobby {

        @Test
        @DisplayName("should update hobby successfully")
        void shouldUpdateHobby() {
            HobbyRequest request = new HobbyRequest();
            request.setName("Travel");
            request.setDescription("Exploring the world");

            when(hobbyRepository.findById(1L)).thenReturn(Optional.of(sampleHobby));
            when(hobbyRepository.existsByNameIgnoreCaseAndIdNot("Travel", 1L)).thenReturn(false);
            when(hobbyRepository.save(sampleHobby)).thenReturn(sampleHobby);
            when(hobbyMapper.toResponse(sampleHobby)).thenReturn(sampleHobbyResponse);

            HobbyResponse result = hobbyService.updateHobby(1L, request);

            assertThat(result).isNotNull();
            verify(hobbyRepository).save(sampleHobby);
        }

        @Test
        @DisplayName("should throw ConflictException when name already taken by another hobby")
        void shouldThrowWhenNameTaken() {
            HobbyRequest request = new HobbyRequest();
            request.setName("Cooking");

            when(hobbyRepository.findById(1L)).thenReturn(Optional.of(sampleHobby));
            when(hobbyRepository.existsByNameIgnoreCaseAndIdNot("Cooking", 1L)).thenReturn(true);

            assertThatThrownBy(() -> hobbyService.updateHobby(1L, request))
                    .isInstanceOf(ConflictException.class);
        }
    }

    @Nested
    @DisplayName("deleteHobby()")
    class DeleteHobby {

        @Test
        @DisplayName("should delete hobby successfully")
        void shouldDeleteHobby() {
            when(hobbyRepository.findById(1L)).thenReturn(Optional.of(sampleHobby));

            hobbyService.deleteHobby(1L);

            verify(hobbyRepository).delete(sampleHobby);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(hobbyRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> hobbyService.deleteHobby(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(hobbyRepository, never()).delete(any());
        }
    }
}
