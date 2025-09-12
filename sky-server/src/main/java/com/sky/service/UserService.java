package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Api(tags = "用户管理")
public interface UserService {

    User wxLogin(UserLoginDTO userLoginDTO);
}
