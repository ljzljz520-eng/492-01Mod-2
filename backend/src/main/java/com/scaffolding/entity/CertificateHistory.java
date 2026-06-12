package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("certificate_history")
public class CertificateHistory extends BaseEntity {

    private Long certificateId;

    private Long userId;

    private String userName;

    private String certType;

    private String oldCertNo;

    private String oldCertName;

    private Long oldFileId;

    private String oldFileUrl;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate oldExpireDate;

    private Long oldAuditorId;

    private String oldAuditorName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime oldAuditTime;

    private String newCertNo;

    private String newCertName;

    private Long newFileId;

    private String newFileUrl;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate newExpireDate;

    private Long operatorId;

    private String operatorName;
}
