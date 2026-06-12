package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("work_assignment")
public class WorkAssignment extends BaseEntity {

    private Long workId;

    private String workName;

    private Long userId;

    private String userName;

    private String assignStatus;

    private Integer certCheckPassed;

    private String certCheckDetail;

    private String certWarnings;

    private String remark;
}
