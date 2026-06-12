package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.Certificate;

import java.util.List;
import java.util.Map;

public interface CertificateService extends IService<Certificate> {

    Page<Certificate> pageQuery(Long current, Long size, Long userId, String certType, String certStatus, String auditStatus);

    List<Certificate> getByUserId(Long userId);

    Map<String, Object> checkUserCerts(Long userId, String requiredCerts);

    Certificate uploadCertificate(Certificate certificate, Long operatorId, String operatorName);

    Certificate updateCertificate(Certificate certificate, Long operatorId, String operatorName);

    Certificate auditCertificate(Long id, String auditStatus, String auditRemark, Long auditorId, String auditorName);

    void refreshCertStatus();
}
