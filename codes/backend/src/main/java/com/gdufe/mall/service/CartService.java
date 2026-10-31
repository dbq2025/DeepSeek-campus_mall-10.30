package com.gdufe.mall.service;

import com.gdufe.mall.dto.CartAddDTO;
import com.gdufe.mall.dto.CartUpdateDTO;
import com.gdufe.mall.vo.CartItemVO;
import com.gdufe.mall.vo.CartVO;

public interface CartService {

    CartVO list();

    CartItemVO add(CartAddDTO dto);

    CartItemVO updateQuantity(Long id, CartUpdateDTO dto);

    void remove(Long id);

    void clear();
}
