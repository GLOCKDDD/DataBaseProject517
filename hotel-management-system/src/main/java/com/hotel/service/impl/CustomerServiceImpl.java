package com.hotel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hotel.entity.Customer;
import com.hotel.entity.dto.CustomerDTO;
import com.hotel.mapper.CustomerMapper;
import com.hotel.service.CustomerService;
import com.hotel.util.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 客户服务实现类
 * 处理客户信息录入、查询、会员等级与积分管理
 */
@Service
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {

    @Autowired
    private CustomerMapper customerMapper;

    /**
     * 录入/更新客户信息
     * 新客户：默认会员等级"普通"，积分0
     * 已有客户（按身份证号匹配）：更新姓名、地址、电话
     */
    @Override
    public Customer saveOrUpdateCustomer(CustomerDTO dto) {
        // 校验身份证号格式
        String idNumber = dto.getIdNumber();
        if (idNumber == null || idNumber.length() != 18) {
            throw new BusinessException("身份证号必须为18位");
        }

        // 校验手机号格式
        String phone = dto.getPhone();
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("手机号格式不正确");
        }

        // 检查身份证号是否已存在
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getIdNumber, idNumber);
        Customer existing = customerMapper.selectOne(wrapper);

        if (existing != null) {
            // 已存在，更新信息
            existing.setName(dto.getName());
            existing.setAddress(dto.getAddress());
            existing.setPhone(dto.getPhone());
            existing.setUpdatedAt(LocalDateTime.now());
            customerMapper.updateById(existing);
            return existing;
        } else {
            // 新客户
            Customer customer = new Customer();
            customer.setIdNumber(dto.getIdNumber());
            customer.setName(dto.getName());
            customer.setAddress(dto.getAddress());
            customer.setPhone(dto.getPhone());
            customer.setMembershipLevel("普通");
            customer.setPoints(0);
            customer.setCreatedAt(LocalDateTime.now());
            customer.setUpdatedAt(LocalDateTime.now());
            customerMapper.insert(customer);
            return customer;
        }
    }

    /**
     * 分页查询客户（支持姓名、身份证号、电话模糊查询）
     */
    @Override
    public IPage<Customer> searchCustomers(String keyword, int pageNum, int pageSize) {
        Page<Customer> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            wrapper.and(w -> w
                    .like(Customer::getName, kw)
                    .or()
                    .like(Customer::getIdNumber, kw)
                    .or()
                    .like(Customer::getPhone, kw));
        }
        wrapper.orderByDesc(Customer::getCreatedAt);
        return customerMapper.selectPage(page, wrapper);
    }

    /**
     * 根据身份证号查询客户
     */
    @Override
    public Customer getByIdNumber(String idNumber) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getIdNumber, idNumber);
        return customerMapper.selectOne(wrapper);
    }

    /**
     * 更新会员等级和积分
     */
    @Override
    public void updateMembership(Integer customerId, String level, Integer points) {
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }
        if (level != null) {
            customer.setMembershipLevel(level);
        }
        if (points != null) {
            customer.setPoints(points);
        }
        customer.setUpdatedAt(LocalDateTime.now());
        customerMapper.updateById(customer);
    }
}
