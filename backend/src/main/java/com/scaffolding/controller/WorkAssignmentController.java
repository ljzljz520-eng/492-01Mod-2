package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.entity.WorkAssignment;
import com.scaffolding.exception.BusinessException;
import com.scaffolding.service.WorkAssignmentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/work-assignment")
@Api(tags = "排班管理")
public class WorkAssignmentController {

    @Autowired
    private WorkAssignmentService workAssignmentService;

    @PostMapping
    @ApiOperation("排班（分配工人到班次）")
    public Result<WorkAssignment> assign(@RequestBody WorkAssignment assignment) {
        try {
            if (assignment.getWorkId() == null) {
                return Result.error("班次ID不能为空");
            }
            if (assignment.getUserId() == null) {
                return Result.error("用户ID不能为空");
            }
            WorkAssignment result = workAssignmentService.assignWorker(assignment);
            return Result.success("排班成功", result);
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("排班失败", e);
            return Result.error("排班失败：" + e.getMessage());
        }
    }

    @GetMapping("/preview-cert-check")
    @ApiOperation("预览证件检查结果（排班前检查）")
    public Result<Map<String, Object>> previewCertCheck(
            @RequestParam Long workId,
            @RequestParam Long userId) {
        try {
            Map<String, Object> result = workAssignmentService.previewCertCheck(workId, userId);
            return Result.success(result);
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("预览证件检查失败", e);
            return Result.error("检查失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询排班")
    public Result<WorkAssignment> getById(@PathVariable Long id) {
        WorkAssignment assignment = workAssignmentService.getById(id);
        if (assignment == null) {
            return Result.error("排班不存在");
        }
        return Result.success(assignment);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询排班")
    public Result<PageResult<WorkAssignment>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long workId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String assignStatus) {
        Page<WorkAssignment> page = workAssignmentService.pageQuery(current, size, workId, userId, assignStatus);
        PageResult<WorkAssignment> pageResult = new PageResult<>(
                page.getTotal(),
                page.getRecords(),
                page.getCurrent(),
                page.getSize()
        );
        return Result.success(pageResult);
    }

    @PutMapping("/{id}/status")
    @ApiOperation("更新排班状态")
    public Result<WorkAssignment> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request) {
        try {
            if (!StringUtils.hasText(request.getAssignStatus())) {
                return Result.error("排班状态不能为空");
            }
            WorkAssignment assignment = workAssignmentService.getById(id);
            if (assignment == null) {
                return Result.error("排班不存在");
            }
            assignment.setAssignStatus(request.getAssignStatus());
            assignment.setUpdateTime(LocalDateTime.now());
            workAssignmentService.updateById(assignment);
            return Result.success("更新成功", assignment);
        } catch (Exception e) {
            log.error("更新排班状态失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("取消排班")
    public Result<?> delete(@PathVariable Long id) {
        try {
            WorkAssignment assignment = workAssignmentService.getById(id);
            if (assignment == null) {
                return Result.error("排班不存在");
            }
            assignment.setAssignStatus("cancelled");
            assignment.setUpdateTime(LocalDateTime.now());
            workAssignmentService.updateById(assignment);
            return Result.success("取消成功");
        } catch (Exception e) {
            log.error("取消排班失败", e);
            return Result.error("取消失败：" + e.getMessage());
        }
    }

    public static class UpdateStatusRequest {
        private String assignStatus;

        public String getAssignStatus() {
            return assignStatus;
        }

        public void setAssignStatus(String assignStatus) {
            this.assignStatus = assignStatus;
        }
    }
}
