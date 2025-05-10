package com.siemens.internship.controller.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siemens.internship.controller.dto.ItemDTO;
import com.siemens.internship.utils.TestDataBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class ITItemController {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private ItemDTO item1;
    private ItemDTO item2;

    @BeforeEach
    void setup() {
        item1 = TestDataBuilder.buildItemDTOWithoutId(1);
        item2 = TestDataBuilder.buildItemDTOWithoutId(2);
    }

    @AfterEach
    void cleanUp() {
        item1 = null;
        item2 = null;
    }

    @Test
    void testCreateItem() throws Exception {
        ItemDTO itemDTO = TestDataBuilder.buildItemDTOWithoutId(1);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Item 1"));
    }


    @Test
    void testGetAllItems() throws Exception {
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetItemById() throws Exception {
        String response = mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item1)))
                .andReturn().getResponse().getContentAsString();

        ItemDTO saved = objectMapper.readValue(response, ItemDTO.class);

        mockMvc.perform(get("/api/items/" + saved.getId()))
                .andExpect(status().isFound())
                .andExpect(jsonPath("$.name").value("Item 1"));
    }

    @Test
    void testUpdateItem() throws Exception {
        String response = mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item1)))
                .andReturn().getResponse().getContentAsString();

        ItemDTO saved = objectMapper.readValue(response, ItemDTO.class);
        saved.setName("Updated Item");

        mockMvc.perform(put("/api/items/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saved)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Item"));
    }

    @Test
    void testDeleteItem() throws Exception {
        String response = mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item1)))
                .andReturn().getResponse().getContentAsString();

        ItemDTO saved = objectMapper.readValue(response, ItemDTO.class);

        mockMvc.perform(delete("/api/items/" + saved.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/items/" + saved.getId()))
                .andExpect(status().isNotFound());
    }
}
