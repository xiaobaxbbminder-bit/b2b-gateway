package com.whatsoeversky.minder.sys.repository;

import com.whatsoeversky.minder.sys.entity.SysDictItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SysDictItemRepository extends MongoRepository<SysDictItem, String> {
    List<SysDictItem> findByTypeOrderBySortAsc(String type);

    List<SysDictItem> findByTypeAndEnabledTrueOrderBySortAsc(String type);

    boolean existsByType(String type);
}
