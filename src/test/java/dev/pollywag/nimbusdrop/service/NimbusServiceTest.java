package dev.pollywag.nimbusdrop.service;

import dev.pollywag.nimbusdrop.entity.*;
import dev.pollywag.nimbusdrop.exception.NimbusNotFoundException;
import dev.pollywag.nimbusdrop.exception.ResourceOwnershipException;
import dev.pollywag.nimbusdrop.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NimbusServiceTest {

    // --- Mocked Dependencies ---
    @Mock private NimbusRepository nimbusRepository;
    @Mock private UserRepository userRepository;
    @Mock private DropRepository dropRepository;
    @Mock private NimbusSpaceRepository nimbusSpaceRepository;
    @Mock private DropShareLinkRepository dropShareLinkRepository;
    @Mock private FileStorageService fileStorageService;
    @Mock private EntityFetcher entityFetcher;
    @Mock private ModelMapper modelMapper;

    // --- Class Under Test ---
    @InjectMocks
    private NimbusService nimbusService;

    // --- Reusable Test Data ---
    private User mockUser;
    private Nimbus mockNimbus;
    private NimbusSpace mockNimbusSpace;

    @BeforeEach
    void setUp() {
        // Build a reusable mock User
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        mockNimbusSpace = new NimbusSpace();
        mockNimbusSpace.setUsedStorageBytes(500L);
        mockUser.setNimbusSpace(mockNimbusSpace);

        // Build a reusable mock Nimbus owned by mockUser
        mockNimbus = new Nimbus();
        mockNimbus.setId(1L);
        mockNimbus.setNimbusName("My Nimbus");
        mockNimbus.setUser(mockUser);
        mockNimbus.setDrops(new ArrayList<>());
    }

    // =====================================================================
    // createNimbus()
    // =====================================================================

    @Test
    void createNimbus_shouldReturnSavedNimbus_whenUserExists() {
        // Arrange
        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(nimbusRepository.save(any(Nimbus.class))).thenReturn(mockNimbus);

        // Act
        Nimbus result = nimbusService.createNimbus("My Nimbus", "test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("My Nimbus", result.getNimbusName());
        verify(nimbusRepository, times(1)).save(any(Nimbus.class));
    }

    @Test
    void createNimbus_shouldThrowException_whenUserNotFound() {
        // Arrange
        when(entityFetcher.getUserByEmail("unknown@example.com"))
                .thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                nimbusService.createNimbus("My Nimbus", "unknown@example.com"));

        verify(nimbusRepository, never()).save(any());
    }

    // =====================================================================
    // getNimbusById()
    // =====================================================================

    @Test
    void getNimbusById_shouldReturnNimbus_whenOwnerRequests() {
        // Arrange
        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(entityFetcher.getNimbusById(1L)).thenReturn(mockNimbus);

        // Act
        Nimbus result = nimbusService.getNimbusById(1L, "test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getNimbusById_shouldThrowResourceOwnershipException_whenNotOwner() {
        // Arrange — different user trying to access mockUser's nimbus
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("other@example.com");

        when(entityFetcher.getUserByEmail("other@example.com")).thenReturn(otherUser);
        when(entityFetcher.getNimbusById(1L)).thenReturn(mockNimbus);

        // Act & Assert
        assertThrows(ResourceOwnershipException.class, () ->
                nimbusService.getNimbusById(1L, "other@example.com"));
    }

    // =====================================================================
    // deleteNimbus()
    // =====================================================================

    @Test
    void deleteNimbus_shouldDelete_whenOwnerAndNimbusIsEmpty() {
        // Arrange — nimbus has no drops (already empty)
        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(entityFetcher.getNimbusById(1L)).thenReturn(mockNimbus);
        doNothing().when(fileStorageService).deleteNimbusDirectory(any());

        // Act
        nimbusService.deleteNimbus(1L, "test@example.com");

        // Assert
        verify(nimbusRepository, times(1)).delete(mockNimbus);
        verify(fileStorageService, times(1)).deleteNimbusDirectory("user_1/nimbus_1");
    }

    @Test
    void deleteNimbus_shouldThrowException_whenNimbusHasDrops() {
        // Arrange — nimbus has drops inside
        Drop drop = new Drop();
        mockNimbus.getDrops().add(drop);

        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(entityFetcher.getNimbusById(1L)).thenReturn(mockNimbus);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                nimbusService.deleteNimbus(1L, "test@example.com"));

        verify(nimbusRepository, never()).delete(any());
    }

    @Test
    void deleteNimbus_shouldThrowResourceOwnershipException_whenNotOwner() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);

        when(entityFetcher.getUserByEmail("other@example.com")).thenReturn(otherUser);
        when(entityFetcher.getNimbusById(1L)).thenReturn(mockNimbus);

        // Act & Assert
        assertThrows(ResourceOwnershipException.class, () ->
                nimbusService.deleteNimbus(1L, "other@example.com"));

        verify(nimbusRepository, never()).delete(any());
    }

    // =====================================================================
    // updateNimbusName()
    // =====================================================================

    @Test
    void updateNimbusName_shouldUpdateName_whenOwnerRequests() {
        // Arrange
        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(entityFetcher.getNimbusById(1L)).thenReturn(mockNimbus);
        when(nimbusRepository.save(mockNimbus)).thenReturn(mockNimbus);

        // Act
        Nimbus result = nimbusService.updateNimbusName(1L, "New Name", "test@example.com");

        // Assert
        assertEquals("New Name", result.getNimbusName());
        verify(nimbusRepository, times(1)).save(mockNimbus);
    }

    @Test
    void updateNimbusName_shouldThrowResourceOwnershipException_whenNotOwner() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);

        when(entityFetcher.getUserByEmail("other@example.com")).thenReturn(otherUser);
        when(entityFetcher.getNimbusById(1L)).thenReturn(mockNimbus);

        // Act & Assert
        assertThrows(ResourceOwnershipException.class, () ->
                nimbusService.updateNimbusName(1L, "New Name", "other@example.com"));

        verify(nimbusRepository, never()).save(any());
    }

    // =====================================================================
    // getAllDropByNimbusId()
    // =====================================================================

    @Test
    void getAllDropByNimbusId_shouldReturnDrops_whenOwnerRequests() {
        // Arrange
        List<Drop> mockDrops = List.of(new Drop(), new Drop());
        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(entityFetcher.getNimbusById(1L)).thenReturn(mockNimbus);
        when(dropRepository.findByNimbusId(1L)).thenReturn(mockDrops);

        // Act
        List<Drop> result = nimbusService.getAllDropByNimbusId(1L, "test@example.com");

        // Assert
        assertEquals(2, result.size());
        verify(dropRepository, times(1)).findByNimbusId(1L);
    }

    // =====================================================================
    // emptyNimbus()
    // =====================================================================

    @Test
    void emptyNimbus_shouldClearDrops_whenNimbusHasDrops() {
        // Arrange — nimbus has drops
        mockNimbus.getDrops().add(new Drop());

        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(entityFetcher.getNimbusById(1L)).thenReturn(mockNimbus);
        doNothing().when(fileStorageService).emptyNimbusDirectory(any());

        // Act
        nimbusService.emptyNimbus(1L, "test@example.com");

        // Assert
        verify(dropRepository, times(1)).deleteAllByNimbusId(1L);
        verify(nimbusSpaceRepository, times(1)).save(mockNimbusSpace);
        assertEquals(0L, mockNimbusSpace.getUsedStorageBytes());
    }

    @Test
    void emptyNimbus_shouldThrowException_whenNimbusAlreadyEmpty() {
        // Arrange — nimbus has no drops
        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(entityFetcher.getNimbusById(1L)).thenReturn(mockNimbus);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                nimbusService.emptyNimbus(1L, "test@example.com"));

        verify(dropRepository, never()).deleteAllByNimbusId(any());
    }
}