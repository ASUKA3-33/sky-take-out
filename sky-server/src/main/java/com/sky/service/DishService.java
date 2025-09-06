package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.vo.DishVO;
import org.springframework.stereotype.Service;

import java.util.List;


public interface DishService {
    /**
     * 根据ID查询菜品
     * @param id
     * @return
     */
    public void saveWithFlavors(DishDTO dishDTO);

    /**
     * 分页查询菜品
     * @param dishqueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishqueryDTO);

    /**
     * 菜品批量删除
     * @param ids
     * @return
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据id查询菜品和对应的口味数据
     * @param id
     * @return
     */
    DishVO getByIdWithFlavors(Long id);

    /**
     * 根据id修改菜品信息和对应的口味数据
     * @param dishDTO
     */
    void updateWithFlavors(DishDTO dishDTO);
}
