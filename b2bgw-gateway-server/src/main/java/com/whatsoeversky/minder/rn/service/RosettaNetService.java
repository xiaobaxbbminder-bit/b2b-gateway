package com.whatsoeversky.minder.rn.service;

import com.whatsoeversky.minder.rn.dto.*;
import com.whatsoeversky.minder.rn.entity.RosettaNetCommunicationConfig;
import com.whatsoeversky.minder.rn.entity.RosettaNetPartner;
import com.whatsoeversky.minder.rn.entity.RosettaNetPersonality;
import com.whatsoeversky.minder.rn.entity.RosettaNetPipDefinition;
import com.whatsoeversky.minder.rn.repository.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RosettaNetService {

    @Resource
    private RosettaNetPersonalityRepository personalityRepository;
    @Resource
    private RosettaNetPartnerRepository partnerRepository;
    @Resource
    private RosettaNetPipDefinitionRepository pipDefinitionRepository;
    @Resource
    private RosettaNetCommunicationConfigRepository commConfigRepository;
    @Resource
    private RosettaNetMessageRepository messageRepository;

    // ── Personality ──

    public List<RosettaNetPersonality> listPersonalities() {
        return personalityRepository.findAll();
    }

    public RosettaNetPersonality createPersonality(RosettaNetPersonalityReqDto dto) {
        if (personalityRepository.existsByDuns(dto.getDuns())) {
            throw new RuntimeException("DUNS 已存在: " + dto.getDuns());
        }
        RosettaNetPersonality entity = RosettaNetPersonality.builder()
                .name(dto.getName()).duns(dto.getDuns()).url(dto.getUrl())
                .signCert(dto.getSignCert()).encryptCert(dto.getEncryptCert())
                .build();
        return personalityRepository.save(entity);
    }

    public RosettaNetPersonality updatePersonality(String id, RosettaNetPersonalityReqDto dto) {
        RosettaNetPersonality p = personalityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("通信主体不存在"));
        p.setName(dto.getName()); p.setUrl(dto.getUrl());
        p.setSignCert(dto.getSignCert()); p.setEncryptCert(dto.getEncryptCert());
        return personalityRepository.save(p);
    }

    public void deletePersonality(String id) {
        personalityRepository.deleteById(id);
    }

    // ── Partner ──

    public List<RosettaNetPartner> listPartners() {
        return partnerRepository.findAll();
    }

    public RosettaNetPartner createPartner(RosettaNetPartnerReqDto dto) {
        if (partnerRepository.existsByDuns(dto.getDuns())) {
            throw new RuntimeException("DUNS 已存在: " + dto.getDuns());
        }
        RosettaNetPartner entity = RosettaNetPartner.builder()
                .name(dto.getName()).duns(dto.getDuns()).url(dto.getUrl())
                .signCert(dto.getSignCert()).encryptCert(dto.getEncryptCert())
                .build();
        return partnerRepository.save(entity);
    }

    public RosettaNetPartner updatePartner(String id, RosettaNetPartnerReqDto dto) {
        RosettaNetPartner p = partnerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("合作伙伴不存在"));
        p.setName(dto.getName()); p.setUrl(dto.getUrl());
        p.setSignCert(dto.getSignCert()); p.setEncryptCert(dto.getEncryptCert());
        return partnerRepository.save(p);
    }

    public void deletePartner(String id) {
        partnerRepository.deleteById(id);
    }

    // ── PIP Definition ──

    public List<RosettaNetPipDefinition> listPipDefinitions() {
        return pipDefinitionRepository.findAll();
    }

    public RosettaNetPipDefinition createPipDefinition(RosettaNetPipReqDto dto) {
        RosettaNetPipDefinition entity = RosettaNetPipDefinition.builder()
                .pipId(dto.getPipId()).pipVersion(dto.getPipVersion())
                .documentType(dto.getDocumentType()).description(dto.getDescription()).build();
        return pipDefinitionRepository.save(entity);
    }

    public RosettaNetPipDefinition updatePipDefinition(String id, RosettaNetPipReqDto dto) {
        RosettaNetPipDefinition p = pipDefinitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PIP 定义不存在"));
        p.setPipId(dto.getPipId()); p.setPipVersion(dto.getPipVersion());
        p.setDocumentType(dto.getDocumentType()); p.setDescription(dto.getDescription());
        return pipDefinitionRepository.save(p);
    }

    public void deletePipDefinition(String id) {
        pipDefinitionRepository.deleteById(id);
    }

    // ── Communication Config ──

    public List<RosettaNetCommConfigRespDto> listCommConfigs() {
        return commConfigRepository.findAll().stream().map(this::toCommConfigResp).toList();
    }

    public RosettaNetCommConfigRespDto createCommConfig(RosettaNetCommConfigReqDto dto) {
        RosettaNetCommunicationConfig entity = RosettaNetCommunicationConfig.builder()
                .personalityId(dto.getPersonalityId()).partnerId(dto.getPartnerId())
                .pipDefinitionId(dto.getPipDefinitionId())
                .direction(dto.getDirection())
                .signingEnabled(dto.getSigningEnabled()).signAlgorithm(dto.getSignAlgorithm())
                .encryptionEnabled(dto.getEncryptionEnabled()).encryptAlgorithm(dto.getEncryptAlgorithm())
                .build();
        return toCommConfigResp(commConfigRepository.save(entity));
    }

    public RosettaNetCommConfigRespDto updateCommConfig(String id, RosettaNetCommConfigReqDto dto) {
        RosettaNetCommunicationConfig c = commConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("通信配置不存在"));
        c.setPersonalityId(dto.getPersonalityId()); c.setPartnerId(dto.getPartnerId());
        c.setPipDefinitionId(dto.getPipDefinitionId());
        c.setDirection(dto.getDirection());
        c.setSigningEnabled(dto.getSigningEnabled()); c.setSignAlgorithm(dto.getSignAlgorithm());
        c.setEncryptionEnabled(dto.getEncryptionEnabled()); c.setEncryptAlgorithm(dto.getEncryptAlgorithm());
        return toCommConfigResp(commConfigRepository.save(c));
    }

    public void deleteCommConfig(String id) {
        commConfigRepository.deleteById(id);
    }

    private RosettaNetCommConfigRespDto toCommConfigResp(RosettaNetCommunicationConfig c) {
        RosettaNetCommConfigRespDto dto = new RosettaNetCommConfigRespDto();
        dto.setId(c.getId());
        dto.setPersonalityId(c.getPersonalityId());
        dto.setPartnerId(c.getPartnerId());
        dto.setPipDefinitionId(c.getPipDefinitionId());
        dto.setDirection(c.getDirection());
        dto.setSigningEnabled(c.getSigningEnabled());
        dto.setSignAlgorithm(c.getSignAlgorithm());
        dto.setEncryptionEnabled(c.getEncryptionEnabled());
        dto.setEncryptAlgorithm(c.getEncryptAlgorithm());

        personalityRepository.findById(c.getPersonalityId()).ifPresent(p -> {
            dto.setPersonalityName(p.getName());
            dto.setPersonalityDuns(p.getDuns());
        });
        partnerRepository.findById(c.getPartnerId()).ifPresent(p -> {
            dto.setPartnerName(p.getName());
            dto.setPartnerDuns(p.getDuns());
        });
        pipDefinitionRepository.findById(c.getPipDefinitionId()).ifPresent(p -> {
            dto.setPipId(p.getPipId());
            dto.setPipVersion(p.getPipVersion());
            dto.setDocumentType(p.getDocumentType());
        });
        return dto;
    }
}
