package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;

import java.util.List;


public interface ShoppingCartService {

    /**
     * 添加购物车
     * @param shoppingCartDTO 购物车信息
     */
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    /**
     * 展示购物车
     * @return
     */
    List<ShoppingCart> showShoppingCart();

    /**
     * 清空购物车
     *
     */
    void cleamShoppingCart();


    /**
     * 删除购物车商品
     * @param shoppingCartDTO
     */
    void subShoppingCart(ShoppingCartDTO shoppingCartDTO);
}
