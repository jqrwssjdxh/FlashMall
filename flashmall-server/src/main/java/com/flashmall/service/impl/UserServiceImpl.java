package com.flashmall.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flashmall.constant.ResultCode;
import com.flashmall.dto.RegisterDTO;
import com.flashmall.entity.User;
import com.flashmall.exception.BusinessException;
import com.flashmall.mapper.UserMapper;
import com.flashmall.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;   //构造器注入  作用同于以前的字段注入就是@ATUOwired
    }

    @Override
    public User getUserById(Long id) {

        return userMapper.selectById(id);

    }
    public User register(RegisterDTO registerDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, registerDTO.getUsername());

        User existUser = userMapper.selectOne(wrapper);

        if (existUser != null) {
            throw new BusinessException(ResultCode.USER_ALREADY_EXISTS);
        }

        User user = new User();

        user.setUsername(registerDTO.getUsername());
        user.setNickname(registerDTO.getUsername());
        user.setStatus(1);

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        user.setPassword(
                encoder.encode(registerDTO.getPassword())
        );
        userMapper.insert(user);

        return user;
    }
}




