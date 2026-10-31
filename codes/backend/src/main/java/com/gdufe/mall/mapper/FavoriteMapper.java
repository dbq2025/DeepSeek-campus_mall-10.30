package com.gdufe.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gdufe.mall.entity.Favorite;
import com.gdufe.mall.vo.FavoriteItemVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface FavoriteMapper extends BaseMapper<Favorite> {

    /**
     * 收藏分页：联表带出商品字段，并在 SQL 里过滤逻辑删除的商品（契约边界 #29），
     * 避免「先分页后过滤」导致每页条数不齐。
     */
    @Select("SELECT f.id AS favorite_id, f.create_time AS favorite_time, "
            + "p.id, p.name, p.subtitle, p.main_image, p.price, p.stock, p.sales, "
            + "p.category_id, c.name AS category_name "
            + "FROM favorite f "
            + "JOIN product p ON p.id = f.product_id AND p.deleted = 0 "
            + "LEFT JOIN category c ON c.id = p.category_id "
            + "WHERE f.user_id = #{userId} "
            + "ORDER BY f.create_time DESC, f.id DESC")
    IPage<FavoriteItemVO> selectFavoritePage(IPage<?> page, @Param("userId") Long userId);
}
