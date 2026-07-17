package com.whatsoeversky.minder.rn.repository;

import com.whatsoeversky.minder.rn.entity.RosettaNetPersonality;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RosettaNetPersonalityRepository extends MongoRepository<RosettaNetPersonality, String> {
    Optional<RosettaNetPersonality> findByDuns(String duns);
    boolean existsByDuns(String duns);
}
