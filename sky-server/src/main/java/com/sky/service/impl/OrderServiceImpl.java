package com.sky.service.impl;

import ch.qos.logback.core.BasicStatusManager;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.xmlbeans.impl.soap.Detail;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.builder;

@Service
@Slf4j
@Api(tags = "订单服务")
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        //处理业务异常(地址簿为空,购物车数据为空)
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }


        //查询当前用户的购物车数据,是否为空
        Long userId = addressBook.getUserId();
        ShoppingCart shoppingCart=new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartsList = shoppingCartMapper.list(shoppingCart);

        if(shoppingCartsList == null || shoppingCartsList.size() == 0){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //向订单表插入1条订单数据
        Orders orders =new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO ,orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);

        orderMapper.insert(orders);

        List<OrderDetail> orderDetailList=new ArrayList<>();

        //向订单详情表插入n条数据
       for(ShoppingCart cart:shoppingCartsList){
           OrderDetail orderDetail =new OrderDetail();//订单明细
           BeanUtils.copyProperties(cart,orderDetail);
           orderDetail.setOrderId(orders.getId());//设置当前订单明细关联的订单id
           orderDetailList.add(orderDetail);
       }
       orderDetailMapper.insertBatch(orderDetailList);
        //清空当前购物车数据
       shoppingCartMapper.deleteByUserId(userId);

        //封装当前用户的购物车数据
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();

        //返回订单信息
      return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 分页查询历史记录
     * @param pageNum
     * @param pageSize
     * @param statues
     * @return
     */
    @Override
    public PageResult pageQuery4User(int pageNum, int pageSize, Integer statues) {

        log.info("分页查询page={},pageSize={}",pageNum,pageSize);
        //封装查询参数
        OrdersPageQueryDTO ordersPageQueryDTO=new OrdersPageQueryDTO();
        ordersPageQueryDTO.setPage(pageNum);
        ordersPageQueryDTO.setPageSize(pageSize);
        ordersPageQueryDTO.setStatus(statues);

        PageHelper.startPage(pageNum, pageSize);
        Page<Orders> page=orderMapper.pageQuery(ordersPageQueryDTO);
        List<Orders> list=new ArrayList<>();

        if( page!= null ||page.getTotal()>0){
            for(Orders orders:page){
            Long userId=orders.getUserId();

            List<OrderDetail> orderDetailList=orderDetailMapper.getByOrderId(orders.getId());

            OrderVO orderVO=new OrderVO();
            BeanUtils.copyProperties(orders,orderVO);
            orderVO.setOrderDetailList(orderDetailList);

            list.add(orderVO);
            }
        }
        PageResult pageResult=new PageResult();
        pageResult.setTotal(page.getTotal());
        pageResult.setRecords(list);
        return pageResult;

    }

    /**
     * 根据订单id查询订单列表
     * @param id
     * @return
     */
    @Override
    public OrderVO details(Long id) {
        //根据订单id查询订单
        Orders orders=orderMapper.getById(id);
        //查询该订单详情
        List<OrderDetail> orderDetailList=orderDetailMapper.getByOrderId(orders.getId());

        OrderVO orderVO=new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;
    }


    /**
     * 用户取消订单
     * @param id
     */
    @Override
    public void userCancelById(Long id)throws Exception {

        Orders ordersDB=orderMapper.getById(id);
        //查看订单是否存在
        if(ordersDB==null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //查看订单是否处于待付款状态
        if(ordersDB.getStatus()>2) {
           throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //获取一个订单对象,先用于比较状态,再用于更新订单
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());

        // 订单处于待接单状态下取消，需要进行退款
        if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            //调用微信支付退款接口
            weChatPayUtil.refund(
                    ordersDB.getNumber(), //商户订单号
                    ordersDB.getNumber(), //商户退款单号
                    new BigDecimal(0.01),//退款金额，单位 元
                    new BigDecimal(0.01));//原订单金额

            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }
        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 再来一单
     * @param id 订单id
     */
    @Override
    public void repetition(Long id) {

        //获取用户id
        Long userId=BaseContext.getCurrentId();

        //根据订单id查询订单
        Orders orders=orderMapper.getById(id);
        //获取订单详情
       List<OrderDetail>orderDetailList=orderDetailMapper.getByOrderId(id);

       List<ShoppingCart> shoppingCartList=new ArrayList<>();
       for(OrderDetail orderDetail:orderDetailList){
           ShoppingCart shoppingCart=new ShoppingCart();
           BeanUtils.copyProperties(orderDetail,shoppingCart);
           shoppingCart.setUserId(userId);
           shoppingCart.setCreateTime(LocalDateTime.now());
           shoppingCartList.add(shoppingCart);
       }

       ShoppingCartMapper.insertBatch(shoppingCartList);

    }

    /**
     * 条件查询
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult condictionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {

        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<Orders> page=orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> pageList=getOrderVOList(page);

        return new PageResult(page.getTotal(),pageList);
    }

    private List<OrderVO> getOrderVOList(Page<Orders> page) {

        List<OrderVO> orderVOList=new ArrayList<>();

        List<Orders>ordersList=page.getResult();

      if(!CollectionUtils.isEmpty(ordersList)){
          for(Orders orders:ordersList){
              OrderVO orderVO=new OrderVO();
              BeanUtils.copyProperties(orders,orderVO);
              String orderDishesStr=getOrderDishesStr(orders);
              orderVO.setOrderDishes(orderDishesStr);
              orderVOList.add(orderVO);
          }
      }
      return orderVOList;
    }

    private String getOrderDishesStr(Orders orders) {
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        List<String>orderDsihList=orderDetailList.stream().map(a->{
            String dishName=a.getName()+"*"+a.getNumber()+";";
            return dishName;
        }).collect(Collectors.toList());

        return String.join(",",orderDsihList);
    }

    /**
     *订单状态统计
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        Integer toBeConfirmed=orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed=orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress=orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);
        OrderStatisticsVO orderStatisticsVO=new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    /**
     * 确认订单
     * @param ordersConfirmDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders=Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(ordersConfirmDTO.getStatus())
                .build();
        orderMapper.update(orders);
    }

    /**
     * 订单拒绝
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {

        //DB是为了可读性
        Orders ordersDB=orderMapper.getById(ordersRejectionDTO.getId());

        //判断是否符合拒单条件
        if(ordersDB==null||!ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
           throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //支付状态
        Integer payStatus = ordersDB.getPayStatus();
        if (payStatus == Orders.PAID) {
            //用户已支付，需要退款
            String refund = weChatPayUtil.refund(
                    ordersDB.getNumber(),
                    ordersDB.getNumber(),
                    new BigDecimal(0.01),
                    new BigDecimal(0.01));
            log.info("申请退款：{}", refund);
        }

        Orders orders=new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.CANCELLED);

        orders.setCancelReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO)throws Exception {

        //DB是为了可读性
        Orders ordersDB=orderMapper.getById(ordersCancelDTO.getId());

        if(ordersDB.getStatus().equals(Orders.PAID)){
            //用户已支付，需要退款
            String refund = weChatPayUtil.refund(
                    ordersDB.getNumber(),
                    ordersDB.getNumber(),
                    new BigDecimal(0.01),
                    new BigDecimal(0.01));
            log.info("申请退款：{}", refund);
        }
        //更新订单状态、取消原因、取消时间
        Orders orders=new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 订单配送
     * @param id
     */
    @Override
    public void delivery(Long id) {

        Orders ordersDB=orderMapper.getById(id);

        if(ordersDB==null||!ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders=new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);

    }

    /**
     * 订单完成
     * @param id
     */
    @Override
    public void complete(Long id) {
        Orders ordersDB=orderMapper.getById(id);
        if(ordersDB==null||!ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders=new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(orders);

    }

}
