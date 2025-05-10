package com.siemens.internship.repository;

import com.siemens.internship.model.Item;
import com.siemens.internship.utils.TestDataBuilder;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void testFindAllIds() {
        Item item1 = TestDataBuilder.buildItemWithoutId(1);
        Item item2 = TestDataBuilder.buildItemWithoutId(2);

        Item savedItem1 = itemRepository.save(item1);
        Item savedItem2 = itemRepository.save(item2);

        List<Long> ids = itemRepository.findAllIds();

        assertThat(ids).containsExactlyInAnyOrder(savedItem1.getId(), savedItem2.getId());
    }

    @Test
    void testFindByEmail() {
        Item item = TestDataBuilder.buildItem(1);
        itemRepository.save(item);

        Optional<Item> found = itemRepository.findByEmail("item1@gmail.com");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Item 1");
    }

    @Test
    void testCrudOperations() {
        Item item = TestDataBuilder.buildItem(1);

        Item saved = itemRepository.save(item);
        assertThat(saved.getId()).isNotNull();

        Optional<Item> found = itemRepository.findById(saved.getId());
        assertThat(found).isPresent();

        itemRepository.deleteById(saved.getId());
        assertThat(itemRepository.findById(saved.getId())).isNotPresent();
    }
}