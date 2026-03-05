package com.juntong.multimodalantiscamassistant.module.report.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.juntong.multimodalantiscamassistant.module.alert.entity.Alert;
import com.juntong.multimodalantiscamassistant.module.alert.mapper.AlertMapper;
import com.juntong.multimodalantiscamassistant.module.report.dto.GenerateReportDTO;
import com.juntong.multimodalantiscamassistant.module.report.entity.Report;
import com.juntong.multimodalantiscamassistant.module.report.mapper.ReportMapper;
import com.juntong.multimodalantiscamassistant.module.report.service.ReportService;
import com.juntong.multimodalantiscamassistant.module.report.vo.ReportVO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

        private final AlertMapper alertMapper;
        private final ObjectMapper objectMapper;

        @Override
        @SneakyThrows
        public ReportVO generate(Long userId, GenerateReportDTO dto) {
                LocalDate start = dto.getStartDate();
                LocalDate end = dto.getEndDate();

                // 查询该时间段内的预警记录
                List<Alert> alerts = new com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper<>(
                                alertMapper)
                                .eq(Alert::getUserId, userId)
                                .ge(Alert::getCreatedAt, LocalDateTime.of(start, LocalTime.MIN))
                                .le(Alert::getCreatedAt, LocalDateTime.of(end, LocalTime.MAX))
                                .list();

                // 统计各等级数量
                int high = (int) alerts.stream().filter(a -> a.getSeverity() == 3).count();
                int mid = (int) alerts.stream().filter(a -> a.getSeverity() == 2).count();
                int low = (int) alerts.stream().filter(a -> a.getSeverity() == 1).count();

                // 统计诈骗类型分布
                Map<String, Integer> scamTypeStats = new HashMap<>();
                alerts.forEach(a -> {
                        String type = a.getScamType() != null ? a.getScamType() : "UNKNOWN";
                        scamTypeStats.merge(type, 1, (oldVal, newVal) -> oldVal + newVal);
                });

                // 生成总结文字
                String summary = buildSummary(start, end, alerts.size(), high, scamTypeStats);

                // 把统计数据序列化为 JSON 存库
                Map<String, Object> contentMap = new HashMap<>();
                contentMap.put("totalAlerts", alerts.size());
                contentMap.put("highSeverityCount", high);
                contentMap.put("midSeverityCount", mid);
                contentMap.put("lowSeverityCount", low);
                contentMap.put("scamTypeStats", scamTypeStats);
                contentMap.put("summary", summary);
                String contentJson = objectMapper.writeValueAsString(contentMap);

                Report report = Report.builder()
                                .userId(userId)
                                .startDate(start)
                                .endDate(end)
                                .content(contentJson)
                                .createdAt(LocalDateTime.now())
                                .build();
                save(report);

                // 组装返回 VO
                ReportVO vo = new ReportVO();
                vo.setId(report.getId());
                vo.setStartDate(start);
                vo.setEndDate(end);
                vo.setCreatedAt(report.getCreatedAt());
                vo.setTotalAlerts(alerts.size());
                vo.setHighSeverityCount(high);
                vo.setMidSeverityCount(mid);
                vo.setLowSeverityCount(low);
                vo.setScamTypeStats(scamTypeStats);
                vo.setSummary(summary);
                return vo;
        }

        @Override
        @SneakyThrows
        public List<ReportVO> listReports(Long userId) {
                return lambdaQuery()
                                .eq(Report::getUserId, userId)
                                .orderByDesc(Report::getCreatedAt)
                                .list()
                                .stream()
                                .map(r -> {
                                        ReportVO vo = new ReportVO();
                                        vo.setId(r.getId());
                                        vo.setStartDate(r.getStartDate());
                                        vo.setEndDate(r.getEndDate());
                                        vo.setCreatedAt(r.getCreatedAt());
                                        // 列表接口不返回完整 content，只显示摘要
                                        try {
                                                Map<String, Object> map = objectMapper.readValue(r.getContent(),
                                                                new TypeReference<Map<String, Object>>() {
                                                                });
                                                vo.setTotalAlerts((Integer) map.get("totalAlerts"));
                                                vo.setSummary((String) map.get("summary"));
                                        } catch (Exception ignored) {
                                        }
                                        return vo;
                                }).toList();
        }

        /** 根据统计数据生成简短的中文总结 */
        private String buildSummary(LocalDate start, LocalDate end, int total, int high,
                        Map<String, Integer> scamTypeStats) {
                if (total == 0) {
                        return String.format("%s 至 %s 期间未检测到任何诈骗风险，请继续保持安全习惯。", start, end);
                }
                String topType = scamTypeStats.entrySet().stream()
                                .max(Map.Entry.comparingByValue())
                                .map(Map.Entry::getKey)
                                .orElse("未知类型");
                return String.format(
                                "%s 至 %s 期间共触发 %d 次预警，其中高风险 %d 次。" +
                                                "最主要的诈骗类型为【%s】，请重点防范。",
                                start, end, total, high, topType);
        }
}
