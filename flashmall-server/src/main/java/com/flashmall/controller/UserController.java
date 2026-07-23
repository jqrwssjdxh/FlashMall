package com.flashmall.controller;
import com.flashmall.dto.LoginDTO;
import com.flashmall.dto.RegisterDTO;

import com.flashmall.common.Result;
import com.flashmall.entity.User;
import com.flashmall.service.UserService;
import com.flashmall.vo.LoginVO;
import com.flashmall.vo.UserVO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {


    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/{id}")
    public Result<UserVO> getUser(@PathVariable Long id){

        User user = userService.getUserById(id);

        UserVO vo = new UserVO();

        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        return Result.success(vo);
    }
    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody RegisterDTO registerDTO) {

        User user = userService.register(registerDTO);

        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());

        return Result.success(vo);
    }
    @PostMapping("/login")
    public Result<LoginVO> login(
            @RequestBody LoginDTO loginDTO
    ){

        return Result.success(
                userService.login(loginDTO)
        );

    }
}



