package com.sky.controller.admin;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@RestController
@Slf4j
@RequestMapping("/admin/dish")
@Api(tags = "后台菜品管理")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     *
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
      log.info("新增菜品：{}", dishDTO);
      dishService.saveWithFlavors(dishDTO);

      //清理缓存数据
        String key ="dish_"+dishDTO.getCategoryId();
        cleanCache(key);
      return Result.success();
    }

    /**
     * 分页查询菜品
     * @param dishqueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询菜品")
    public Result<PageResult> page(DishPageQueryDTO dishqueryDTO) {
        log.info("分页查询菜品：{}", dishqueryDTO);
        PageResult pageResult = dishService.pageQuery(dishqueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 菜品批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("菜品批量删除")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("删除菜品：{}", ids);
        dishService.deleteBatch(ids);

        //将所有菜品缓存数据清理掉,以dish_开头
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        return Result.success();

    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id查询菜品：{}", id);
        DishVO  dishVO = dishService.getByIdWithFlavors(id);
        return Result.success(dishVO);
    }

    /**
     * 菜品信息更新
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("菜品信息更新")
    public Result update(@RequestBody DishDTO dishDTO) {
        log.info("更新菜品：{}", dishDTO);
        dishService.updateWithFlavors(dishDTO);

        //将所有菜品缓存数据清理掉,以dish_开头
        cleanCache("dish_*");
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据分类获取菜品列表")
    public Result<List<Dish>> list(Long categoryId){
        log.info("根据分类获取菜品列表：{}", categoryId);
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }


    /**
     * 菜品起售停售
     *
     */
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售停售")
    public Result<String> startOrShop(@PathVariable Integer status,Long id){
        dishService.startOrStop(status,id);

        //将所有菜品缓存数据清理掉,以dish_开头
        cleanCache("dish_*");

        return Result.success();
    }

    /**
     *  清理缓存数据
     * @param pattern
     */
    private void cleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);

    }

}
