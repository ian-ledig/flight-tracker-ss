<div align="center">
  <img src="https://raw.githubusercontent.com/ian-ledig/flight-tracker-cs/master/public/images/logo/logo.svg" alt="Flight Tracker Logo" width="150" />
  <h1>Flight Tracker (Backend)</h1>
</div>

<p align="center">
  <a href="#english">
    <img src="https://img.shields.io/badge/English-blue?style=for-the-badge" alt="English">
  </a>
  <a href="#japanese">
    <img src="https://img.shields.io/badge/日本語-blue?style=for-the-badge" alt="Japanese">
  </a>
</p>

## Demo <a id="demo"></a>
### SOON
<div align="center">
  <video width="600" autoplay loop muted playsinline>
    <source src="https://raw.githubusercontent.com/ian-ledig/flight-tracker-cs/master/public/demo/flight-tracker-demo.mp4" type="video/mp4">
    Your browser does not support the video tag. Please view the demo at <a href="https://raw.githubusercontent.com/ian-ledig/flight-tracker-cs/master/public/demo/flight-tracker-demo.mp4">this link</a>.
  </video>
</div>

## Overview <a id="english"></a>
Flight Tracker Backend (`flight-tracker-ss`) is a Spring Boot application that serves as the data provider for the `flight-tracker-cs` frontend ([GitHub](https://github.com/ian-ledig/flight-tracker-cs)). It integrates with the AviationStack API to fetch real-time flight data, enabling the frontend to display flight details, KPIs, and flight path visualizations.

## Features
- **Flight Data API**: Provides endpoints to retrieve upcoming flight data based on IATA airline codes, flight numbers, and long-haul filters.
- **AviationStack Integration**: Connects to the AviationStack API for accurate and up-to-date flight information.
- **Support for Frontend**: Serves the `flight-tracker-cs` Next.js application with reliable data.

## Prerequisites
- **Java**: Version 17 or higher (LTS recommended).
- **Maven**: For building and managing dependencies.
- **Git**: For cloning the repository.
- An API key from [AviationStack](https://aviationstack.com/) for flight data.

## Installation
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/ian-ledig/flight-tracker-ss.git
   cd flight-tracker-ss
   ```

2. **Configure the Application**:
   Create an `application.yml` file in the root of the project with the following configuration:
   ```yaml
   aviationstack:
     url: https://api.aviationstack.com/v1
     access-key: YOUR-API-KEY
     mock-enabled: false
   server:
     port: 8080
   ```
   Replace `YOUR-API-KEY` with your AviationStack API key.

3. **Build the Application**:
   ```bash
   mvn clean install
   ```

4. **Run the Application**:
   ```bash
   mvn spring-boot:run
   ```
   The server will start on `http://localhost:8080`.

## Usage
- The backend provides RESTful endpoints consumed by the `flight-tracker-cs` frontend ([GitHub](https://github.com/ian-ledig/flight-tracker-cs)).
- Ensure the AviationStack API key is valid and the server is running before starting the frontend application.

## Contributing
Contributions are welcome! Please fork the repository, create a feature branch, and submit a pull request with your changes. Ensure your code follows the project's coding standards and includes appropriate tests.

## License
This project is licensed under the MIT License. See the `LICENSE` file for details.

## Contact
For questions or feedback, please contact the project maintainer at [ian.ledigjp@gmail.com](mailto:ian.ledigjp@gmail.com).

---

<div align="center">
  <img src="https://raw.githubusercontent.com/ian-ledig/flight-tracker-cs/master/public/images/logo/logo.svg" alt="フライトトラッカーロゴ" width="150" />
  <h1>フライトトラッカー（バックエンド）</h1>
</div>

<p align="center">
  <a href="#english">
    <img src="https://img.shields.io/badge/English-blue?style=for-the-badge" alt="English">
  </a>
  <a href="#japanese">
    <img src="https://img.shields.io/badge/日本語-blue?style=for-the-badge" alt="Japanese">
  </a>
</p>

## デモ <a id="japanese"></a>
### SOON
<div align="center">
  <video width="600" autoplay loop muted playsinline>
    <source src="https://raw.githubusercontent.com/ian-ledig/flight-tracker-cs/master/public/demo/flight-tracker-demo.mp4" type="video/mp4">
    ブラウザがビデオタグをサポートしていません。デモは<a href="https://raw.githubusercontent.com/ian-ledig/flight-tracker-cs/master/public/demo/flight-tracker-demo.mp4">こちら</a>でご覧ください。
  </video>
</div>

## 概要
フライトトラッカーバックエンド（`flight-tracker-ss`）は、Spring Bootアプリケーションであり、`flight-tracker-cs`フロントエンド（[GitHub](https://github.com/ian-ledig/flight-tracker-cs)）のデータプロバイダーとして機能します。AviationStack APIと統合し、リアルタイムのフライトデータを提供し、フロントエンドでフライト詳細、KPI、フライト経路の可視化を表示できるようにします。

## 機能
- **フライトデータAPI**：IATA航空会社コード、フライト番号、長距離フィルターに基づいて近日中のフライトデータを取得するエンドポイントを提供。
- **AviationStack統合**：正確で最新のフライト情報のためにAviationStack APIに接続。
- **フロントエンドサポート**：`flight-tracker-cs` Next.jsアプリケーションに信頼性の高いデータを提供。

## 前提条件
- **Java**：バージョン17以上（LTS推奨）。
- **Maven**：依存関係のビルドと管理に必要。
- **Git**：リポジトリのクローンに必要。
- フライトデータのための[AviationStack](https://aviationstack.com/)のAPIキー。

## インストール
1. **リポジトリをクローン**：
   ```bash
   git clone https://github.com/ian-ledig/flight-tracker-ss.git
   cd flight-tracker-ss
   ```

2. **アプリケーションの設定**：
   プロジェクトのルートに以下の設定を含む`application.yml`ファイルを作成：
   ```yaml
   aviationstack:
     url: https://api.aviationstack.com/v1
     access-key: YOUR-API-KEY
     mock-enabled: false
   server:
     port: 8080
   ```
   `YOUR-API-KEY`をAviationStackのAPIキーに置き換える。

3. **アプリケーションのビルド**：
   ```bash
   mvn clean install
   ```

4. **アプリケーションの実行**：
   ```bash
   mvn spring-boot:run
   ```
   サーバーは`http://localhost:8080`で起動。

## 使用方法
- バックエンドは、`flight-tracker-cs`フロントエンド（[GitHub](https://github.com/ian-ledig/flight-tracker-cs)）が使用するRESTfulエンドポイントを提供。
- AviationStack APIキーが有効であり、フロントエンドアプリケーションを起動する前にサーバーが動作していることを確認。

## 貢献
貢献を歓迎します！リポジトリをフォークし、機能ブランチを作成し、変更をプルリクエストとして提出してください。コードがプロジェクトのコーディング規範に従い、適切なテストを含んでいることを確認してください。

## ライセンス
このプロジェクトはMITライセンスの下でライセンスされています。詳細は`LICENSE`ファイルを参照してください。

## 連絡先
ご質問やフィードバックは、プロジェクトメンテナー（[ian.ledigjp@gmail.com](mailto:ian.ledigjp@gmail.com)）までご連絡ください。