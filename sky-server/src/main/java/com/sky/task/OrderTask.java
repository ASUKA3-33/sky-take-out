package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类,对订单处理
 */
@Component
@Slf4j
public class OrderTask {

    @Autowired
    OrderMapper orderMapper;
    /**
     * 处理超超时单
     */

    @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutOrder() {
        log.info("定时处理超时订单{}", LocalDateTime.now());

        LocalDateTime time=LocalDateTime.now().plusMinutes(-15);

        //select * from orders where status =? and order_time
       List<Orders> ordersList= orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, LocalDateTime.now().minusMinutes(10));

       if(ordersList!=null && ordersList.size()>0){
            for(Orders order:ordersList){
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("超时未支付");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            }
       }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder() {
        log.info("定时处理派送中的订单:{}", LocalDateTime.now());

        LocalDateTime time=LocalDateTime.now().plusMinutes(-60);
        List<Orders> ordersList= orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);

        if(ordersList!=null && ordersList.size()>0){
            for(Orders order:ordersList){
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            }
        }
    }
}
