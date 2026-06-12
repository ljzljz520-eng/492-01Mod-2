package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.Certificate;
import com.scaffolding.entity.CertificateHistory;
import com.scaffolding.exception.BusinessException;
import com.scaffolding.mapper.CertificateMapper;
import com.scaffolding.mapper.CertificateHistoryMapper;
import com.scaffolding.service.CertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CertificateServiceImpl extends ServiceImpl<CertificateMapper, Certificate> implements CertificateService {

    private static final long EXPIRING_THRESHOLD_DAYS = 30;

    private static final Map<String, String> CERT_TYPE_MAP = new LinkedHashMap<>();

    static {
        CERT_TYPE_MAP.put("electrician", "电工证");
        CERT_TYPE_MAP.put("welder", "焊工证");
        CERT_TYPE_MAP.put("height_work", "高处作业证");
        CERT_TYPE_MAP.put("health", "健康证");
    }

    @Autowired
    private CertificateHistoryMapper certificateHistoryMapper;

    @Override
    public Page<Certificate> pageQuery(Long current, Long size, Long userId, String certType, String certStatus, String auditStatus) {
        Page<Certificate> page = new Page<>(current, size);
        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(Certificate::getUserId, userId);
        }
        if (StringUtils.hasText(certType)) {
            wrapper.eq(Certificate::getCertType, certType);
        }
        if (StringUtils.hasText(certStatus)) {
            wrapper.eq(Certificate::getCertStatus, certStatus);
        }
        if (StringUtils.hasText(auditStatus)) {
            wrapper.eq(Certificate::getAuditStatus, auditStatus);
        }
        wrapper.orderByDesc(Certificate::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public List<Certificate> getByUserId(Long userId) {
        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Certificate::getUserId, userId);
        wrapper.orderByAsc(Certificate::getCertType);
        return this.list(wrapper);
    }

    @Override
    public Map<String, Object> checkUserCerts(Long userId, String requiredCerts) {
        Map<String, Object> result = new HashMap<>();
        if (!StringUtils.hasText(requiredCerts)) {
            result.put("passed", true);
            result.put("detail", Collections.emptyList());
            result.put("warnings", Collections.emptyList());
            return result;
        }

        List<String> requiredList = Arrays.asList(requiredCerts.split(","));
        List<Certificate> userCerts = getByUserId(userId);
        Map<String, Certificate> certMap = userCerts.stream()
                .collect(Collectors.toMap(Certificate::getCertType, c -> c, (a, b) -> a));

        List<Map<String, Object>> detail = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        boolean allPassed = true;

        for (String reqType : requiredList) {
            String typeName = CERT_TYPE_MAP.getOrDefault(reqType, reqType);
            Map<String, Object> item = new HashMap<>();
            item.put("certType", reqType);
            item.put("certTypeName", typeName);

            Certificate cert = certMap.get(reqType);
            if (cert == null) {
                item.put("status", "missing");
                item.put("message", "缺少" + typeName);
                allPassed = false;
            } else if ("expired".equals(cert.getCertStatus())) {
                item.put("status", "expired");
                item.put("message", typeName + "已过期");
                item.put("expireDate", cert.getExpireDate().toString());
                allPassed = false;
            } else if ("rejected".equals(cert.getAuditStatus())) {
                item.put("status", "rejected");
                item.put("message", typeName + "审核未通过");
                allPassed = false;
            } else if ("expiring".equals(cert.getCertStatus())) {
                item.put("status", "expiring");
                long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), cert.getExpireDate());
                item.put("message", typeName + "即将过期（剩余" + daysLeft + "天）");
                item.put("expireDate", cert.getExpireDate().toString());
                item.put("daysLeft", daysLeft);
                warnings.add(typeName + "将于" + cert.getExpireDate() + "到期，剩余" + daysLeft + "天");
            } else if (!"approved".equals(cert.getAuditStatus())) {
                item.put("status", "pending_audit");
                item.put("message", typeName + "待审核");
                allPassed = false;
            } else {
                item.put("status", "valid");
                item.put("message", typeName + "有效");
                item.put("expireDate", cert.getExpireDate().toString());
            }
            detail.add(item);
        }

        result.put("passed", allPassed);
        result.put("detail", detail);
        result.put("warnings", warnings);
        return result;
    }

    @Override
    public Certificate uploadCertificate(Certificate certificate, Long operatorId, String operatorName) {
        certificate.setAuditStatus("pending");
        certificate.setCertStatus(calcCertStatus(certificate.getExpireDate()));
        certificate.setCreateTime(LocalDateTime.now());
        certificate.setUpdateTime(LocalDateTime.now());
        this.save(certificate);
        return certificate;
    }

    @Override
    public Certificate updateCertificate(Certificate certificate, Long operatorId, String operatorName) {
        Certificate oldCert = this.getById(certificate.getId());
        if (oldCert == null) {
            throw new BusinessException("证件不存在");
        }

        CertificateHistory history = new CertificateHistory();
        history.setCertificateId(oldCert.getId());
        history.setUserId(oldCert.getUserId());
        history.setUserName(oldCert.getUserName());
        history.setCertType(oldCert.getCertType());
        history.setOldCertNo(oldCert.getCertNo());
        history.setOldCertName(oldCert.getCertName());
        history.setOldFileId(oldCert.getFileId());
        history.setOldFileUrl(oldCert.getFileUrl());
        history.setOldExpireDate(oldCert.getExpireDate());
        history.setOldAuditorId(oldCert.getAuditorId());
        history.setOldAuditorName(oldCert.getAuditorName());
        history.setOldAuditTime(oldCert.getAuditTime());
        history.setNewCertNo(certificate.getCertNo());
        history.setNewCertName(certificate.getCertName());
        history.setNewFileId(certificate.getFileId());
        history.setNewFileUrl(certificate.getFileUrl());
        history.setNewExpireDate(certificate.getExpireDate());
        history.setOperatorId(operatorId);
        history.setOperatorName(operatorName);
        history.setCreateTime(LocalDateTime.now());
        certificateHistoryMapper.insert(history);

        certificate.setCertStatus(calcCertStatus(certificate.getExpireDate()));
        certificate.setAuditStatus("pending");
        certificate.setAuditorId(null);
        certificate.setAuditorName(null);
        certificate.setAuditTime(null);
        certificate.setAuditRemark(null);
        certificate.setUpdateTime(LocalDateTime.now());
        this.updateById(certificate);
        return certificate;
    }

    @Override
    public Certificate auditCertificate(Long id, String auditStatus, String auditRemark, Long auditorId, String auditorName) {
        Certificate cert = this.getById(id);
        if (cert == null) {
            throw new BusinessException("证件不存在");
        }
        cert.setAuditStatus(auditStatus);
        cert.setAuditorId(auditorId);
        cert.setAuditorName(auditorName);
        cert.setAuditTime(LocalDateTime.now());
        cert.setAuditRemark(auditRemark);
        cert.setUpdateTime(LocalDateTime.now());
        this.updateById(cert);
        return cert;
    }

    @Override
    public void refreshCertStatus() {
        LambdaQueryWrapper<Certificate> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Certificate::getCertStatus, Arrays.asList("valid", "expiring"));
        List<Certificate> certs = this.list(wrapper);
        for (Certificate cert : certs) {
            String newStatus = calcCertStatus(cert.getExpireDate());
            if (!newStatus.equals(cert.getCertStatus())) {
                cert.setCertStatus(newStatus);
                cert.setUpdateTime(LocalDateTime.now());
                this.updateById(cert);
            }
        }
    }

    private String calcCertStatus(LocalDate expireDate) {
        if (expireDate == null) {
            return "valid";
        }
        LocalDate today = LocalDate.now();
        if (expireDate.isBefore(today)) {
            return "expired";
        }
        long daysLeft = ChronoUnit.DAYS.between(today, expireDate);
        if (daysLeft <= EXPIRING_THRESHOLD_DAYS) {
            return "expiring";
        }
        return "valid";
    }
}
