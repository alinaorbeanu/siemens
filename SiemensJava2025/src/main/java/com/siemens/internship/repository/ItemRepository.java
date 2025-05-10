package com.siemens.internship.repository;

import com.siemens.internship.model.Item;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("SELECT id FROM Item")
    List<Long> findAllIds();

    Optional<Item> findByEmail(String email);
}
