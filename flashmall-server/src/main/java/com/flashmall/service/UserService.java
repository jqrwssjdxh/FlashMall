package com.flashmall.service;

import com.flashmall.dto.LoginDTO;
import com.flashmall.dto.RegisterDTO;
import com.flashmall.entity.User;
import com.flashmall.vo.LoginVO;

public interface UserService {

    User getUserById(Long id);

    User register(RegisterDTO registerDTO);

    LoginVO login(LoginDTO loginDTO);
}