package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController("adminShopController")
@RequestMapping("/admin/shop")
public class ShopController {

    public static final String KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 设置店铺状态
     * @Param status 店铺状态
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("设置店铺状态")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置店铺状态:{}",status==1?"上线":"下线");
        redisTemplate.opsForValue().set(KEY,status);
        return Result.success();
    }

    /**
     *查询店铺状态
     * @return
     */
     @GetMapping("/status")
    public Result<Integer> getStatus(){

         Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
         log.info("查询店铺状态:{}",status==1?"上线":"下线");
         return Result.success(status);
     }
}
