package com.gdufe.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdufe.mall.entity.Product;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 扣库存：条件更新兜住并发超卖，stock 永不为负。
     * 返回 0 表示库存不足或商品已下架/删除，调用方必须整单回滚。
     */
    @Update("UPDATE product SET stock = stock - #{qty} "
            + "WHERE id = #{id} AND stock >= #{qty} AND status = 1 AND deleted = 0")
    int deductStock(@Param("id") Long id, @Param("qty") int qty);

    /** 取消/超时回补库存（商品可能已下架，不做 status 限制，但必须存在） */
    @Update("UPDATE product SET stock = stock + #{qty} WHERE id = #{id}")
    int restoreStock(@Param("id") Long id, @Param("qty") int qty);

    /** 支付成功才累加销量（D1 决策） */
    @Update("UPDATE product SET sales = sales + #{qty} WHERE id = #{id}")
    int increaseSales(@Param("id") Long id, @Param("qty") int qty);
}
