package com.gdufe.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gdufe.mall.common.exception.BizException;
import com.gdufe.mall.common.result.PageResult;
import com.gdufe.mall.common.result.ResultCode;
import com.gdufe.mall.common.security.SecurityUtil;
import com.gdufe.mall.common.util.PageUtil;
import com.gdufe.mall.entity.Favorite;
import com.gdufe.mall.entity.Product;
import com.gdufe.mall.mapper.FavoriteMapper;
import com.gdufe.mall.mapper.ProductMapper;
import com.gdufe.mall.service.FavoriteService;
import com.gdufe.mall.vo.FavoriteItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;

    @Override
    public PageResult<FavoriteItemVO> page(Integer page, Integer size) {
        Long userId = SecurityUtil.currentUserId();
        IPage<FavoriteItemVO> result = favoriteMapper
                .selectFavoritePage(PageUtil.of(page, size), userId);
        return PageResult.of(result);
    }

    @Override
    public void add(Long productId) {
        Long userId = SecurityUtil.currentUserId();
        Product p = productMapper.selectById(productId);
        if (p == null) {
            throw new BizException(ResultCode.NOT_FOUND, "商品不存在");
        }
        Long exists = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));
        if (exists != null && exists > 0) {
            throw new BizException(ResultCode.DUPLICATE_OPERATION, "已经收藏过该商品");
        }
        Favorite f = new Favorite();
        f.setUserId(userId);
        f.setProductId(productId);
        favoriteMapper.insert(f);
    }

    @Override
    public void remove(Long productId) {
        Long userId = SecurityUtil.currentUserId();
        // 取消收藏幂等：没收藏过也返回成功，避免前端切换状态时反复报错
        favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));
    }

    @Override
    public boolean check(Long productId) {
        Long userId = SecurityUtil.currentUserId();
        if (productId == null) {
            return false;
        }
        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));
        return count != null && count > 0;
    }
}
