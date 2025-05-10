package com.siemens.internship.service.impl;

import com.siemens.internship.controller.dto.ItemDTO;
import com.siemens.internship.exception.ObjectNotFoundException;
import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.service.ItemService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ModelMapper modelMapper;

    private static ExecutorService executor = Executors.newFixedThreadPool(10);
    private List<Item> processedItems = new ArrayList<>();
    private int processedCount = 0;


    public List<ItemDTO> findAll() {
        List<Item> items = itemRepository.findAll();

        return items.stream()
                .map(this::mapToItemDTO)
                .sorted()
                .toList();
    }

    public ItemDTO findById(Long id) {
        return itemRepository.findById(id)
                .map(this::mapToItemDTO)
                .orElseThrow(() -> new ObjectNotFoundException("Item with id: " + id + " not found!"));
    }

    @Override
    public ItemDTO findByEmail(String email) {
        return itemRepository.findByEmail(email)
                .map(this::mapToItemDTO)
                .orElseThrow(() -> new ObjectNotFoundException("Item with email: " + email + " not found!"));
    }

    public ItemDTO save(ItemDTO itemDTO) {
        var itemToAdd = ItemDTO.builder()
                .name(itemDTO.getName())
                .description(itemDTO.getDescription())
                .status(itemDTO.getStatus())
                .email(itemDTO.getEmail())
                .build();

        var item = mapToItem(itemToAdd);

        return mapToItemDTO(itemRepository.save(item));
    }

    public void deleteById(Long id) {
        Optional<Item> itemToDelete = itemRepository.findById(id);
        if (itemToDelete.isPresent()) {
            itemRepository.delete(itemToDelete.get());
        } else {
            throw new ObjectNotFoundException("Item with id: " + id + " not found!");
        }
    }

    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     * <p>
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */
    @Async
    public List<ItemDTO> processItemsAsync() {

//        List<Long> itemIds = itemRepository.findAllIds();
//
//        for (Long id : itemIds) {
//            CompletableFuture.runAsync(() -> {
//                try {
//                    Thread.sleep(100);
//
//                    Item item = itemRepository.findById(id).orElse(null);
//                    if (item == null) {
//                        return;
//                    }
//
//                    processedCount++;
//
//                    item.setStatus("PROCESSED");
//                    itemRepository.save(item);
//                    processedItems.add(item);
//
//                } catch (InterruptedException e) {
//                    System.out.println("Error: " + e.getMessage());
//                }
//            }, executor);
//        }
//
//        return processedItems;
        return new ArrayList<>();
    }

    private Item mapToItem(ItemDTO itemDTO) {
        return modelMapper.map(itemDTO, Item.class);
    }

    private ItemDTO mapToItemDTO(Item item) {
        return modelMapper.map(item, ItemDTO.class);
    }
}
