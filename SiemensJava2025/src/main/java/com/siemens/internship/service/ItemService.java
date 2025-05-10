package com.siemens.internship.service;

import com.siemens.internship.controller.dto.ItemDTO;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ItemService {

    List<ItemDTO> findAll();

    ItemDTO findById(Long id);

    ItemDTO findByEmail(String email);

    ItemDTO save(ItemDTO itemDTO);

    void deleteById(Long id);

    CompletableFuture<List<ItemDTO>> processItemsAsync();

}

