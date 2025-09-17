package com.sky.service;


import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

import java.util.List;

public interface OrderService {


   OrderSubmitVO submitOrder(OrdersSubmitDTO orderSybmitDTO);

   /**
    * 订单支付
    * @param ordersPaymentDTO
    * @return
    */
   OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

   /**
    * 支付成功，修改订单状态
    * @param outTradeNo
    */
   void paySuccess(String outTradeNo);

   /**
    *分页查询订单列表
    * @param page
    * @param pageSize
    *@param statues
    */
   PageResult pageQuery4User(int page, int pageSize, Integer statues);

   /**
    * 根据订单id查询订单详情
    * @param id
    * @return
    */
    OrderVO details(Long id);

   /**
    * 用户根据id取消订单
    * @param id
    */
   void userCancelById(Long id)throws Exception;

   /**
    * 再来一单
    * @param id
    */
   void repetition(Long id);
}
