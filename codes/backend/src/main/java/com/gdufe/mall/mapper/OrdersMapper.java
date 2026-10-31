package com.gdufe.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gdufe.mall.entity.Orders;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrdersMapper extends BaseMapper<Orders> {

    // ---------- 状态迁移：全部条件更新，返回 0 由调用方回读判断幂等或 3002 ----------

    @Update("UPDATE orders SET status = 1, pay_time = NOW(), pay_amount = total_amount "
            + "WHERE id = #{id} AND user_id = #{userId} AND status = 0")
    int pay(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE orders SET status = 4, cancel_time = NOW(), cancel_reason = #{reason} "
            + "WHERE id = #{id} AND user_id = #{userId} AND status = 0")
    int cancelByUser(@Param("id") Long id, @Param("userId") Long userId, @Param("reason") String reason);

    @Update("UPDATE orders SET status = 4, cancel_time = NOW(), cancel_reason = #{reason} "
            + "WHERE id = #{id} AND status = 0")
    int cancelBySystem(@Param("id") Long id, @Param("reason") String reason);

    @Update("UPDATE orders SET status = 3, finish_time = NOW() "
            + "WHERE id = #{id} AND user_id = #{userId} AND status = 2")
    int confirmByUser(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE orders SET status = 2, ship_time = NOW(), tracking_no = #{trackingNo} "
            + "WHERE id = #{id} AND status = 1")
    int ship(@Param("id") Long id, @Param("trackingNo") String trackingNo);

    @Update("UPDATE orders SET status = 3, finish_time = NOW() WHERE id = #{id} AND status = 2")
    int confirmByAdmin(@Param("id") Long id);

    @Select("SELECT id FROM orders WHERE status = 0 AND deleted = 0 AND create_time < #{deadline} "
            + "ORDER BY create_time ASC LIMIT #{limit}")
    List<Long> selectTimeoutIds(@Param("deadline") LocalDateTime deadline, @Param("limit") int limit);

    // ---------- 统计：聚合下推到数据库，禁止捞全表到内存 ----------

    @Select("SELECT COUNT(*) FROM orders WHERE deleted = 0 AND create_time >= CURDATE() "
            + "AND create_time < DATE_ADD(CURDATE(), INTERVAL 1 DAY)")
    long countTodayOrders();

    @Select("SELECT IFNULL(SUM(pay_amount), 0) FROM orders WHERE deleted = 0 AND status IN (1,2,3) "
            + "AND create_time >= CURDATE() AND create_time < DATE_ADD(CURDATE(), INTERVAL 1 DAY)")
    BigDecimal sumTodaySales();

    @Select("SELECT COUNT(*) FROM orders WHERE deleted = 0 AND status = 1")
    long countPendingShipment();

    @Select("SELECT DATE(create_time) AS d, IFNULL(SUM(pay_amount), 0) AS amt FROM orders "
            + "WHERE deleted = 0 AND status IN (1,2,3) AND create_time >= #{from} "
            + "GROUP BY DATE(create_time)")
    List<Map<String, Object>> sumSalesByDay(@Param("from") LocalDateTime from);

    @Select("SELECT status, COUNT(*) AS cnt FROM orders WHERE deleted = 0 GROUP BY status")
    List<Map<String, Object>> countByStatus();
}
