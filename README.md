# SIP（飯店預訂系統）

**作品簡介**

__SIP 是一個基於 Spring Boot 開發的飯店預訂系統，提供了飯店註冊、客房管理、訂單處理和客戶管理等功能。前端採用 Thymeleaf 作為模板引擎，結合 HTML、CSS、Bootstrap 進行頁面設計，並使用 SweetAlert 提供友好的交互提示。使用 MSSQL 作為關聯型資料庫，並實現了多種資料表關聯，如一對多、一對一關係。__

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
5. 諸多其他關聯
* 並發處理：使用悲觀鎖進行資源保護
* Maven: 使用 pom.xml 進行版本控制和依賴管理
  

### 前端：
* Thymeleaf：模板引擎，用於服務端渲染 HTML。
* HTML5 & CSS3：頁面結構和樣式。
* Bootstrap：響應式佈局和 UI 元件。
* SweetAlert：美觀的彈出提示框，提升用戶體驗。
* JavaScript：實現頁面交互和動態效果。

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
## Blocks of code
```
bash
git clone https://github.com/yourusername/SIP.git
```

