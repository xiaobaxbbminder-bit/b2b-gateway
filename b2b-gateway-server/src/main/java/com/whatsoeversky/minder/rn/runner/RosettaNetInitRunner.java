package com.whatsoeversky.minder.rn.runner;

import com.whatsoeversky.minder.rn.entity.RosettaNetPipDefinition;
import com.whatsoeversky.minder.rn.repository.RosettaNetPipDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class RosettaNetInitRunner implements ApplicationRunner {

    @Autowired
    private RosettaNetPipDefinitionRepository pipDefinitionRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (pipDefinitionRepository.count() > 0) return;

        pipDefinitionRepository.save(RosettaNetPipDefinition.builder()
                .pipId("3A4").pipVersion("V02.02").documentType("PurchaseOrderRequest")
                .description("采购订单").build());
        pipDefinitionRepository.save(RosettaNetPipDefinition.builder()
                .pipId("3A4").pipVersion("V02.02").documentType("PurchaseOrderResponse")
                .description("采购订单确认").build());
        pipDefinitionRepository.save(RosettaNetPipDefinition.builder()
                .pipId("3B2").pipVersion("V02.02").documentType("InvoiceRequest")
                .description("发票通知").build());
        pipDefinitionRepository.save(RosettaNetPipDefinition.builder()
                .pipId("3B2").pipVersion("V02.02").documentType("InvoiceResponse")
                .description("发票确认").build());
        pipDefinitionRepository.save(RosettaNetPipDefinition.builder()
                .pipId("3C3").pipVersion("V02.02").documentType("OrderStatusRequest")
                .description("订单状态查询").build());
    }
}
