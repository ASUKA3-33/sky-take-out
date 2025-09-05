package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper

public interface DishFlavorMapper {

    /**
     * 批量插入口味信息
     * @Param flavors
     */
   void insertBath(List<DishFlavor> flavors);

}
