package com.sky.controller.user;


import com.sky.dto.OrdersSubmitDTO;
import com.sky.mapper.OrderDetailMapper;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Api("用户端订单相关接口")
@Slf4j
public class OrderController {

    @Autowired
    OrderService orderService;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    /**
     * 用户下单
     * @param orderSybmitDTO
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO orderSybmitDTO) {

        log.info("用户下单：{}", orderSybmitDTO);

       OrderSubmitVO orderSubmitVO = orderService.submitOrder(orderSybmitDTO);
        return Result.success(orderSubmitVO);
    }


    /**
     * 用户订单详情
     * @param id
     * @return
     */
    @ApiOperation("用户订单详情")
    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> details(@PathVariable("id") Long id) {
        log.info("用户订单详情：{}", id);
        orderService.details(id);
        return Result.success(null);
    }

    /**
     * 用户取消订单
     * @param id
     * @return
     * @throws Exception
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("用户取消订单")
    public Result cancel(@PathVariable("id") Long id)throws Exception{
        log.info("用户取消订单：{}", id);
        orderService.userCancelById(id);
        return Result.success(null);
    }

    /**
     * 再来一单
     * @param id
     */
    @ApiOperation("再来一单")
    @PostMapping("/repetition/{id}")
    public Result repetition(@PathVariable("id") Long id){
        log.info("再来一单：{}", id);
        orderService.repetition(id);
        return Result.success();
    }


}
