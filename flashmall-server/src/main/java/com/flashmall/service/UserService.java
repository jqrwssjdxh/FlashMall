package com.flashmall.service;

import com.flashmall.dto.RegisterDTO;
import com.flashmall.entity.User;

public interface UserService {

    User getUserById(Long id);

    User register(RegisterDTO registerDTO);
}