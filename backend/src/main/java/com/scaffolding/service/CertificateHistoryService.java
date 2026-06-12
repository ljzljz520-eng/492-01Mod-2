package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.CertificateHistory;

public interface CertificateHistoryService extends IService<CertificateHistory> {

    Page<CertificateHistory> pageQuery(Long current, Long size, Long certificateId, Long userId);
}
