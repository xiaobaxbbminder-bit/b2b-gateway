package com.whatsoeversky.minder.rn.repository;

import com.whatsoeversky.minder.rn.entity.RosettaNetCommunicationConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RosettaNetCommunicationConfigRepository extends MongoRepository<RosettaNetCommunicationConfig, String> {
    List<RosettaNetCommunicationConfig> findByPersonalityId(String personalityId);
    List<RosettaNetCommunicationConfig> findByPartnerId(String partnerId);
}
