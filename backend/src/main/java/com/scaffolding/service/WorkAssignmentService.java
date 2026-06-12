package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.WorkAssignment;

import java.util.Map;

public interface WorkAssignmentService extends IService<WorkAssignment> {

    Page<WorkAssignment> pageQuery(Long current, Long size, Long workId, Long userId, String assignStatus);

    WorkAssignment assignWorker(WorkAssignment assignment);

    Map<String, Object> previewCertCheck(Long workId, Long userId);
}
