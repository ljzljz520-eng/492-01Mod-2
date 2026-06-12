package com.scaffolding.task;

import com.scaffolding.service.CertificateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CertificateStatusTask {

    @Autowired
    private CertificateService certificateService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void refreshCertStatus() {
        log.info("开始定时刷新证件状态");
        try {
            certificateService.refreshCertStatus();
            log.info("定时刷新证件状态完成");
        } catch (Exception e) {
            log.error("定时刷新证件状态失败", e);
        }
    }
}
