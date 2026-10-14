package com.example.chat.controller;

import com.example.chat.dto.DiaryDayVO;
import com.example.chat.dto.SaveTurnRequest;
import com.example.chat.security.UserContext;
import com.example.chat.service.DiaryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/diary")
public class DiaryController {

    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    /** 落库一轮对话（用户消息 + bot 回复）。返回 void，与 /api/auth/password 风格一致。 */
    @PostMapping("/turns")
    public void saveTurn(@Valid @RequestBody SaveTurnRequest req) {
        diaryService.saveTurn(UserContext.require(), req);
    }

    /** 按日期读某天日记；不存在时返回 200 空 body */
    @GetMapping("/days/{date}")
    public DiaryDayVO getDay(@PathVariable String date) {
        return diaryService.getDay(UserContext.require(), date);
    }

    /** 整月（或任意 ≤62 天区间）批量读取 */
    @GetMapping("/days")
    public List<DiaryDayVO> getDays(
            @RequestParam("from") String from,
            @RequestParam("to") String to) {
        return diaryService.getDaysRange(UserContext.require(), from, to);
    }
}