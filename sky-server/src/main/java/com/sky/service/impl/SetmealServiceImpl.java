package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.IterableUtils.forEach;


@Service
@Slf4j
@Api(tags = "套餐管理")
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    @ApiOperation("新增套餐")
    @Override
    public Result saveWithDish(SetmealDTO setmealDTO) {

        //属性拷贝
         Setmeal setmeal = new Setmeal();
         BeanUtils.copyProperties(setmealDTO, setmeal);

        //插入套餐信息
         setmealMapper.saveWithDish(setmeal);

         //获取新增套餐的id
         Long setmealId = setmeal.getId();
        //把菜品信息插入套餐菜品表
         List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
         for (SetmealDish setmealDish : setmealDishes) {
             setmealDish.setSetmealId(setmealId);
         }

         setmealDishMapper.insertBatch(setmealDishes);
        return null;
    }

    /**
     * 分页查询套餐
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {

        //分页查询套餐信息
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page= setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Override
    public void deleteBatch(List<Long> ids) {

        ids.forEach(id -> {
            Setmeal setmeal = setmealMapper.getById(id);
            if(setmeal.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });

        ids.forEach(setmealId -> {
            //删除套餐信息
            setmealMapper.deleteById(setmealId);
            //删除套餐菜品信息
            setmealDishMapper.deleteBySetmealId(setmealId);
        });
    }

    /**
     * 根据id查询套餐信息以及菜品信息
     * @param id
     * @return
     */
    @Override
    public SetmealVO getByIdWithDish(Long id) {

        //获取要查询的套餐信息
        Setmeal setmeal = setmealMapper.getById(id);
        //获取要查询的套餐菜品信息
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
        //封装套餐信息以及菜品信息
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    @Override
    public void update(SetmealDTO setmealDTO) {

        //属性拷贝
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        //修改套餐信息
        setmealMapper.update(setmeal);

        //要操作的套餐id
        Long setmealId = setmeal.getId();
        //删除原有套餐菜品信息
        setmealDishMapper.deleteBySetmealId(setmealId);
        //获取要更新的套餐菜品信息
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        //插入新套餐菜品信息
        forEach(setmealDishes, setmealDish -> {
          setmealDish.setSetmealId(setmealId);
        });
        setmealDishMapper.insertBatch(setmealDishes);
    }

    @Override
    public void startOrStop(Integer status, Long id) {

        if(status == StatusConstant.ENABLE){
            List<Dish> dishList= dishMapper.getBySetmealId(id);
            if(dishList!=null&&dishList.size()>0){
                dishList.forEach(dish -> {
                    if(dish.getStatus() == StatusConstant.DISABLE){
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }
        Setmeal setmeal =Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
