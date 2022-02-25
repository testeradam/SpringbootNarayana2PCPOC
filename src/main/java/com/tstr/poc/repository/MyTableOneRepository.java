package com.tstr.poc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tstr.poc.model.MyTableOne;

@Repository
public interface MyTableOneRepository extends JpaRepository<MyTableOne, Long>{

}
