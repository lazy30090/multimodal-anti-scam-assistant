package com.juntong.multimodalantiscamassistant.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.juntong.multimodalantiscamassistant.module.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper，继承 BaseMapper 即可获得全套 CRUD
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
