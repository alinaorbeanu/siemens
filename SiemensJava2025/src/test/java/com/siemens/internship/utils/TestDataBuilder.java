package com.siemens.internship.utils;

import com.siemens.internship.controller.dto.ItemDTO;
import com.siemens.internship.model.Item;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TestDataBuilder {

    public static ItemDTO buildItemDTO(int number) {
        return ItemDTO.builder()
                .id(1L)
                .name("Item " + number)
                .description("Description " + number)
                .status("NEW")
                .email("item" + number + "@gmail.com")
                .build();
    }

    public static ItemDTO buildItemDTOWithoutId(int number) {
        return ItemDTO.builder()
                .name("Item " + number)
                .description("Description " + number)
                .status("NEW")
                .email("item" + number + "@gmail.com")
                .build();
    }

    public static Item buildItem(int number) {
        return Item.builder()
                .id(1L)
                .name("Item " + number)
                .description("Description " + number)
                .status("NEW")
                .email("item" + number + "@gmail.com")
                .build();
    }

    public static Item buildItemWithoutId(int number) {
        return Item.builder()
                .name("Item " + number)
                .description("Description " + number)
                .status("NEW")
                .email("item" + number + "@gmail.com")
                .build();
    }
}
