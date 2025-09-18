package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin/order")
@Api(tags = "后台-订单管理")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 条件查询订单
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/conditionSearch")
    @ApiOperation(value = "条件查询订单")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("条件查询订单");

        PageResult pageResult = orderService.condictionSearch(ordersPageQueryDTO);

        return Result.success();
    }

    /**
     * 订单状态统计
     * @return
     */
    @ApiOperation(value = "订单状态统计")
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statistics(){
        log.info("订单状态统计");
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }

    /**
     * 查看订单详细信息
     * @param id
     * @return
     */
    @ApiOperation(value = "查看订单详细信息")
    @GetMapping("/detail/{id}")
    public Result<OrderVO> details(@PathVariable Long id){
        log.info("查看订单详细信息");
        OrderVO orderVO =orderService.details(id);
        return Result.success(orderVO);
    }

    /**
     *确认订单
     */
    @PutMapping("/confirm")
    @ApiOperation(value = "确认订单")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        log.info("确认订单");
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }


    /**
     * 拒绝订单
     * @param ordersRejectionDTO
     * @return
     * @throws Exception
     */
    @PutMapping("/rejection")
    @ApiOperation(value = "拒绝订单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO)throws Exception{
        log.info("拒绝订单");
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO )throws   Exception{
        log.info("取消订单");
        orderService.cancel(ordersCancelDTO);
        return Result.success();
    }

    /**
     * 订单配送
     * @param id
     * @return Result
     */
    @PutMapping("/delivery/{id}")
    @ApiOperation(value = "订单配送")
    public Result delivery(@PathVariable Long id) throws Exception {
        log.info("订单配送,{}", id);
        orderService.delivery(id);
        return Result.success();
    }

    /**
     * 完成订单
     */
    @PutMapping("/compelete/{id}")
    @ApiOperation(value = "完成订单")
    public Result compelete(@PathVariable Long id) throws Exception {
        log.info("完成订单,{}", id);
        orderService.complete(id);
        return Result.success();
    }


}
