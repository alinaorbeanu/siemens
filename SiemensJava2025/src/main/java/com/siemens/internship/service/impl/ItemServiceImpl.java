package com.siemens.internship.service.impl;

import com.siemens.internship.controller.dto.ItemDTO;
import com.siemens.internship.exception.ObjectNotFoundException;
import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.service.ItemService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
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

    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    public List<ItemDTO> findAll() {
        List<Item> items = itemRepository.findAll();

        return items.stream()
                .map(this::mapToItemDTO)
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

    @Async
    public List<ItemDTO> processItemsAsync() {
        List<Long> itemIds = itemRepository.findAllIds();

        //
        Map<Integer, List<ItemDTO>> itemStatusMap = new ConcurrentHashMap<>();
        itemStatusMap.put(0, Collections.synchronizedList(new ArrayList<>())); //
        itemStatusMap.put(1, Collections.synchronizedList(new ArrayList<>())); //

        //

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Long itemId : itemIds) {
            CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
                        try {
                            Thread.sleep(100); // Simulated delay
                            // Guaranteed non-null, so we skip null checks
                            return mapToItemDTO(itemRepository.findById(itemId).orElseThrow());
                        } catch (InterruptedException e) {
                            throw new RuntimeException("Interrupted while retrieving item " + itemId, e);
                        }
                    }, executor)
                    .thenAcceptAsync(itemDTO -> {
                        if (!"PROCESSED".equals(itemDTO.getStatus())) {
                            itemDTO.setStatus("PROCESSED");
                            itemRepository.save(mapToItem(itemDTO));
                            itemStatusMap.get(1).add(itemDTO); // processed
                        } else {
                            itemStatusMap.get(0).add(itemDTO); // already processed
                        }
                    }, executor)
                    .exceptionally(ex -> {
                        System.err.println("Error processing item ID " + itemId + ": " + ex.getMessage());
                        return null;
                    });

            futures.add(future);
        }

        // Return only after all items are processed and no unprocessed items remain
        try {
            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v -> itemStatusMap.get(1)).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException();
        }
    }

    private Item mapToItem(ItemDTO itemDTO) {
        return modelMapper.map(itemDTO, Item.class);
    }

    private ItemDTO mapToItemDTO(Item item) {
        return modelMapper.map(item, ItemDTO.class);
    }
}
