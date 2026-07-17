package com.whatsoeversky.minder.rn.controller;

import com.whatsoeversky.minder.common.Result;
import com.whatsoeversky.minder.rn.entity.RosettaNetPartner;
import com.whatsoeversky.minder.rn.entity.RosettaNetPersonality;
import com.whatsoeversky.minder.rn.entity.RosettaNetPipDefinition;
import com.whatsoeversky.minder.rn.dto.*;
import com.whatsoeversky.minder.rn.service.RosettaNetService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rosettanet")
public class RosettaNetController {

    @Resource
    private RosettaNetService rosettaNetService;

    // Personality
    @GetMapping("/personalities")
    public Result<List<RosettaNetPersonality>> listPersonalities() {
        return Result.success(rosettaNetService.listPersonalities());
    }

    @PostMapping("/personalities")
    public Result<RosettaNetPersonality> createPersonality(@RequestBody RosettaNetPersonalityReqDto dto) {
        return Result.success(rosettaNetService.createPersonality(dto));
    }

    @PutMapping("/personalities/{id}")
    public Result<RosettaNetPersonality> updatePersonality(@PathVariable String id, @RequestBody RosettaNetPersonalityReqDto dto) {
        return Result.success(rosettaNetService.updatePersonality(id, dto));
    }

    @DeleteMapping("/personalities/{id}")
    public Result<Void> deletePersonality(@PathVariable String id) {
        rosettaNetService.deletePersonality(id);
        return Result.success();
    }

    // Partner
    @GetMapping("/partners")
    public Result<List<RosettaNetPartner>> listPartners() {
        return Result.success(rosettaNetService.listPartners());
    }

    @PostMapping("/partners")
    public Result<RosettaNetPartner> createPartner(@RequestBody RosettaNetPartnerReqDto dto) {
        return Result.success(rosettaNetService.createPartner(dto));
    }

    @PutMapping("/partners/{id}")
    public Result<RosettaNetPartner> updatePartner(@PathVariable String id, @RequestBody RosettaNetPartnerReqDto dto) {
        return Result.success(rosettaNetService.updatePartner(id, dto));
    }

    @DeleteMapping("/partners/{id}")
    public Result<Void> deletePartner(@PathVariable String id) {
        rosettaNetService.deletePartner(id);
        return Result.success();
    }

    // PIP
    @GetMapping("/pips")
    public Result<List<RosettaNetPipDefinition>> listPips() {
        return Result.success(rosettaNetService.listPipDefinitions());
    }

    @PostMapping("/pips")
    public Result<RosettaNetPipDefinition> createPip(@RequestBody RosettaNetPipReqDto dto) {
        return Result.success(rosettaNetService.createPipDefinition(dto));
    }

    @PutMapping("/pips/{id}")
    public Result<RosettaNetPipDefinition> updatePip(@PathVariable String id, @RequestBody RosettaNetPipReqDto dto) {
        return Result.success(rosettaNetService.updatePipDefinition(id, dto));
    }

    @DeleteMapping("/pips/{id}")
    public Result<Void> deletePip(@PathVariable String id) {
        rosettaNetService.deletePipDefinition(id);
        return Result.success();
    }

    // Communication Config
    @GetMapping("/configs")
    public Result<List<RosettaNetCommConfigRespDto>> listConfigs() {
        return Result.success(rosettaNetService.listCommConfigs());
    }

    @PostMapping("/configs")
    public Result<RosettaNetCommConfigRespDto> createConfig(@RequestBody RosettaNetCommConfigReqDto dto) {
        return Result.success(rosettaNetService.createCommConfig(dto));
    }

    @PutMapping("/configs/{id}")
    public Result<RosettaNetCommConfigRespDto> updateConfig(@PathVariable String id, @RequestBody RosettaNetCommConfigReqDto dto) {
        return Result.success(rosettaNetService.updateCommConfig(id, dto));
    }

    @DeleteMapping("/configs/{id}")
    public Result<Void> deleteConfig(@PathVariable String id) {
        rosettaNetService.deleteCommConfig(id);
        return Result.success();
    }
}
