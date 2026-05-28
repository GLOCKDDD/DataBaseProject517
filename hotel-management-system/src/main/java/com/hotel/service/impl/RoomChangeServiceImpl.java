package com.hotel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hotel.entity.Checkin;
import com.hotel.entity.Room;
import com.hotel.entity.RoomChange;
import com.hotel.entity.dto.RoomChangeDTO;
import com.hotel.mapper.CheckinMapper;
import com.hotel.mapper.RoomChangeMapper;
import com.hotel.mapper.RoomMapper;
import com.hotel.service.RoomChangeService;
import com.hotel.util.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 换房服务实现类
 * 处理换房业务逻辑，包括校验、状态更新和记录生成
 */
@Service
public class RoomChangeServiceImpl extends ServiceImpl<RoomChangeMapper, RoomChange> implements RoomChangeService {

    @Autowired
    private CheckinMapper checkinMapper;

    @Autowired
    private RoomMapper roomMapper;

    /**
     * 办理换房
     * 流程：校验入住记录 -> 校验新旧房间状态 -> 更新房间状态 -> 更新入住记录 -> 保存换房记录
     * 业务规则：
     * - 入住记录状态必须为"入住中"
     * - 原房间状态必须为"占用"
     * - 新房间状态必须为"空闲"
     * - 不能换到同一房间
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRoomChange(RoomChangeDTO dto) {
        // 1. 查询入住记录，校验状态为"入住中"
        Checkin checkin = checkinMapper.selectById(dto.getCheckinId());
        if (checkin == null) {
            throw new BusinessException("入住记录不存在");
        }
        if (!"入住中".equals(checkin.getStatus())) {
            throw new BusinessException("该入住记录当前状态不允许换房，仅'入住中'状态可换房");
        }

        Integer oldRoomId = checkin.getRoomId();

        // 2. 校验不能换到同一房间
        if (oldRoomId.equals(dto.getNewRoomId())) {
            throw new BusinessException("不能换到同一房间");
        }

        // 3. 查询原房间，校验状态为"占用"
        Room oldRoom = roomMapper.selectById(oldRoomId);
        if (oldRoom == null) {
            throw new BusinessException("原房间信息不存在");
        }
        if (!"占用".equals(oldRoom.getStatus())) {
            throw new BusinessException("原房间状态异常，当前状态：" + oldRoom.getStatus());
        }

        // 4. 查询新房间，校验状态为"空闲"
        Room newRoom = roomMapper.selectById(dto.getNewRoomId());
        if (newRoom == null) {
            throw new BusinessException("新房间信息不存在");
        }
        if (!"空闲".equals(newRoom.getStatus())) {
            throw new BusinessException("新房间当前不可用，状态：" + newRoom.getStatus());
        }

        // 5. 更新原房间状态为"清洁中"
        oldRoom.setStatus("清洁中");
        roomMapper.updateById(oldRoom);

        // 6. 更新新房间状态为"占用"
        newRoom.setStatus("占用");
        roomMapper.updateById(newRoom);

        // 7. 更新入住记录的房间编号为新房间
        checkin.setRoomId(dto.getNewRoomId());
        checkinMapper.updateById(checkin);

        // 8. 创建换房记录
        RoomChange roomChange = new RoomChange();
        roomChange.setCheckinId(dto.getCheckinId());
        roomChange.setOldRoomId(oldRoomId);
        roomChange.setNewRoomId(dto.getNewRoomId());
        roomChange.setChangeTime(LocalDateTime.now());
        roomChange.setReason(dto.getReason());
        baseMapper.insert(roomChange);
    }

    /**
     * 查询某入住记录的换房历史
     * 按换房时间倒序排列
     */
    @Override
    public List<RoomChange> getChangesByCheckin(Integer checkinId) {
        LambdaQueryWrapper<RoomChange> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomChange::getCheckinId, checkinId)
                .orderByDesc(RoomChange::getChangeTime);
        return baseMapper.selectList(wrapper);
    }
}
