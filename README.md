# SIP（飯店預訂系統）

**作品簡介**

* SIP 是一個基於 Spring Boot 開發的飯店預訂系統，提供了飯店註冊、客房管理、訂單處理和客戶管理等功能。
* 前端採用 Thymeleaf 作為模板引擎，結合 HTML、CSS、Bootstrap、SweetAlert、FullCalendar 進行頁面設計。
* 使用 Ajax非同步請求 提升用戶體驗。
* 系統設計上，飯店後台採用 session 驗證，用戶後台則運用 JWT（JSON Web Token）進行身份驗證。
* 使用 MSSQL 作為關聯型資料庫，並實現了多種資料表關聯，如一對多、一對一關係。

## 功能特點、說明
### 飯店管理：飯店可以註冊並登入系統，管理自己的飯店信息、房間信息和訂單。
### 客房管理：支持添加、修改和刪除房間信息，包括價格、數量、圖片等。
### 訂單處理與悲觀鎖：客戶可以搜尋飯店、預訂房間，飯店可以查看並管理訂單狀態
* **建立訂單**：在建立訂單時，系統會使用悲觀鎖來鎖定所選房間的庫存資料，確保在同一時間內只有一個交易能夠修改該房間的庫存。這樣可以避免多個使用者同時訂購導致超賣的問題。當交易成功後，房間的可用庫存會自動減少。

* **取消訂單**：在取消訂單時，系統同樣使用悲觀鎖來鎖定房間資料，確保房間庫存正確恢復。鎖定期間，其他交易無法修改該房間的庫存，直到取消訂單操作完成並釋放鎖。

* **庫存控制**：對每個房間進行實時的庫存管理。

### 客戶管理：客戶可以註冊、登入，查看訂單歷史，編輯個人信息。
### 第三方登入:Google、Line
## 技術架構
### 後端：
* 後端框架：Spring Boot
* 資料庫：MSSQL
* ORM框架：JPA (Java Persistence API)
* 資料庫關聯：
1. 一對多（One-to-Many）：飯店與房間之間的關聯
2. 一對一（One-to-One）：訂單與客戶詳細資料的關聯
3. 一對多（One-to-Many）：訂單與飯店的關聯
4. 多對一（Many-to-One）：訂單明細與房間的關聯
5. 其他多層次關聯。
* 併發控制：使用悲觀鎖進行資源保護
* Maven: 使用 pom.xml 進行版本控制和依賴管理
* JWT 身份驗證：基於 JWT（JSON Web Token）進行身份驗證，提升用戶資料安全性與跨域存取便捷性。
  

### 前端：
* Thymeleaf：模板引擎。
* HTML5 & CSS3：頁面結構和樣式。
* Bootstrap：響應式佈局和 UI 元件。
* SweetAlert：美觀的彈出提示框，提升用戶體驗。
* JavaScript：實現頁面交互和動態效果。
* AJAX：實現非同步數據傳輸，提升頁面交互速度和流暢度。

### 資料庫設計：
| Tables  | 說明 |
| ------------- |:-------------:|
| HotelLogin      | 飯店登入資料    |
| Hotel       | 飯店基本資料  |
| HotelDetail     | 飯店詳細資料|
| DefaultPicture      | 預設大頭圖片|
| HotelLoginPicture      | 飯店後台帳號頭像|
| Room     | 房間     |
| RoomQuantityPriceByDate      | 房間日期庫存     |
| RoomPicture      | 房間圖片     |
| Customer      | 客戶     |
| OrderTable      | 訂單    |
| OrderItem      | 訂單明細     |
## 資料庫圖表
![image](https://github.com/user-attachments/assets/29ad0936-bdec-467c-ae7d-6bb86ed0c97a)

## 安裝與運行
### 環境需求
* 使用Maven版控與依賴管理
### 部署步驟
1. 克隆專案：
```
bash
git clone https://github.com/chuan025860/sip.git
```
2. 建立資料庫：
* MS SQL 中創建名為 SIP 的資料庫。
* 執行 SIP.sql 文件中的 SQL 語句。

3. 設定配置：
* src/main/resources/application.properties 中，設定資料庫連接的用戶名和密碼。
* src/main/resources/application.properties 中，設定 spring.mail.username 和spring.mail.password (需聯繫作者)
* src/main/resources/application.properties 中，自定義 app.jwt-secret 私鑰
* src/main/java/com/sip/sipproject/controller/CustomerLoginController 中，設定Line登入金鑰 client_secret (需聯繫作者)
* src/main/java/com/sip/sipproject/controller/HotelLoginController 中，設定Line登入金鑰 client_secret (需聯繫作者)
5. 編譯並運行專案

6. 在瀏覽器中訪問：
```
首頁:http://localhost:8080/sip/sipIndex
後台登入:http://localhost:8080/sip/hotel/login
用戶登入:http://localhost:8080/sip/customer/login
```
### 部分截圖展示
![image](https://github.com/user-attachments/assets/a4581e04-8b99-4967-abb9-c35943805e48)
![image](https://github.com/user-attachments/assets/9f574a45-fbe2-4022-9b4b-c7c9a6cc0934)
![image](https://github.com/user-attachments/assets/7e46b27c-728b-4f7d-9a61-d2f2da47d581)
![image](https://github.com/user-attachments/assets/a0e5004c-92f9-4a74-9457-247e06d6784f)
![image](https://github.com/user-attachments/assets/3bdf7d2e-d834-45c3-a451-88636e46aee2)
![image](https://github.com/user-attachments/assets/e451a66a-3027-4c8b-8af9-e1bbe16341cb)
![image](https://github.com/user-attachments/assets/b9cf1623-d99b-4b6d-8878-d7aab6105aea)
![image](https://github.com/user-attachments/assets/d05577c3-67a6-45be-935b-f538ca5ec229)
![image](https://github.com/user-attachments/assets/fee40944-a967-4fb6-8ba3-6e800474b802)
![image](https://github.com/user-attachments/assets/81d7f459-a341-4d27-aaf2-e73ea2a4e70f)
![image](https://github.com/user-attachments/assets/2e6d645c-bad7-4a25-9e8c-4c19664db5e0)
![image](https://github.com/user-attachments/assets/4df2a0ed-8a8f-4bbc-9887-798c35c28e7f)
![image](https://github.com/user-attachments/assets/86549ab7-1e22-4397-bbe9-cf2c8676626b)
![image](https://github.com/user-attachments/assets/9b24546d-5c10-4fe9-a497-6bab2d86b86d)









