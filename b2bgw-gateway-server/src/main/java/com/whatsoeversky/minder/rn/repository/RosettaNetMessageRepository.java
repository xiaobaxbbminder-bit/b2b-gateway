package com.whatsoeversky.minder.rn.repository;

import com.whatsoeversky.minder.rn.entity.RosettaNetMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RosettaNetMessageRepository extends MongoRepository<RosettaNetMessage, String> {
    List<RosettaNetMessage> findByPersonalityDunsOrderByCreatedAtDesc(String personalityDuns);
    List<RosettaNetMessage> findByPartnerDunsOrderByCreatedAtDesc(String partnerDuns);
}
