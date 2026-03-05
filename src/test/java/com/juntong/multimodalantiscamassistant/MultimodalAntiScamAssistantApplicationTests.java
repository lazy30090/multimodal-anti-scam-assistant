package com.juntong.multimodalantiscamassistant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.juntong.multimodalantiscamassistant.module.alert.mapper.AlertMapper;
import com.juntong.multimodalantiscamassistant.module.chat.mapper.ChatMessageMapper;
import com.juntong.multimodalantiscamassistant.module.detect.mapper.DetectionRecordMapper;
import com.juntong.multimodalantiscamassistant.module.guardian.mapper.GuardianMapper;
import com.juntong.multimodalantiscamassistant.module.guardian.mapper.GuardianUserBindMapper;
import com.juntong.multimodalantiscamassistant.module.guardian.mapper.GuardianWhitelistMapper;
import com.juntong.multimodalantiscamassistant.module.report.mapper.ReportMapper;
import com.juntong.multimodalantiscamassistant.module.user.mapper.UserMapper;

@SpringBootTest
@ActiveProfiles("test")
class MultimodalAntiScamAssistantApplicationTests {

    @MockitoBean
    private ObjectMapper objectMapper;

    @MockitoBean
    private AlertMapper alertMapper;
    @MockitoBean
    private ChatMessageMapper chatMessageMapper;
    @MockitoBean
    private DetectionRecordMapper detectionRecordMapper;
    @MockitoBean
    private GuardianMapper guardianMapper;
    @MockitoBean
    private GuardianUserBindMapper guardianUserBindMapper;
    @MockitoBean
    private GuardianWhitelistMapper guardianWhitelistMapper;
    @MockitoBean
    private ReportMapper reportMapper;
    @MockitoBean
    private UserMapper userMapper;

    @Test
    void contextLoads() {
    }

}
