package com.whatsoeversky.minder.sys.service;

import com.whatsoeversky.minder.sys.entity.SysDictItem;
import com.whatsoeversky.minder.sys.repository.SysDictItemRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysDictService {

    @Resource
    private SysDictItemRepository sysDictItemRepository;

    public List<SysDictItem> getByType(String type) {
        return sysDictItemRepository.findByTypeAndEnabledTrueOrderBySortAsc(type);
    }

    public List<SysDictItem> getAllByType(String type) {
        return sysDictItemRepository.findByTypeOrderBySortAsc(type);
    }

    public List<String> getTypes() {
        return sysDictItemRepository.findAll().stream()
                .map(SysDictItem::getType)
                .distinct()
                .sorted()
                .toList();
    }

    public SysDictItem create(SysDictItem item) {
        item.setId(null);
        if (item.getEnabled() == null) item.setEnabled(true);
        if (item.getSort() == null) item.setSort(0);
        return sysDictItemRepository.save(item);
    }

    public SysDictItem update(String id, SysDictItem item) {
        SysDictItem exist = sysDictItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("字典项不存在"));
        exist.setKey(item.getKey());
        exist.setValue(item.getValue());
        exist.setSort(item.getSort());
        exist.setEnabled(item.getEnabled());
        return sysDictItemRepository.save(exist);
    }

    public void delete(String id) {
        sysDictItemRepository.deleteById(id);
    }
}
