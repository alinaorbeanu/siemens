package com.siemens.internship.service.impl;

import com.siemens.internship.controller.dto.ItemDTO;
import com.siemens.internship.exception.EmailAlreadyExistsException;
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
        var item = mapToItem(itemDTO);

        return mapToItemDTO(itemRepository.save(item));
    }

    @Override
    public ItemDTO update(Long id, ItemDTO itemDTO) {
        ItemDTO itemToUpdate = findById(id);

        if (itemDTO.getEmail().equals(itemToUpdate.getEmail())) {
            buildItemWithoutEmail(itemDTO, itemToUpdate);

            return mapToItemDTO(itemRepository.save(mapToItem(itemToUpdate)));
        } else {
            ItemDTO itemByEmail = findByEmail(itemDTO.getEmail());
            if (itemByEmail == null) {
                buildItemWithoutEmail(itemDTO, itemToUpdate);
                itemToUpdate.setEmail(itemDTO.getEmail());
                return mapToItemDTO(itemRepository.save(mapToItem(itemToUpdate)));
            } else {
                throw new EmailAlreadyExistsException("Item with email: " + itemDTO.getEmail() + " already exists!");
            }
        }
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
    public CompletableFuture<List<ItemDTO>> processItemsAsync() {

        // The full explications are on the README.md file
        // Retrieve all items ids from the DB
        List<Long> itemIds = itemRepository.findAllIds();

        // I chose as tread-safe collection HashMap and I stored the items like this:
        // Key: 0 - value: a list with items status != "PROCESSED" and we should process
        // I don't need the items that already processed, so I didn't create a slot for them
        Map<Integer, List<ItemDTO>> itemStatusMap = new ConcurrentHashMap<>();
        itemStatusMap.put(0, Collections.synchronizedList(new ArrayList<>()));

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // An async task is used to process each item
        for (Long itemId : itemIds) {
            CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
                        try {
                            Thread.sleep(100);
                            // When saving each item, I check that each field is filled in, also the email is unique and valid
                            return mapToItemDTO(itemRepository.findById(itemId).orElseThrow());
                        } catch (InterruptedException e) {
                            throw new RuntimeException("Interrupted while retrieving item " + itemId, e);
                        }
                    }, executor)
                    .thenAcceptAsync(itemDTO -> {

                        // If status is not "PROCESSED" then it will be updated in DB and added to corresponding map
                        if (!"PROCESSED".equals(itemDTO.getStatus())) {
                            itemDTO.setStatus("PROCESSED");
                            itemRepository.save(mapToItem(itemDTO));
                            itemStatusMap.get(0).add(itemDTO);
                        }
                    }, executor)
                    .exceptionally(ex -> {
                        System.err.println("Error processing item ID " + itemId + ": " + ex.getMessage());
                        return null;
                    });

            futures.add(future);
        }

        // Wait for all tasks to complete and then return the newly processed items
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> itemStatusMap.get(0));
    }

    private Item mapToItem(ItemDTO itemDTO) {
        return modelMapper.map(itemDTO, Item.class);
    }

    private ItemDTO mapToItemDTO(Item item) {
        return modelMapper.map(item, ItemDTO.class);
    }

    private static void buildItemWithoutEmail(ItemDTO itemDTO, ItemDTO itemToUpdate) {
        itemToUpdate.setName(itemDTO.getName());
        itemToUpdate.setDescription(itemDTO.getDescription());
        itemToUpdate.setStatus(itemDTO.getStatus());
    }
}
