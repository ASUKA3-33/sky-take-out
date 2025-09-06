package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper

public interface DishFlavorMapper {

    /**
     * 批量插入口味信息
     * @Param flavors
     */
   void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据菜品id删除口味信息
     * @Param id
     */
    @Delete("delete from dish_flavor where dish_id = #{dishId}")
    void deleteByDishId(Long dishId);

    /**
     * 根据菜品id批量删除口味信息
     * @param dishIds
     */
    void deleteByDishIds(List<Long> dishIds);

    /**
     * 根据菜品id查询口味信息
     * @Param id
     */
    @Select("select * from dish_flavor where dish_id = #{id}")
    List<DishFlavor> getByDishId(Long id);
}
