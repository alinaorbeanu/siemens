package com.siemens.internship.service;

import com.siemens.internship.model.Item;
import java.util.List;
import java.util.Optional;

public interface ItemService {

    List<Item> findAll();

    Optional<Item> findById(Long id);

    Item save(Item item);

    void deleteById(Long id);

    List<Item> processItemsAsync();

}

