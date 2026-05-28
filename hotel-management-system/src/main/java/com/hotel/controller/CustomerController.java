package com.hotel.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hotel.entity.Customer;
import com.hotel.entity.dto.CustomerDTO;
import com.hotel.service.CustomerService;
import com.hotel.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 客户信息管理控制器
 * 包含：客户录入、查询、会员等级与积分管理
 */
@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    /**
     * 录入/更新客户信息
     * POST /api/customer/save
     */
    @PostMapping("/save")
    public Result<?> saveCustomer(@Valid @RequestBody CustomerDTO dto) {
        Customer customer = customerService.saveOrUpdateCustomer(dto);
        return Result.success("客户信息保存成功", customer);
    }

    /**
     * 分页查询客户（支持姓名、身份证号、电话模糊查询）
     * GET /api/customer/search?keyword=张三&pageNum=1&pageSize=10
     */
    @GetMapping("/search")
    public Result<?> searchCustomers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<Customer> result = customerService.searchCustomers(keyword, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * 查询单个客户详情
     * GET /api/customer/{customerId}
     */
    @GetMapping("/{customerId}")
    public Result<?> getCustomerById(@PathVariable Integer customerId) {
        Customer customer = customerService.getById(customerId);
        if (customer == null) {
            return Result.error("客户不存在");
        }
        return Result.success(customer);
    }

    /**
     * 根据身份证号查询客户
     * GET /api/customer/idnumber/{idNumber}
     */
    @GetMapping("/idnumber/{idNumber}")
    public Result<?> getCustomerByIdNumber(@PathVariable String idNumber) {
        Customer customer = customerService.getByIdNumber(idNumber);
        if (customer == null) {
            return Result.error("未找到该客户");
        }
        return Result.success(customer);
    }

    /**
     * 更新会员等级和积分
     * PUT /api/customer/membership
     */
    @PutMapping("/membership")
    public Result<?> updateMembership(
            @RequestParam Integer customerId,
            @RequestParam String level,
            @RequestParam Integer points) {
        customerService.updateMembership(customerId, level, points);
        return Result.success("会员信息更新成功", null);
    }

    /**
     * 删除客户
     * DELETE /api/customer/{customerId}
     */
    @DeleteMapping("/{customerId}")
    public Result<?> deleteCustomer(@PathVariable Integer customerId) {
        customerService.removeById(customerId);
        return Result.success("客户删除成功", null);
    }
}
