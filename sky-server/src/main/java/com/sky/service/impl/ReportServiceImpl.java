package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.OrderService;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Api(tags = "报表模块")
public class ReportServiceImpl implements ReportService {


    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;


    /**
     * 获取营业额统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {

        //当前集合用于存放范围内的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            //日期计算,计算指定日期后一天对应的日期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //当前集合用于存放每天的营业额
        List<Double> turnoverList = new ArrayList<>();

        for (LocalDate date : dateList) {
            log.info("查询日期：date:{}", date);
            //计算开始时间和结束时间
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //Select sum(amount) from orders where order_time >beginTime and order_time <endTime and status = 5
            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            //判断返回的数值是否为空,为空则赋值为0.0
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);

        }

        //组装返回对象
        return TurnoverReportVO.builder().dateList(StringUtils.join(dateList, ",")).turnoverList(StringUtils.join(turnoverList, ",")).build();
    }

    /**
     * 获取用户统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //存放每天新增用户数,select count(id) from user where create_time<? and create_time>?
        List<Integer> newUserList = new ArrayList<>();
        //存放每天总用户数,select count(id) from user where create_time<?
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            log.info("查询日期：date:{}", date);
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            //先查询总用户数
            map.put("begin", beginTime);
            Integer totalUser = userMapper.countByMap(map);
            //查询新增用户数
            map.put("end", endTime);
            Integer newUser = userMapper.countByMap(map);
        }

        //组装返回对象
        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    /**
     * 获取订单统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> valideOrderCountList = new ArrayList<>();


        for (LocalDate date : dateList) {
            log.info("查询日期：date:{}", date);
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            //查询每天订单总数 select count(id) from orders where order_time >beginTime and order_time <endTime
            Integer orderCount = getOrderCount(beginTime, endTime, null);
            //查询每天有效订单数 select count(id) from orders where order_time >beginTime and order_time <endTime and status = 5
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);


            orderCountList.add(orderCount);
            valideOrderCountList.add(validOrderCount);
        }

        //计算时间内订单总数
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        //计算时间内有效订单总数
        Integer validOrderCount = valideOrderCountList.stream().reduce(Integer::sum).get();
        //计算订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        //组装返回对象
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(valideOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();

    }

    //封装查询订单总数的方法
    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status) {
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", status);

        return orderMapper.countByMap(map);
    }

    /**
     * 获取销售排行榜前10
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {

        //获取时间段
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        //获取销售排行榜前10数据
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(begin, end);

        //用流对象转换成字符串
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        //封装返回对象
        String nameList = StringUtils.join(names, ".");

        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(numbers, ".");

        //返回封装对象
        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    /**
     * 导出运营数据报表
     *
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        //查询数据库,获取营业数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        //查询概览数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));

        //通过POI将数据写入Excel文件
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            //基于模板创建Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);

            //获取Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");
            //填充数据-时间
            sheet.getRow(1).getCell(1).setCellValue("时间:" + dateBegin + "至" + dateEnd);

            //填充数据-营业数据

            //第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());
            //获取第5行
            row= sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            for (int i = 0; i <30 ; i++) {
                LocalDate date = dateBegin.plusDays(i);
                //查询第i天的营业数据
                workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                //获取某一行
                 row = sheet.getRow(i + 7);
                 row.getCell(1).setCellValue(date.toString());
                 row.getCell(2).setCellValue(businessDataVO.getTurnover());
                 row.getCell(3).setCellValue(businessDataVO.getValidOrderCount());
                 row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
                 row.getCell(5).setCellValue(businessDataVO.getUnitPrice());
                 row.getCell(6).setCellValue(businessDataVO.getNewUsers());
            }

            //关闭流
            out.close();
            in.close();
            excel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
