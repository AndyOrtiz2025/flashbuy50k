package com.flashbuy.catalog.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flashbuy.catalog.model.Season;

public interface SeasonRepository extends JpaRepository<Season, UUID> {
    List<Season> findByIsActiveTrue();
}