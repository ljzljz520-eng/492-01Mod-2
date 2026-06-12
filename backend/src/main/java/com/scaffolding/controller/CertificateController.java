package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.entity.Certificate;
import com.scaffolding.entity.CertificateHistory;
import com.scaffolding.entity.User;
import com.scaffolding.exception.BusinessException;
import com.scaffolding.service.CertificateHistoryService;
import com.scaffolding.service.CertificateService;
import com.scaffolding.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/certificate")
@Api(tags = "证件管理")
public class CertificateController {

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private CertificateHistoryService certificateHistoryService;

    @Autowired
    private UserService userService;

    @PostMapping
    @ApiOperation("上传证件")
    public Result<Certificate> upload(@RequestBody Certificate certificate) {
        try {
            if (certificate.getUserId() == null) {
                return Result.error("用户ID不能为空");
            }
            if (!StringUtils.hasText(certificate.getCertType())) {
                return Result.error("证件类型不能为空");
            }
            User user = userService.getById(certificate.getUserId());
            if (user == null) {
                return Result.error("用户不存在");
            }
            certificate.setUserName(user.getNickname());
            Long operatorId = 1L;
            String operatorName = "admin";
            Certificate result = certificateService.uploadCertificate(certificate, operatorId, operatorName);
            return Result.success("上传成功", result);
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("上传证件失败", e);
            return Result.error("上传失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ApiOperation("更新证件（上传新证件）")
    public Result<Certificate> update(@PathVariable Long id, @RequestBody Certificate certificate) {
        try {
            certificate.setId(id);
            Long operatorId = 1L;
            String operatorName = "admin";
            Certificate result = certificateService.updateCertificate(certificate, operatorId, operatorName);
            return Result.success("更新成功", result);
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新证件失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/audit")
    @ApiOperation("审核证件")
    public Result<Certificate> audit(
            @PathVariable Long id,
            @RequestBody AuditRequest request) {
        try {
            if (!StringUtils.hasText(request.getAuditStatus())) {
                return Result.error("审核状态不能为空");
            }
            if (!"approved".equals(request.getAuditStatus()) && !"rejected".equals(request.getAuditStatus())) {
                return Result.error("审核状态只能是approved或rejected");
            }
            Long auditorId = 1L;
            String auditorName = "admin";
            Certificate result = certificateService.auditCertificate(
                    id, request.getAuditStatus(), request.getAuditRemark(), auditorId, auditorName);
            return Result.success("审核成功", result);
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("审核证件失败", e);
            return Result.error("审核失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询证件")
    public Result<Certificate> getById(@PathVariable Long id) {
        Certificate certificate = certificateService.getById(id);
        if (certificate == null) {
            return Result.error("证件不存在");
        }
        return Result.success(certificate);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询证件")
    public Result<PageResult<Certificate>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String certType,
            @RequestParam(required = false) String certStatus,
            @RequestParam(required = false) String auditStatus) {
        Page<Certificate> page = certificateService.pageQuery(current, size, userId, certType, certStatus, auditStatus);
        PageResult<Certificate> pageResult = new PageResult<>(
                page.getTotal(),
                page.getRecords(),
                page.getCurrent(),
                page.getSize()
        );
        return Result.success(pageResult);
    }

    @GetMapping("/user/{userId}")
    @ApiOperation("查询用户的所有证件")
    public Result<List<Certificate>> getByUserId(@PathVariable Long userId) {
        List<Certificate> list = certificateService.getByUserId(userId);
        return Result.success(list);
    }

    @GetMapping("/{id}/history")
    @ApiOperation("查询证件的变更历史")
    public Result<PageResult<CertificateHistory>> getHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Page<CertificateHistory> page = certificateHistoryService.pageQuery(current, size, id, null);
        PageResult<CertificateHistory> pageResult = new PageResult<>(
                page.getTotal(),
                page.getRecords(),
                page.getCurrent(),
                page.getSize()
        );
        return Result.success(pageResult);
    }

    @PostMapping("/refresh-status")
    @ApiOperation("刷新所有证件状态")
    public Result<?> refreshStatus() {
        try {
            certificateService.refreshCertStatus();
            return Result.success("刷新成功");
        } catch (Exception e) {
            log.error("刷新证件状态失败", e);
            return Result.error("刷新失败：" + e.getMessage());
        }
    }

    @PostMapping("/check")
    @ApiOperation("检查用户证件是否满足要求")
    public Result<Map<String, Object>> checkCerts(
            @RequestParam Long userId,
            @RequestParam(required = false) String requiredCerts) {
        Map<String, Object> result = certificateService.checkUserCerts(userId, requiredCerts);
        return Result.success(result);
    }

    public static class AuditRequest {
        private String auditStatus;
        private String auditRemark;

        public String getAuditStatus() {
            return auditStatus;
        }

        public void setAuditStatus(String auditStatus) {
            this.auditStatus = auditStatus;
        }

        public String getAuditRemark() {
            return auditRemark;
        }

        public void setAuditRemark(String auditRemark) {
            this.auditRemark = auditRemark;
        }
    }
}
