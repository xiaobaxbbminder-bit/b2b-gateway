package com.whatsoeversky.minder.rn.repository;

import com.whatsoeversky.minder.rn.entity.RosettaNetPartner;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RosettaNetPartnerRepository extends MongoRepository<RosettaNetPartner, String> {
    Optional<RosettaNetPartner> findByDuns(String duns);
    boolean existsByDuns(String duns);
}
