package com.whatsoeversky.minder.sys.controller;

import com.whatsoeversky.minder.common.Result;
import com.whatsoeversky.minder.sys.entity.SysDictItem;
import com.whatsoeversky.minder.sys.service.SysDictService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dict")
public class SysDictController {

    @Resource
    private SysDictService sysDictService;

    @GetMapping("/types")
    public Result<List<String>> types() {
        return Result.success(sysDictService.getTypes());
    }

    @GetMapping("/{type}")
    public Result<List<SysDictItem>> getByType(@PathVariable String type) {
        return Result.success(sysDictService.getByType(type));
    }

    @GetMapping("/{type}/all")
    public Result<List<SysDictItem>> getAllByType(@PathVariable String type) {
        return Result.success(sysDictService.getAllByType(type));
    }

    @PostMapping
    public Result<SysDictItem> create(@RequestBody SysDictItem item) {
        return Result.success(sysDictService.create(item));
    }

    @PutMapping("/{id}")
    public Result<SysDictItem> update(@PathVariable String id, @RequestBody SysDictItem item) {
        return Result.success(sysDictService.update(id, item));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        sysDictService.delete(id);
        return Result.success();
    }
}
