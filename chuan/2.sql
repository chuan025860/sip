-- 步驟 1：刪除現有的外鍵約束（假設外鍵約束名稱為 temporderitem_ibfk_1）
ALTER TABLE TempOrderItem
DROP FOREIGN KEY temporderitem_ibfk_1;

-- 步驟 2：重新添加外鍵約束並設置 ON DELETE CASCADE
ALTER TABLE TempOrderItem
ADD CONSTRAINT temporderitem_ibfk_1
FOREIGN KEY (orderID) REFERENCES TempOrderTable(orderID)
ON DELETE CASCADE;

-- 臨時訂單表格
CREATE TABLE TempOrderTable (
    orderID VARCHAR(36) NOT NULL PRIMARY KEY,  -- 訂單 ID
    loginID INT NOT NULL,                      -- 用戶 ID
    hotelID INT NOT NULL,                      -- 酒店 ID
    email VARCHAR(100) NOT NULL,               -- 電子郵件
    lastName VARCHAR(40) NOT NULL,             -- 姓
    firstName VARCHAR(40) NOT NULL,            -- 名
    tel VARCHAR(20) NOT NULL,                  -- 電話
    orderStatus VARCHAR(20) NOT NULL DEFAULT 'valid', -- 訂單狀態 (預設有效)
    orderTime VARCHAR(40) NOT NULL,            -- 訂單時間
    updateTime VARCHAR(40) NOT NULL,           -- 更新時間
    orderPrice INT NOT NULL,                   -- 訂單價格
    payment VARCHAR(40) NOT NULL,              -- 付款方式
    numPeople INT NOT NULL,                    -- 訂單人數
    checkInDate DATE NOT NULL,                 -- 入住日期
    checkOutDate DATE NOT NULL,                -- 退房日期
    evaluation VARCHAR(40),                    -- 評價
    comment VARCHAR(40),                       -- 評論
    FOREIGN KEY (loginID) REFERENCES Customer(loginID),
    FOREIGN KEY (hotelID) REFERENCES Hotel(hotelID)
);

-- 臨時訂單項目表格
CREATE TABLE TempOrderItem (
    orderItemID VARCHAR(36) NOT NULL PRIMARY KEY,  -- 訂單項目 ID
    orderID VARCHAR(36) NOT NULL,                   -- 訂單 ID
    productID VARCHAR(36) NOT NULL,                 -- 產品 ID (房型 ID)
    quantity INT NOT NULL,                          -- 數量
    orderItemPrice INT NOT NULL,                    -- 訂單項目價格
    FOREIGN KEY (orderID) REFERENCES TempOrderTable(orderID) ON DELETE CASCADE,
    FOREIGN KEY (productID) REFERENCES Room(productID)
);