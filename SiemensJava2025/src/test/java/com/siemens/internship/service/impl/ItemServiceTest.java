package com.siemens.internship.service.impl;

import com.siemens.internship.controller.dto.ItemDTO;
import com.siemens.internship.exception.EmailAlreadyExistsException;
import com.siemens.internship.exception.ObjectNotFoundException;
import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.utils.TestDataBuilder;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemServiceImpl itemServiceImpl;

    @Captor
    private ArgumentCaptor<Item> itemArgumentCaptor;

    @Test
    public void testSaveItem() {
        Item item = TestDataBuilder.buildItem(1);
        ItemDTO itemDTO = TestDataBuilder.buildItemDTO(1);

        when(modelMapper.map(itemDTO, Item.class)).thenReturn(item);
        when(modelMapper.map(item, ItemDTO.class)).thenReturn(itemDTO);
        when(itemRepository.save(item)).thenReturn(item);

        ItemDTO returnedItemDTO = itemServiceImpl.save(itemDTO);

        assertNotNull(returnedItemDTO);
        verify(itemRepository).save(itemArgumentCaptor.capture());
        Item capturedItem = itemArgumentCaptor.getValue();

        assertThat(capturedItem.getEmail()).isEqualTo(itemDTO.getEmail());
        assertThat(capturedItem.getName()).isEqualTo(itemDTO.getName());
    }

    @Test
    public void testFindAll_shouldReturnAllItems() {
        Item item1 = TestDataBuilder.buildItem(1);
        Item item2 = TestDataBuilder.buildItem(2);
        ItemDTO itemDTO1 = TestDataBuilder.buildItemDTO(1);
        ItemDTO itemDTO2 = TestDataBuilder.buildItemDTO(2);

        when(itemRepository.findAll()).thenReturn(List.of(item1, item2));
        when(modelMapper.map(item1, ItemDTO.class)).thenReturn(itemDTO1);
        when(modelMapper.map(item2, ItemDTO.class)).thenReturn(itemDTO2);

        List<ItemDTO> itemDTOList = itemServiceImpl.findAll();

        assertEquals(2, itemDTOList.size());
        assertTrue(itemDTOList.containsAll(List.of(itemDTO1, itemDTO2)));
    }

    @Test
    public void findById_shouldReturnRequestedItem() {
        Item item = TestDataBuilder.buildItem(1);
        ItemDTO expectedItem = TestDataBuilder.buildItemDTO(1);

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(modelMapper.map(item, ItemDTO.class)).thenReturn(expectedItem);

        ItemDTO returnedItemDTO = itemServiceImpl.findById(item.getId());

        verify(itemRepository).findById(item.getId());
        assertEquals(expectedItem, returnedItemDTO);
    }

    @Test
    public void testFindById_shouldThrowNotFoundException() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> itemServiceImpl.findById(1L));
    }

    @Test
    public void findByEmail_shouldReturnRequestedItem() {
        Item item = TestDataBuilder.buildItem(1);
        ItemDTO expectedItem = TestDataBuilder.buildItemDTO(1);

        when(itemRepository.findByEmail(item.getEmail())).thenReturn(Optional.of(item));
        when(modelMapper.map(item, ItemDTO.class)).thenReturn(expectedItem);

        ItemDTO returnedItemDTO = itemServiceImpl.findByEmail(item.getEmail());

        verify(itemRepository).findByEmail(item.getEmail());
        assertEquals(expectedItem, returnedItemDTO);
    }

    @Test
    public void testFindByEmail_shouldThrowNotFoundException() {
        when(itemRepository.findByEmail("item1@gmal.com")).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> itemServiceImpl.findByEmail("item1@gmal.com"));
    }

    @Test
    public void testDeleteItem() {
        long itemId = 1L;
        Item item = TestDataBuilder.buildItem(1);
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        itemServiceImpl.deleteById(itemId);

        verify(itemRepository).delete(item);
    }

    @Test
    public void testDeleteItem_shouldThrowNotFoundException() {
        Long itemId = 1L;
        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ObjectNotFoundException.class, () -> {
            itemServiceImpl.deleteById(itemId);
        });

        assertEquals("Item with id: " + itemId + " not found!", exception.getMessage());
    }

    @Test
    public void testUpdate_ItemExists_SameEmail() {
        ItemDTO itemDTO = TestDataBuilder.buildItemDTO(1);
        Item savedItem = TestDataBuilder.buildItem(1);

        ItemServiceImpl spyService = Mockito.spy(itemServiceImpl);
        doReturn(itemDTO).when(spyService).findById(itemDTO.getId());

        when(modelMapper.map(itemDTO, Item.class)).thenReturn(savedItem);
        when(modelMapper.map(savedItem, ItemDTO.class)).thenReturn(itemDTO);
        when(itemRepository.save(savedItem)).thenReturn(savedItem);

        ItemDTO updatedItemDTO = spyService.update(itemDTO.getId(), itemDTO);

        assertNotNull(updatedItemDTO);
        assertEquals(itemDTO.getEmail(), updatedItemDTO.getEmail());
        verify(itemRepository).save(savedItem);
    }

    @Test
    public void testUpdate_ItemExists_DifferentEmail_EmailAlreadyExistsException() {
        Item existingItem = TestDataBuilder.buildItem(1);
        ItemDTO incomingDTO = TestDataBuilder.buildItemDTO(1);
        incomingDTO.setEmail("email2@example.com");

        Item conflictingItem = TestDataBuilder.buildItem(2);
        conflictingItem.setEmail("email2@example.com");
        ItemDTO conflictingItemDTO = TestDataBuilder.buildItemDTO(2);
        conflictingItemDTO.setEmail("email2@example.com");

        when(itemRepository.findById(existingItem.getId())).thenReturn(Optional.of(existingItem));
        when(itemRepository.findByEmail("email2@example.com")).thenReturn(Optional.of(conflictingItem));
        when(modelMapper.map(conflictingItem, ItemDTO.class)).thenReturn(conflictingItemDTO);
        when(modelMapper.map(existingItem, ItemDTO.class)).thenReturn(
                TestDataBuilder.buildItemDTO(1)
        );

        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class,
                () -> itemServiceImpl.update(existingItem.getId(), incomingDTO));

        assertEquals("Item with email: email2@example.com already exists!", exception.getMessage());
    }

    @Test
    public void testUpdate_ItemNotFound() {
        ItemDTO itemDTO = TestDataBuilder.buildItemDTO(1);

        when(itemRepository.findById(itemDTO.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ObjectNotFoundException.class, () -> {
            itemServiceImpl.update(itemDTO.getId(), itemDTO);
        });

        assertEquals("Item with id: " + itemDTO.getId() + " not found!", exception.getMessage());
    }

    @Test
    void testProcessItemsAsync() throws Exception {
        Executor executorReal = Executors.newFixedThreadPool(4);
        ReflectionTestUtils.setField(itemServiceImpl, "executor", executorReal);

        List<Long> ids = List.of(1L, 2L, 3L);

        Item item1 = TestDataBuilder.buildItem(1);
        Item item2 = TestDataBuilder.buildItem(2);
        Item item3 = TestDataBuilder.buildItem(3);
        item3.setStatus("PROCESSED");

        ItemDTO itemDTO1 = TestDataBuilder.buildItemDTO(1);
        ItemDTO itemDTO2 = TestDataBuilder.buildItemDTO(2);
        ItemDTO itemDTO3 = TestDataBuilder.buildItemDTO(3);
        itemDTO3.setStatus("PROCESSED");

        when(itemRepository.findAllIds()).thenReturn(ids);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));
        when(itemRepository.findById(3L)).thenReturn(Optional.of(item3));

        when(modelMapper.map(item1, ItemDTO.class)).thenReturn(itemDTO1);
        when(modelMapper.map(item2, ItemDTO.class)).thenReturn(itemDTO2);
        when(modelMapper.map(item3, ItemDTO.class)).thenReturn(itemDTO3);

        CompletableFuture<List<ItemDTO>> future = itemServiceImpl.processItemsAsync();
        List<ItemDTO> processedItems = future.get();

        assertEquals(2, processedItems.size());
        assertTrue(processedItems.stream().allMatch(item -> "PROCESSED".equals(item.getStatus())));

        Mockito.verify(itemRepository, times(2)).save(itemArgumentCaptor.capture());
        List<Item> savedItems = itemArgumentCaptor.getAllValues();

        assertEquals(2, savedItems.size());
    }
}
