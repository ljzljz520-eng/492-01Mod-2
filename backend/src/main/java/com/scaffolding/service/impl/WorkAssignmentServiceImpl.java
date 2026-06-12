package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.Work;
import com.scaffolding.entity.WorkAssignment;
import com.scaffolding.exception.BusinessException;
import com.scaffolding.mapper.WorkAssignmentMapper;
import com.scaffolding.mapper.WorkMapper;
import com.scaffolding.service.CertificateService;
import com.scaffolding.service.WorkAssignmentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WorkAssignmentServiceImpl extends ServiceImpl<WorkAssignmentMapper, WorkAssignment> implements WorkAssignmentService {

    @Autowired
    private WorkMapper workMapper;

    @Autowired
    private CertificateService certificateService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Page<WorkAssignment> pageQuery(Long current, Long size, Long workId, Long userId, String assignStatus) {
        Page<WorkAssignment> page = new Page<>(current, size);
        LambdaQueryWrapper<WorkAssignment> wrapper = new LambdaQueryWrapper<>();
        if (workId != null) {
            wrapper.eq(WorkAssignment::getWorkId, workId);
        }
        if (userId != null) {
            wrapper.eq(WorkAssignment::getUserId, userId);
        }
        if (StringUtils.hasText(assignStatus)) {
            wrapper.eq(WorkAssignment::getAssignStatus, assignStatus);
        }
        wrapper.orderByDesc(WorkAssignment::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public WorkAssignment assignWorker(WorkAssignment assignment) {
        Work work = workMapper.selectById(assignment.getWorkId());
        if (work == null) {
            throw new BusinessException("班次不存在");
        }

        String requiredCerts = work.getRequiredCerts();
        Map<String, Object> checkResult = certificateService.checkUserCerts(assignment.getUserId(), requiredCerts);

        boolean passed = (boolean) checkResult.get("passed");
        List<String> warnings = (List<String>) checkResult.get("warnings");

        if (!passed) {
            throw new BusinessException("证件检查未通过，无法排入现场班次");
        }

        assignment.setWorkName(work.getWorkName());
        assignment.setCertCheckPassed(1);
        assignment.setAssignStatus("assigned");
        assignment.setCertWarnings(warnings.isEmpty() ? null : String.join("；", warnings));
        assignment.setCreateTime(LocalDateTime.now());
        assignment.setUpdateTime(LocalDateTime.now());

        try {
            assignment.setCertCheckDetail(objectMapper.writeValueAsString(checkResult.get("detail")));
        } catch (JsonProcessingException e) {
            assignment.setCertCheckDetail("[]");
        }

        this.save(assignment);
        return assignment;
    }

    @Override
    public Map<String, Object> previewCertCheck(Long workId, Long userId) {
        Work work = workMapper.selectById(workId);
        if (work == null) {
            throw new BusinessException("班次不存在");
        }
        return certificateService.checkUserCerts(userId, work.getRequiredCerts());
    }
}
