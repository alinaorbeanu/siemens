package com.siemens.internship.controller.impl;

import com.siemens.internship.controller.ItemController;
import com.siemens.internship.controller.dto.ItemDTO;
import com.siemens.internship.exception.ObjectNotFoundException;
import com.siemens.internship.service.ItemService;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ItemControllerImpl implements ItemController {

    @Autowired
    private ItemService itemService;

    @Override
    public ResponseEntity<List<ItemDTO>> getAllItems() {
        return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ItemDTO> createItem(ItemDTO itemDTO) {
        return new ResponseEntity<>(itemService.save(itemDTO), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ItemDTO> getItemById(Long id) {
        return new ResponseEntity<>(itemService.findById(id), HttpStatus.OK);
    }

    //TO DO:
    @Override
    public ResponseEntity<ItemDTO> updateItem(Long id, ItemDTO itemDTO) {
        try {
            itemService.findById(id);
            itemDTO.setId(id);
            return new ResponseEntity<>(itemService.save(itemDTO), HttpStatus.CREATED);
        } catch (ObjectNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity<Void> deleteItem(Long id) {
        itemService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<List<ItemDTO>> processItems() {
        try {
            return new ResponseEntity<>(itemService.processItemsAsync().get(), HttpStatus.OK);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException();
        }
    }
}
