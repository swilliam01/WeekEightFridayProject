package com.example.listthings;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ListingRepository extends CrudRepository<Listing, Long>{
    public List<Listing> findAllByOrderByDate();
}