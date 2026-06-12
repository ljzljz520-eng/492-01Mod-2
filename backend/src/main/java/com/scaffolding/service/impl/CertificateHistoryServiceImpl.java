package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.CertificateHistory;
import com.scaffolding.mapper.CertificateHistoryMapper;
import com.scaffolding.service.CertificateHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CertificateHistoryServiceImpl extends ServiceImpl<CertificateHistoryMapper, CertificateHistory> implements CertificateHistoryService {

    @Override
    public Page<CertificateHistory> pageQuery(Long current, Long size, Long certificateId, Long userId) {
        Page<CertificateHistory> page = new Page<>(current, size);
        LambdaQueryWrapper<CertificateHistory> wrapper = new LambdaQueryWrapper<>();
        if (certificateId != null) {
            wrapper.eq(CertificateHistory::getCertificateId, certificateId);
        }
        if (userId != null) {
            wrapper.eq(CertificateHistory::getUserId, userId);
        }
        wrapper.orderByDesc(CertificateHistory::getCreateTime);
        return this.page(page, wrapper);
    }
}
