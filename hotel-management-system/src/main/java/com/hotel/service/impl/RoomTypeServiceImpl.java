package com.hotel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hotel.entity.Room;
import com.hotel.entity.RoomType;
import com.hotel.mapper.RoomMapper;
import com.hotel.mapper.RoomTypeMapper;
import com.hotel.service.RoomTypeService;
import com.hotel.util.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 客房类型服务实现类
 * 处理客房类型增删改查、价格/最大人数维护
 */
@Service
public class RoomTypeServiceImpl extends ServiceImpl<RoomTypeMapper, RoomType> implements RoomTypeService {

    @Autowired
    private RoomTypeMapper roomTypeMapper;

    @Autowired
    private RoomMapper roomMapper;

    /**
     * 新增客房类型
     * 校验：类型名称唯一、基础价格>0、最大入住人数>=1
     */
    @Override
    public void createRoomType(RoomType roomType) {
        // 校验类型名称唯一性
        LambdaQueryWrapper<RoomType> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomType::getTypeName, roomType.getTypeName());
        if (roomTypeMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("客房类型名称已存在");
        }

        // 校验基础价格
        if (roomType.getBasePrice() == null || roomType.getBasePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("基础价格必须大于0");
        }

        // 校验最大入住人数
        if (roomType.getCapacity() == null || roomType.getCapacity() < 1) {
            throw new BusinessException("最大入住人数至少为1");
        }

        roomTypeMapper.insert(roomType);
    }

    /**
     * 更新客房类型
     */
    @Override
    public void updateRoomType(RoomType roomType) {
        RoomType existing = roomTypeMapper.selectById(roomType.getTypeId());
        if (existing == null) {
            throw new BusinessException("客房类型不存在");
        }

        // 如果修改了类型名称，检查唯一性
        if (roomType.getTypeName() != null && !roomType.getTypeName().equals(existing.getTypeName())) {
            LambdaQueryWrapper<RoomType> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RoomType::getTypeName, roomType.getTypeName());
            if (roomTypeMapper.selectCount(wrapper) > 0) {
                throw new BusinessException("客房类型名称已存在");
            }
        }

        if (roomType.getBasePrice() != null && roomType.getBasePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("基础价格必须大于0");
        }
        if (roomType.getCapacity() != null && roomType.getCapacity() < 1) {
            throw new BusinessException("最大入住人数至少为1");
        }

        roomTypeMapper.updateById(roomType);
    }

    /**
     * 删除客房类型
     * 检查是否有关联房间，有则拒绝删除
     */
    @Override
    public void deleteRoomType(Integer typeId) {
        LambdaQueryWrapper<Room> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Room::getTypeId, typeId);
        if (roomMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("该房型下仍有客房，无法删除");
        }
        roomTypeMapper.deleteById(typeId);
    }
}
