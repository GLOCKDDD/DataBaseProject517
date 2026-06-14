package com.hotel.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hotel.entity.Customer;
import com.hotel.entity.dto.CustomerDTO;

import java.util.List;
import java.util.Map;

/**
 * 客户信息服务接口
 */
public interface CustomerService extends IService<Customer> {

    /**
     * 录入/更新客户信息
     */
    Customer saveOrUpdateCustomer(CustomerDTO dto);

    /**
     * 分页查询客户（支持姓名、身份证号、电话模糊查询）
     */
    IPage<Customer> searchCustomers(String keyword, int pageNum, int pageSize);

    /**
     * 根据身份证号查询客户
     */
    Customer getByIdNumber(String idNumber);

    /**
     * 更新会员等级
     */
    void updateMembership(Integer customerId, String level, Integer points);

    /**
     * 查询客户历史入住记录（含房间号）
     */
    List<Map<String, Object>> getCustomerCheckins(Integer customerId);
}
