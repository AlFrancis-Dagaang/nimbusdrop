package dev.pollywag.nimbusdrop.service;

import dev.pollywag.nimbusdrop.entity.*;
import dev.pollywag.nimbusdrop.exception.DropNotFoundException;
import dev.pollywag.nimbusdrop.exception.ExceededQuotaException;
import dev.pollywag.nimbusdrop.exception.ResourceOwnershipException;
import dev.pollywag.nimbusdrop.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DropServiceTest {

    // --- Mocked Dependencies ---
    @Mock private DropRepository dropRepository;
    @Mock private QuotaService quotaService;
    @Mock private NimbusRepository nimbusRepository;
    @Mock private FileStorageService fileStorageService;
    @Mock private UserRepository userRepository;
    @Mock private NimbusSpaceRepository nimbusSpaceRepository;
    @Mock private EntityFetcher entityFetcher;
    @Mock private ModelMapper modelMapper;

    // --- Class Under Test ---
    @InjectMocks
    private DropService dropService;

    // --- Reusable Test Data ---
    private User mockUser;
    private Nimbus mockNimbus;
    private Drop mockDrop;
    private NimbusSpace mockNimbusSpace;
    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
        mockNimbusSpace = new NimbusSpace();
        mockNimbusSpace.setId(1L);
        mockNimbusSpace.setUsedStorageBytes(1000L);

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setNimbusSpace(mockNimbusSpace);

        mockNimbus = new Nimbus();
        mockNimbus.setId(1L);
        mockNimbus.setNimbusName("My Nimbus");
        mockNimbus.setUser(mockUser);
        mockNimbus.setDrops(new ArrayList<>());

        mockDrop = new Drop();
        mockDrop.setId(1L);
        mockDrop.setDropName("test-file.pdf");
        mockDrop.setDropKey("user_1/nimbus_1/uuid-test-file.pdf");
        mockDrop.setSize(500L);
        mockDrop.setContentType("application/pdf");
        mockDrop.setNimbus(mockNimbus);

        mockFile = mock(MultipartFile.class);
    }

    // =====================================================================
    // getDropById()
    // =====================================================================

    @Test
    void getDropById_shouldReturnDrop_whenOwnerRequests() {
        // Arrange
        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(entityFetcher.getDropById(1L)).thenReturn(mockDrop);

        // Act
        Drop result = dropService.getDropById(1L, "test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test-file.pdf", result.getDropName());
    }

    @Test
    void getDropById_shouldThrowResourceOwnershipException_whenNotOwner() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);

        when(entityFetcher.getUserByEmail("other@example.com")).thenReturn(otherUser);
        when(entityFetcher.getDropById(1L)).thenReturn(mockDrop);

        // Act & Assert
        assertThrows(ResourceOwnershipException.class, () ->
                dropService.getDropById(1L, "other@example.com"));
    }

    // =====================================================================
    // uploadDrop()
    // =====================================================================

    @Test
    void uploadDrop_shouldSaveDrop_whenAllConditionsMet() throws IOException {
        // Arrange
        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(entityFetcher.getNimbusById(1L)).thenReturn(mockNimbus);
        when(quotaService.canUpload(1L)).thenReturn(true);
        when(quotaService.canUploadByStorage(anyLong(), anyLong())).thenReturn(true);
        when(mockFile.getOriginalFilename()).thenReturn("test-file.pdf");
        when(mockFile.getContentType()).thenReturn("application/pdf");
        when(mockFile.getSize()).thenReturn(500L);
        doNothing().when(fileStorageService).saveDropFile(anyString(), any(MultipartFile.class));
        when(userRepository.save(mockUser)).thenReturn(mockUser);

        // Act
        Drop result = dropService.uploadDrop(1L, mockFile, "test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("test-file.pdf", result.getDropName());
        assertEquals("application/pdf", result.getContentType());
        assertEquals(500L, result.getSize());
        verify(fileStorageService, times(1)).saveDropFile(anyString(), any(MultipartFile.class));
        verify(quotaService, times(1)).registerUpload(1L, 500L);
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void uploadDrop_shouldThrowResourceOwnershipException_whenNotNimbusOwner() throws IOException {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setNimbusSpace(mockNimbusSpace);

        when(entityFetcher.getUserByEmail("other@example.com")).thenReturn(otherUser);
        when(entityFetcher.getNimbusById(1L)).thenReturn(mockNimbus);

        // Act & Assert
        assertThrows(ResourceOwnershipException.class, () ->
                dropService.uploadDrop(1L, mockFile, "other@example.com"));

        verify(fileStorageService, never()).saveDropFile(anyString(), any());
    }

    @Test
    void uploadDrop_shouldThrowExceededQuotaException_whenUploadQuotaExceeded() throws IOException {
        // Arrange
        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(entityFetcher.getNimbusById(1L)).thenReturn(mockNimbus);
        when(quotaService.canUpload(1L)).thenReturn(false); // quota exceeded

        // Act & Assert
        assertThrows(ExceededQuotaException.class, () ->
                dropService.uploadDrop(1L, mockFile, "test@example.com"));

        verify(fileStorageService, never()).saveDropFile(anyString(), any());
    }

    @Test
    void uploadDrop_shouldThrowExceededQuotaException_whenStorageQuotaExceeded() throws IOException {
        // Arrange
        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(entityFetcher.getNimbusById(1L)).thenReturn(mockNimbus);
        when(quotaService.canUpload(1L)).thenReturn(true);
        when(mockFile.getSize()).thenReturn(999999999L); // huge file
        when(quotaService.canUploadByStorage(anyLong(), anyLong())).thenReturn(false); // storage exceeded

        // Act & Assert
        assertThrows(ExceededQuotaException.class, () ->
                dropService.uploadDrop(1L, mockFile, "test@example.com"));

        verify(fileStorageService, never()).saveDropFile(anyString(), any());
    }

    // =====================================================================
    // deleteDrop()
    // =====================================================================

    @Test
    void deleteDrop_shouldDeleteDrop_whenOwnerRequests() throws IOException {
        // Arrange
        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(entityFetcher.getDropById(1L)).thenReturn(mockDrop);
        doNothing().when(fileStorageService).deleteDrop(anyString());

        // Act
        dropService.deleteDrop(1L, "test@example.com");

        // Assert
        verify(fileStorageService, times(1)).deleteDrop("user_1/nimbus_1/uuid-test-file.pdf");
        verify(dropRepository, times(1)).delete(mockDrop);
        verify(nimbusSpaceRepository, times(1)).save(mockNimbusSpace);
        assertEquals(500L, mockNimbusSpace.getUsedStorageBytes()); // 1000 - 500
    }

    @Test
    void deleteDrop_shouldThrowResourceOwnershipException_whenNotOwner() throws IOException {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setNimbusSpace(mockNimbusSpace);

        when(entityFetcher.getUserByEmail("other@example.com")).thenReturn(otherUser);
        when(entityFetcher.getDropById(1L)).thenReturn(mockDrop);

        // Act & Assert
        assertThrows(ResourceOwnershipException.class, () ->
                dropService.deleteDrop(1L, "other@example.com"));

        verify(dropRepository, never()).delete(any());
        verify(fileStorageService, never()).deleteDrop(anyString());
    }

    // =====================================================================
    // openDrop()
    // =====================================================================

    @Test
    void openDrop_shouldReturnResource_whenOwnerAndQuotaAvailable() throws MalformedURLException {
        // Arrange
        Resource mockResource = mock(Resource.class);

        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(entityFetcher.getDropById(1L)).thenReturn(mockDrop);
        when(quotaService.canView(1L)).thenReturn(true);
        when(fileStorageService.openDropFile(anyString())).thenReturn(mockResource);

        // Act
        Resource result = dropService.openDrop(1L, "test@example.com");

        // Assert
        assertNotNull(result);
        verify(quotaService, times(1)).registerView(1L);
        verify(fileStorageService, times(1)).openDropFile(mockDrop.getDropKey());
    }

    @Test
    void openDrop_shouldThrowExceededQuotaException_whenViewQuotaExceeded() throws MalformedURLException {
        // Arrange
        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(entityFetcher.getDropById(1L)).thenReturn(mockDrop);
        when(quotaService.canView(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(ExceededQuotaException.class, () ->
                dropService.openDrop(1L, "test@example.com"));

        verify(fileStorageService, never()).openDropFile(anyString());
    }

    @Test
    void openDrop_shouldThrowResourceOwnershipException_whenNotOwner() throws MalformedURLException {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setNimbusSpace(mockNimbusSpace);

        when(entityFetcher.getUserByEmail("other@example.com")).thenReturn(otherUser);
        when(entityFetcher.getDropById(1L)).thenReturn(mockDrop);

        // Act & Assert
        assertThrows(ResourceOwnershipException.class, () ->
                dropService.openDrop(1L, "other@example.com"));

        verify(fileStorageService, never()).openDropFile(anyString());
    }

    // =====================================================================
    // downloadDropFile()
    // =====================================================================

    @Test
    void downloadDropFile_shouldReturnResource_whenOwnerAndQuotaAvailable() throws IOException {
        // Arrange
        Resource mockResource = mock(Resource.class);

        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(entityFetcher.getNimbusById(1L)).thenReturn(mockNimbus);
        when(quotaService.canDownload(1L)).thenReturn(true);
        when(fileStorageService.downloadDropFile(anyString())).thenReturn(mockResource);

        // Act
        Resource result = dropService.downloadDropFile("user_1/nimbus_1/file.pdf", "test@example.com", 1L);

        // Assert
        assertNotNull(result);
        verify(quotaService, times(1)).registerDownload(1L);
        verify(fileStorageService, times(1)).downloadDropFile("user_1/nimbus_1/file.pdf");
    }

    @Test
    void downloadDropFile_shouldThrowExceededQuotaException_whenDownloadQuotaExceeded() throws IOException {
        // Arrange
        when(entityFetcher.getUserByEmail("test@example.com")).thenReturn(mockUser);
        when(entityFetcher.getNimbusById(1L)).thenReturn(mockNimbus);
        when(quotaService.canDownload(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(ExceededQuotaException.class, () ->
                dropService.downloadDropFile("user_1/nimbus_1/file.pdf", "test@example.com", 1L));

        verify(fileStorageService, never()).downloadDropFile(anyString());
    }

    @Test
    void downloadDropFile_shouldThrowResourceOwnershipException_whenNotNimbusOwner() throws IOException {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setNimbusSpace(mockNimbusSpace);

        when(entityFetcher.getUserByEmail("other@example.com")).thenReturn(otherUser);
        when(entityFetcher.getNimbusById(1L)).thenReturn(mockNimbus);

        // Act & Assert
        assertThrows(ResourceOwnershipException.class, () ->
                dropService.downloadDropFile("user_1/nimbus_1/file.pdf", "other@example.com", 1L));

        verify(fileStorageService, never()).downloadDropFile(anyString());
    }
}