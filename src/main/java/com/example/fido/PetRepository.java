package com.example.fido;

import org.springframework.data.repository.CrudRepository;

import java.util.ArrayList;

public interface PetRepository extends CrudRepository<Pet, Long>{
  ArrayList<Pet> findAllByStatus(String status);
}