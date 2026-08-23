package com.example.user.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.user.infrastructure.entity.PointsLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 积分流水 Mapper
 */
@Mapper
public interface PointsLogMapper extends BaseMapper<PointsLogDO> {
}
