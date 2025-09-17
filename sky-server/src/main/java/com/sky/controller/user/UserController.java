package com.sky.controller.user;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderReportVO;
import com.sky.vo.OrderVO;
import com.sky.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/user")
@Api(tags = "C端用户相关接口")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private OrderService orderService;

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    @PostMapping("/user/login")
    @ApiOperation("微信登录接口")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("微信登录接口，参数：{}", userLoginDTO.getCode());

        //微信登录
        User user = userService.wxLogin(userLoginDTO);

        //为微信用户生成token
        Map<String, Object> claims=new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id (user.getId())
                .openid (user.getOpenid())
                .token(token)
                .build();

        //返回token
        return Result.success(userLoginVO);
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/order/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    /**
     * 历史订单查询
     * @return
     * @Param
     */
    @GetMapping("order/historyOrders")
    @ApiOperation("历史订单查询")
    public Result<PageResult> page(int page, int pageSize, Integer statues) {

        log.info("历史订单查询，参数：page={},pageSize={},statues={}",page,pageSize,statues);
       PageResult pageResult= orderService.pageQuery4User(page, pageSize, statues);

       return Result.success(pageResult);


    }

}
