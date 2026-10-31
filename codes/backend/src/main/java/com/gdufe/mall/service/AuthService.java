package com.gdufe.mall.service;

import com.gdufe.mall.dto.LoginDTO;
import com.gdufe.mall.dto.RegisterDTO;
import com.gdufe.mall.vo.LoginVO;
import com.gdufe.mall.vo.UserVO;

public interface AuthService {

    LoginVO login(LoginDTO dto);

    LoginVO register(RegisterDTO dto);

    LoginVO adminLogin(LoginDTO dto);

    UserVO currentUser();

    void logout();
}
