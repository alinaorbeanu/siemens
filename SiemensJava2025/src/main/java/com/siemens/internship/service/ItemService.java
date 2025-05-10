package com.siemens.internship.service;

import com.siemens.internship.controller.dto.ItemDTO;
import java.util.List;

public interface ItemService {

    List<ItemDTO> findAll();

    ItemDTO findById(Long id);

    ItemDTO findByEmail(String email);

    ItemDTO save(ItemDTO itemDTO);

    void deleteById(Long id);

    List<ItemDTO> processItemsAsync();

}

