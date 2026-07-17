package com.whatsoeversky.minder.rn.repository;

import com.whatsoeversky.minder.rn.entity.RosettaNetPipDefinition;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RosettaNetPipDefinitionRepository extends MongoRepository<RosettaNetPipDefinition, String> {
    Optional<RosettaNetPipDefinition> findByPipIdAndPipVersion(String pipId, String pipVersion);
}
