package com.flashmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flashmall.constant.ResultCode;
import com.flashmall.dto.LoginDTO;
import com.flashmall.dto.RegisterDTO;
import com.flashmall.entity.User;
import com.flashmall.exception.BusinessException;
import com.flashmall.mapper.UserMapper;
import com.flashmall.service.UserService;
import com.flashmall.utils.JwtUtil;
import com.flashmall.vo.LoginVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
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
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));

        userMapper.insert(user);
        return user;
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        return vo;
    }
}
