package com.sky.controller.admin;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Api(tags ="套餐管理")
public class SetmealController {

    @Autowired
    SetmealService setmealService;

    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增套餐")
    @Cacheable(cacheNames = "setmealChache",key="#setmealDTO.categoryId")
     public Result setmeal(@RequestBody SetmealDTO setmealDTO) {
        log.info("新增套餐：{}", setmealDTO);
        setmealService.saveWithDish(setmealDTO);
        return Result.success();
    }

    /**
     * 分页查询套餐
     * @param setmealPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO) {
           log.info("分页查询套餐：{}", setmealPageQueryDTO);
           PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
           return Result.success(pageResult);
    }

    /**
     * 删除套餐
     * @param Ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("删除套餐")
    @CacheEvict(cacheNames = "setmealChache",allEntries = true)
    public Result deleteSetmeal(@RequestParam List<Long>ids){
        log.info("删除套餐：{}", ids);
        setmealService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 根据id查询套餐关联菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        log.info("查询套餐：{}", id);
        SetmealVO setmealVO = setmealService.getByIdWithDish(id);
        return Result.success(setmealVO);
    }

    /**
     * 修改套餐
     * @param id
     * @return
     */
    @ApiOperation("修改套餐")
    @PutMapping
    @CacheEvict(cacheNames = "setmealChache",allEntries = true)
    public Result  update(@RequestBody SetmealDTO setmealDTO) {
        log.info("修改套餐：{}", setmealDTO.getId());
        setmealService.update(setmealDTO);
        return Result.success();
    }

    /**
     * 启用或禁用套餐
     * @param status
     * @param id
     * @return
     */
    @ApiOperation("启用或禁用套餐")
    @PostMapping("/status/{status}")
    @CacheEvict(cacheNames = "setmealChache",allEntries = true)
    public Result startOrStop(@PathVariable Integer status,Long id) {
        log.info("启用或禁用套餐：{}", id);
        setmealService.startOrStop(status,id);
        return Result.success();
    }
}
