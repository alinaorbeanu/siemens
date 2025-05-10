package com.siemens.internship.controller.impl;

import com.siemens.internship.controller.ItemController;
import com.siemens.internship.controller.dto.ItemDTO;
import com.siemens.internship.exception.ObjectNotFoundException;
import com.siemens.internship.service.ItemService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
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
    public ResponseEntity<ItemDTO> createItem(ItemDTO itemDTO, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
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
        return new ResponseEntity<>(itemService.processItemsAsync(), HttpStatus.OK);
    }
}
