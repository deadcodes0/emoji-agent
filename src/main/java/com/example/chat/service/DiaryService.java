package com.example.chat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.chat.dto.DiaryDayVO;
import com.example.chat.dto.DiaryTurnVO;
import com.example.chat.dto.SaveTurnRequest;
import com.example.chat.entity.DiaryDay;
import com.example.chat.entity.DiaryTurn;
import com.example.chat.exception.BizException;
import com.example.chat.mapper.DiaryDayMapper;
import com.example.chat.mapper.DiaryTurnMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DiaryService {

    private final DiaryDayMapper dayMapper;
    private final DiaryTurnMapper turnMapper;

    public DiaryService(DiaryDayMapper dayMapper, DiaryTurnMapper turnMapper) {
        this.dayMapper = dayMapper;
        this.turnMapper = turnMapper;
    }

    private static final String[] WEEKDAY_CN =
            {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};

    /**
     * 落库一轮对话：
     * - diary_days 没有当天行 → 新建（turn_count = 1）
     * - 已存在             → turn_count + 1、刷新 dominant_emotion / risk_level
     * - diary_turns 每次插一行
     */
    @Transactional
    public void saveTurn(Long userId, SaveTurnRequest req) {
        LocalDate date = LocalDate.parse(req.date());

        DiaryDay day = dayMapper.selectOne(
                new LambdaQueryWrapper<DiaryDay>()
                        .eq(DiaryDay::getUserId, userId)
                        .eq(DiaryDay::getDiaryDate, date));

        if (day == null) {
            day = new DiaryDay();
            day.setUserId(userId);
            day.setDiaryDate(date);
            day.setDominantEmotion(req.emotion());
            day.setRiskLevel(req.riskLevel());
            day.setTurnCount(1);
            dayMapper.insert(day);
        } else {
            day.setTurnCount(day.getTurnCount() == null ? 1 : day.getTurnCount() + 1);
            day.setDominantEmotion(req.emotion());
            day.setRiskLevel(req.riskLevel());
            day.setUpdatedAt(LocalDateTime.now());
            dayMapper.updateById(day);
        }

        DiaryTurn turn = new DiaryTurn();
        turn.setUserId(userId);
        turn.setDiaryDayId(day.getId());
        turn.setOccurredAt(LocalDateTime.now());
        turn.setTurnTime(req.time());
        turn.setUserMessage(req.userMsg());
        turn.setBotReply(req.reply());
        turn.setEmotion(req.emotion());
        turn.setRiskLevel(req.riskLevel());
        turnMapper.insert(turn);
    }

    /** 按日期读某天的日记；不存在返回 null（前端会保留本地数据） */
    public DiaryDayVO getDay(Long userId, String dateStr) {
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            throw new BizException("日期格式错误，应为 yyyy-MM-dd");
        }

        DiaryDay day = dayMapper.selectOne(
                new LambdaQueryWrapper<DiaryDay>()
                        .eq(DiaryDay::getUserId, userId)
                        .eq(DiaryDay::getDiaryDate, date));

        if (day == null) return null;

        List<DiaryTurn> turns = turnMapper.selectList(
                new LambdaQueryWrapper<DiaryTurn>()
                        .eq(DiaryTurn::getUserId, userId)
                        .eq(DiaryTurn::getDiaryDayId, day.getId())
                        .orderByAsc(DiaryTurn::getOccurredAt)
                        .orderByAsc(DiaryTurn::getId));

        List<DiaryTurnVO> turnVOs = turns.stream()
                .map(t -> new DiaryTurnVO(
                        t.getTurnTime(),
                        t.getUserMessage(),
                        t.getBotReply(),
                        t.getEmotion(),
                        t.getRiskLevel(),
                        List.of()   // suggestions 暂不落库，返回空
                ))
                .toList();

        return new DiaryDayVO(
                date.toString(),
                WEEKDAY_CN[date.getDayOfWeek().getValue() % 7],
                day.getSummary(),
                day.getDominantEmotion(),
                day.getRiskLevel(),
                turnVOs
        );
    }

    /** 一次拉取 [from, to] 区间内所有有数据的日期 */
    public List<DiaryDayVO> getDaysRange(Long userId, String fromStr, String toStr) {
        LocalDate from, to;
        try {
            from = LocalDate.parse(fromStr);
            to = LocalDate.parse(toStr);
        } catch (DateTimeParseException e) {
            throw new BizException("日期格式错误，应为 yyyy-MM-dd");
        }
        if (from.isAfter(to)) {
            throw new BizException("from 不能晚于 to");
        }
        // 防止一次拉爆；两个月足够覆盖"上月尾 + 本月 + 下月头"的日历视图
        if (from.plusDays(62).isBefore(to)) {
            throw new BizException("查询跨度不能超过 62 天");
        }

        List<DiaryDay> days = dayMapper.selectList(
                new LambdaQueryWrapper<DiaryDay>()
                        .eq(DiaryDay::getUserId, userId)
                        .ge(DiaryDay::getDiaryDate, from)
                        .le(DiaryDay::getDiaryDate, to)
                        .orderByAsc(DiaryDay::getDiaryDate));
        if (days.isEmpty()) return List.of();

        List<Long> dayIds = days.stream().map(DiaryDay::getId).toList();

        // 一次性把区间内所有 turn 拉出来，按 dayId 分组，避免 N+1
        List<DiaryTurn> allTurns = turnMapper.selectList(
                new LambdaQueryWrapper<DiaryTurn>()
                        .eq(DiaryTurn::getUserId, userId)
                        .in(DiaryTurn::getDiaryDayId, dayIds)
                        .orderByAsc(DiaryTurn::getOccurredAt)
                        .orderByAsc(DiaryTurn::getId));

        Map<Long, List<DiaryTurn>> byDay = new HashMap<>();
        for (DiaryTurn t : allTurns) {
            byDay.computeIfAbsent(t.getDiaryDayId(), k -> new ArrayList<>()).add(t);
        }

        List<DiaryDayVO> result = new ArrayList<>(days.size());
        for (DiaryDay day : days) {
            List<DiaryTurnVO> turnVOs = byDay.getOrDefault(day.getId(), List.of())
                    .stream()
                    .map(t -> new DiaryTurnVO(
                            t.getTurnTime(),
                            t.getUserMessage(),
                            t.getBotReply(),
                            t.getEmotion(),
                            t.getRiskLevel(),
                            List.of()))
                    .toList();

            LocalDate d = day.getDiaryDate();
            result.add(new DiaryDayVO(
                    d.toString(),
                    WEEKDAY_CN[d.getDayOfWeek().getValue() % 7],
                    day.getSummary(),
                    day.getDominantEmotion(),
                    day.getRiskLevel(),
                    turnVOs));
        }
        return result;
    }
}