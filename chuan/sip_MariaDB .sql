CREATE DATABASE SIP;
USE SIP;
CREATE TABLE HotelLogin (
    loginID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    loginName VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    headshot LONGBLOB,
    googleID VARCHAR(30),
    lineID VARCHAR(40)
);

CREATE TABLE Hotel (
    hotelID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    loginID INT NOT NULL,
    hotelName VARCHAR(100) NOT NULL,
    hotelType VARCHAR(50) NOT NULL,
    hotelStar VARCHAR(10) NOT NULL,
    tel VARCHAR(20) NOT NULL,
    country VARCHAR(50) NOT NULL,
    city VARCHAR(50) NOT NULL,
    region VARCHAR(50) NOT NULL,
    street VARCHAR(100),
    postalCode VARCHAR(20) NOT NULL,
    hotelIntroduction TEXT,
    state BIT NOT NULL,
    FOREIGN KEY (loginID) REFERENCES HotelLogin(loginID)
);

CREATE TABLE HotelDetail (
    hotelID INT NOT NULL PRIMARY KEY,
    mapURL VARCHAR(100) NOT NULL,
    guiNumber VARCHAR(40) NOT NULL,
    businessName VARCHAR(100) NOT NULL,
    openYear INT NOT NULL,
    cleaningService BIT,
    expressCheckin BIT,
    counter24hr BIT,
    freeWiFi BIT,
    roomCard BIT,
    noSmoking BIT,
    petFriendly BIT,
    petDeposit INT,
    petCleaningFee INT,
    reservationNotice TEXT,
    FOREIGN KEY (hotelID) REFERENCES Hotel(hotelID)
);

CREATE TABLE Room (
    hotelID INT NOT NULL,
    productID VARCHAR(36) NOT NULL,
    productName VARCHAR(100) NOT NULL,
    productType VARCHAR(30) NOT NULL,  /* 房間類型:單?雙?套房? */
    price INT NOT NULL,
    state BIT NOT NULL,  /* 狀態 */
    capacity INT NOT NULL,  /* 房間最多容納人數 */
    productDescription TEXT,
    score INT,
    checkInTime CHAR(5) NOT NULL,
    checkOutTime CHAR(5) NOT NULL,
    childExtraBed BIT,
    childrenPrice INT,  /* childExtraBed 的價格 */
    age INT,  /* childExtraBed 的使用限制年齡 */
    privateBathroom BIT NOT NULL,
    showerRoom BIT NOT NULL,
    createDate DATETIME DEFAULT CURRENT_TIMESTAMP,  /*因為uuid排序問題 改用創建日期做排序，預設為當前時間 */
    PRIMARY KEY (productID),
    UNIQUE (hotelID, productID),
    FOREIGN KEY (hotelID) REFERENCES Hotel(hotelID)
);

CREATE TABLE RoomQuantityPriceByDate (
    roomQuantityID VARCHAR(36) PRIMARY KEY,
    productID VARCHAR(36) NOT NULL,
    quantityByDate INT,
    price INT NOT NULL,
    date DATE,
    discountPrice INT,
    FOREIGN KEY (productID) REFERENCES Room(productID)
);

CREATE TABLE RoomPicture (
    imgID INT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    productID VARCHAR(36) NOT NULL,
    imageData LONGBLOB NOT NULL,
    FOREIGN KEY (productID) REFERENCES Room(productID)
);

CREATE TABLE HotelLoginPicture (
    HotelLoginImgID INT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    loginID INT NOT NULL,
    imageData LONGBLOB NOT NULL,
    FOREIGN KEY (loginID) REFERENCES HotelLogin(loginID)
);

CREATE TABLE DefaultPicture (
    defaultPictureID INT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    PictureName VARCHAR(100) NOT NULL,
    imageData LONGBLOB NOT NULL
);

CREATE TABLE Customer (
    loginID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    CustomerName VARCHAR(100) NOT NULL,
    sex VARCHAR(10) NOT NULL,
    birthday DATE NOT NULL,
    phone VARCHAR(20) NOT NULL,
    headshot LONGBLOB,
    country VARCHAR(50) NOT NULL,
    city VARCHAR(50) NOT NULL,
    region VARCHAR(50) NOT NULL,
    street VARCHAR(100),
    postalCode VARCHAR(20) NOT NULL,
    googleID VARCHAR(30),
    LineID VARCHAR(40)
);

CREATE TABLE OrderTable (
    orderID VARCHAR(36) NOT NULL PRIMARY KEY,
    loginID INT NOT NULL,
    hotelID INT NOT NULL,
    email VARCHAR(100) NOT NULL,
    lastName VARCHAR(40) NOT NULL,
    firstName VARCHAR(40) NOT NULL,
    tel VARCHAR(20) NOT NULL,
    orderStatus VARCHAR(20) NOT NULL DEFAULT 'valid',
    orderTime VARCHAR(40) NOT NULL,
    updateTime VARCHAR(40) NOT NULL,
    orderPrice INT NOT NULL,
    payment VARCHAR(40) NOT NULL,
    numPeople INT NOT NULL,
    checkInDate DATE NOT NULL,
    checkOutDate DATE NOT NULL,
    evaluation VARCHAR(40),
    comment VARCHAR(40),
    FOREIGN KEY (loginID) REFERENCES Customer(loginID),
    FOREIGN KEY (hotelID) REFERENCES Hotel(hotelID)
);

CREATE TABLE OrderItem (
    orderItemID VARCHAR(36) NOT NULL PRIMARY KEY,
    orderID VARCHAR(36) NOT NULL,
    productID VARCHAR(36) NOT NULL,
    quantity INT NOT NULL,
    orderItemPrice INT NOT NULL,
    FOREIGN KEY (orderID) REFERENCES OrderTable(orderID),
    FOREIGN KEY (productID) REFERENCES Room(productID)
);



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

CREATE TABLE TempOrderItem (
    orderItemID VARCHAR(36) NOT NULL PRIMARY KEY,  -- 訂單項目 ID
    orderID VARCHAR(36) NOT NULL,                   -- 訂單 ID
    productID VARCHAR(36) NOT NULL,                 -- 產品 ID (房型 ID)
    quantity INT NOT NULL,                          -- 數量
    orderItemPrice INT NOT NULL,                    -- 訂單項目價格
    FOREIGN KEY (orderID) REFERENCES TempOrderTable(orderID) ON DELETE CASCADE,
    FOREIGN KEY (productID) REFERENCES Room(productID)
);


-- 步驟 1：刪除現有的外鍵約束（假設外鍵約束名稱為 temporderitem_ibfk_1）
ALTER TABLE TempOrderItem
DROP FOREIGN KEY temporderitem_ibfk_1;

-- 步驟 2：重新添加外鍵約束並設置 ON DELETE CASCADE
ALTER TABLE TempOrderItem
ADD CONSTRAINT temporderitem_ibfk_1
FOREIGN KEY (orderID) REFERENCES TempOrderTable(orderID)
ON DELETE CASCADE;