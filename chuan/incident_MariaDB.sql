CREATE DATABASE Incident
USE Incident

CREATE TABLE incident(
    incidentId INT NOT NULL AUTO_INCREMENT PRIMARY KEY, -- 事件編號，主鍵，自動遞增
    reporter VARCHAR(20) NOT NULL,                     -- 反應人，字串
    task VARCHAR(30) NOT NULL ,                  -- 工作事項，字串
    eventContent TEXT NOT NULL,                         -- 事件內容，長文字
    reportCount INT NOT NULL,                           -- 回報數，整數
    importance VARCHAR(20) NOT NULL,                    -- 重要性
    department VARCHAR(20) NOT NULL,                   -- 處理部門，字串
    handler VARCHAR(20) NOT NULL,                      -- 處理人，字串
    status VARCHAR(20) NOT NULL,                    -- 處理狀態
    lastReportTime DATETIME NOT NULL,                           -- 最後回報時間
    reportDate DATE  NOT NULL                                  -- 反應日期
);
