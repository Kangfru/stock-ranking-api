### 1. 기술 스택
JDK17 (OpenJDK Temurin-17.0.8)
Spring Boot 3.1.2
- WebFlux
- JPA
- Cache (Caffeine 3.1.8)
- Embedded H2 Database
#### 실행 방법
프로젝트 폴더 내 /jar 실행

실행 시 서버 포트 : 8080
H2 Console 포트 : 8078 (http://127.0.0.1:8078)
---
### 2. 요구사항 및 제반사항
#### 2.1 요구사항
- 종목 인기순
    - 조회수 테이블에서 COUNT 컬럼의 값을 기준으로 정렬
- 가격이 많이 오른
    - 전일 종가로부터 금일 가장 가까운 거래의 가격을 비교해 가장 많이 오른 순으로 정렬
- 가격이 많이 내린
    - 전일 종가로부터 금일 가장 가까운 거래의 가격을 비교해 가장 많이 내린 순으로 정렬
- 거래량 많은
    - 금일 거래된 종목 거래 원장 테이블 상 거래량이 가장 많은 순으로 종목 정렬
#### 2.2 제반사항
- 기본적으로 MSA 기반의 서비스라 가정하고 Non-Blocking 하도록 구성하였음.
- 대용량 트래픽에도 잘 견딜 수 있도록 캐시를 활용.
- Embedded H2 JPA는 Asynchronous를 지원하지 않기 때문에 JPA를 통한 DB 접근은 스케쥴링을 통해 접근하고, 해당 데이터를 캐시에 등록하도록 구현.
    - 예외적으로 데이터가 캐시되기 전 고객이 먼저 접근 하는 경우 (캐시에 값이 없는 경우) 동기적으로 DB에서 데이터를 수취 후 캐시 등록 및 리턴하도록 구현.
- WebFlux를 통해 Server 구현 (Netty)
- Caffeine Cache의 AsyncCache를 통해 비동기적으로 CacheManager를 구현 (내부  Map - ConcurrentHashMap)
---
### 3. Database 구조
#### 3.1 STOCK (종목 정보)
|    | 컬럼명      | 데이터 타입    |       |
|----|----------|-----------|-------|
| PK | STOCK_ID | BIGINT    |       |
|    | CODE     | CHAR(255) | 종목 코드 |
|    | NAME     | CHAR(255) | 종목 이름 |
#### 3.2 STOCK_VIEW (종목 조회수)
|    | 컬럼명           | 데이터 타입  |     |
|----|---------------|---------|-----|
| PK | STOCK_VIEW_ID | BIGINT  |     |
| FK | STOCK_ID      | BIGINT  |     |
|    | COUNT         | INTEGER | 조회수 |
#### 3.3 STOCK_EXCHANGE (종목 거래 원장 테이블)
|    | 컬럼명                | 데이터 타입        |             |
|----|--------------------|---------------|-------------|
| PK | EXCHANGE_ID        | BIGINT        |             |
| FK | STOCK_ID           | BIGINT        |             |
|    | VOLUME             | INTEGER       | 거래량         |
|    | PRICE              | NUMBER(38, 2) | 거래가         |
|    | EXCHANGE_TIMESTAMP | TIMESTAMP(6)) | 거래일시        |
|    | EXCHANGE_TYPE      | CHAR(255)     | SELL OR BUY |
#### 3.4 STOCK_PRICE_HISTORY (종목 일자별 가격)
|    | 컬럼명        | 데이터 타입        |    |
|----|------------|---------------|----|
| PK | HISTORY_ID | BIGINT        |    |
| FK | STOCK_ID   | BIGINT        |    |
|    | OPEN       | NUMBER(38, 2) | 시가 |
|    | CLOSE      | NUMBER(38, 2) | 종가 |
|    | HIGH       | NUMBER(38, 2) | 고가 |
|    | LOW        | NUMBER(38, 2) | 저가 |
|    | PRICE_DATE | DATE          | 일자 |

---
### 4. Api Documentation
#### 4.1 공통 정보
순위 관련 API는 아래와 같이 동일한 쿼리 파라미터 및 응답 데이터를 가짐.

- 쿼리 파라미터

| 이름   | 타입      | 설명     | 필수 |
|------|---------|--------|----|
| page | Integer | 페이지 넘버 | O  |

- 리스폰스 데이터

| 이름            | 타입              | 설명         |                     |       |
|---------------|-----------------|------------|---------------------|-------|
| resultCode    | String          | 응답코드       |
| resultMessage | String          | 응답메시지      |
| pageNumber    | Number          | 페이지 넘버     |
| totalPages    | Number          | 총 페이지      |
| stocks        | Array           | 순위화된 종목 정보 |
|               | 이름              | 타입         | 설명                  |
|               | currentPrice    | Number     | 현재가격(마지막거래)         |
|               | fluctuationRate | Number     | 등락률(전일자 종가에서 현재 가격) |
|               | stock           | Object     | 종목 정보               |
|               |                 | 이름         | 타입                  | 설명    |
|               |                 | stockId    | Number              | 종목 id |
|               |                 | code       | String              | 종목 코드 |
|               |                 | name       | String              | 종목 이름 |

- 응답코드

| 코드 | 설명  |
|--- | ---  |
| 0 | Success |
| 400 | 필수 파라미터 누락 |
| 500 | 서버 내부 에러 |

#### 4.2 인기순 가져오기
기본 정보

| 메서드  | URL |
|---|---|
| GET | http://127.0.0.1:8080/api/v1/stock/ranking/popular|

##### 예제
요청
```
curl -X GET --location "http://127.0.0.1:8080/api/v1/stock/ranking/popular?page=1"
```
응답
```json
{  
  "resultCode": "0",  
  "resultMessage": "Success",  
  "stocks": [  
    {  
      "currentPrice": 38757.00,  
      "fluctuationRate": -56.00,  
      "stock": {  
        "stockId": 91,  
        "name": "쌍용C&E",  
        "code": "003410"  
      }  
    },  
    {  
      "currentPrice": 48393.00,  
      "fluctuationRate": -38.00,  
      "stock": {  
        "stockId": 107,  
        "name": "한화시스템",  
        "code": "272210"  
      }  
    },  
    {  
      "currentPrice": 87976.00,  
      "fluctuationRate": 143.00,  
      "stock": {  
        "stockId": 4,  
        "name": "삼성바이오로직스",  
        "code": "207940"  
      }  
    },  
    {  
      "currentPrice": 88005.00,  
      "fluctuationRate": 45.00,  
      "stock": {  
        "stockId": 14,  
        "name": "POSCO홀딩스",  
        "code": "005490"  
      }  
    },  
    {  
      "currentPrice": 69632.00,  
      "fluctuationRate": 604.00,  
      "stock": {  
        "stockId": 81,  
        "name": "GS",  
        "code": "078930"  
      }  
    },  
    {  
      "currentPrice": 19705.00,  
      "fluctuationRate": -71.00,  
      "stock": {  
        "stockId": 48,  
        "name": "아모레퍼시픽",  
        "code": "090430"  
      }  
    },  
    {  
      "currentPrice": 80912.00,  
      "fluctuationRate": 10.00,  
      "stock": {  
        "stockId": 77,  
        "name": "오리온",  
        "code": "271560"  
      }  
    },  
    {  
      "currentPrice": 61772.00,  
      "fluctuationRate": 64.00,  
      "stock": {  
        "stockId": 38,  
        "name": "KT",  
        "code": "030200"  
      }  
    },  
    {  
      "currentPrice": 60461.00,  
      "fluctuationRate": -34.00,  
      "stock": {  
        "stockId": 23,  
        "name": "두산에너빌리티",  
        "code": "034020"  
      }  
    },  
    {  
      "currentPrice": 39022.00,  
      "fluctuationRate": -54.00,  
      "stock": {  
        "stockId": 41,  
        "name": "SK바이오사이언스",  
        "code": "302440"  
      }  
    },  
    {  
      "currentPrice": 32213.00,  
      "fluctuationRate": -54.00,  
      "stock": {  
        "stockId": 2,  
        "name": "LG에너지솔루션",  
        "code": "373220"  
      }  
    },  
    {  
      "currentPrice": 35082.00,  
      "fluctuationRate": -49.00,  
      "stock": {  
        "stockId": 76,  
        "name": "현대미포조선",  
        "code": "010620"  
      }  
    },  
    {  
      "currentPrice": 40523.00,  
      "fluctuationRate": -49.00,  
      "stock": {  
        "stockId": 55,  
        "name": "SK스퀘어",  
        "code": "402340"  
      }  
    },  
    {  
      "currentPrice": 53250.00,  
      "fluctuationRate": 8.00,  
      "stock": {  
        "stockId": 19,  
        "name": "SK",  
        "code": "034730"  
      }  
    },  
    {  
      "currentPrice": 31432.00,  
      "fluctuationRate": 1826.00,  
      "stock": {  
        "stockId": 3,  
        "name": "SK하이닉스",  
        "code": "000660"  
      }  
    },  
    {  
      "currentPrice": 54780.00,  
      "fluctuationRate": 302.00,  
      "stock": {  
        "stockId": 116,  
        "name": "GS건설",  
        "code": "006360"  
      }  
    },  
    {  
      "currentPrice": 90173.00,  
      "fluctuationRate": -17.00,  
      "stock": {  
        "stockId": 45,  
        "name": "엔씨소프트",  
        "code": "036570"  
      }  
    },  
    {  
      "currentPrice": 45056.00,  
      "fluctuationRate": 50.00,  
      "stock": {  
        "stockId": 49,  
        "name": "기업은행",  
        "code": "024110"  
      }  
    },  
    {  
      "currentPrice": 11501.00,  
      "fluctuationRate": -50.00,  
      "stock": {  
        "stockId": 89,  
        "name": "삼성카드",  
        "code": "029780"  
      }  
    },  
    {  
      "currentPrice": 22563.00,  
      "fluctuationRate": -52.00,  
      "stock": {  
        "stockId": 40,  
        "name": "삼성화재",  
        "code": "000810"  
      }  
    }  
  ],  
  "pageNumber": 1,  
  "totalPages": 6  
}
```
#### 4.3 가격 상승 순 가져오기
기본 정보

| 메서드 | URL                                             |
|-----|-------------------------------------------------|
| GET | http://127.0.0.1:8080/api/v1/stock/ranking/high |

##### 예제
요청
```
curl -X GET --location "http://127.0.0.1:8080/api/v1/stock/ranking/high?page=1"
```
응답
```json
{  
  "resultCode": "0",  
  "resultMessage": "Success",  
  "stocks": [  
    {  
      "currentPrice": 39107.00,  
      "fluctuationRate": 4501.00,  
      "stock": {  
        "stockId": 39,  
        "name": "대한항공",  
        "code": "003490"  
      }  
    },  
    {  
      "currentPrice": 85871.00,  
      "fluctuationRate": 3224.00,  
      "stock": {  
        "stockId": 90,  
        "name": "한화에어로스페이스",  
        "code": "012450"  
      }  
    },  
    {  
      "currentPrice": 16036.00,  
      "fluctuationRate": 3008.00,  
      "stock": {  
        "stockId": 45,  
        "name": "엔씨소프트",  
        "code": "036570"  
      }  
    },  
    {  
      "currentPrice": 59156.00,  
      "fluctuationRate": 2610.00,  
      "stock": {  
        "stockId": 40,  
        "name": "삼성화재",  
        "code": "000810"  
      }  
    },  
    {  
      "currentPrice": 68330.00,  
      "fluctuationRate": 1612.00,  
      "stock": {  
        "stockId": 69,  
        "name": "한국타이어앤테크놀로지",  
        "code": "161390"  
      }  
    },  
    {  
      "currentPrice": 83653.00,  
      "fluctuationRate": 1277.00,  
      "stock": {  
        "stockId": 52,  
        "name": "한국조선해양",  
        "code": "009540"  
      }  
    },  
    {  
      "currentPrice": 95628.00,  
      "fluctuationRate": 1160.00,  
      "stock": {  
        "stockId": 113,  
        "name": "현대로템",  
        "code": "064350"  
      }  
    },  
    {  
      "currentPrice": 16839.00,  
      "fluctuationRate": 1113.00,  
      "stock": {  
        "stockId": 89,  
        "name": "삼성카드",  
        "code": "029780"  
      }  
    },  
    {  
      "currentPrice": 96875.00,  
      "fluctuationRate": 971.00,  
      "stock": {  
        "stockId": 111,  
        "name": "한미사이언스",  
        "code": "008930"  
      }  
    },  
    {  
      "currentPrice": 51988.00,  
      "fluctuationRate": 869.00,  
      "stock": {  
        "stockId": 30,  
        "name": "하나금융지주",  
        "code": "086790"  
      }  
    },  
    {  
      "currentPrice": 89993.00,  
      "fluctuationRate": 793.00,  
      "stock": {  
        "stockId": 70,  
        "name": "코웨이",  
        "code": "021240"  
      }  
    },  
    {  
      "currentPrice": 62937.00,  
      "fluctuationRate": 656.00,  
      "stock": {  
        "stockId": 43,  
        "name": "한화솔루션",  
        "code": "009830"  
      }  
    },  
    {  
      "currentPrice": 38977.00,  
      "fluctuationRate": 583.00,  
      "stock": {  
        "stockId": 50,  
        "name": "SK아이이테크놀로지",  
        "code": "361610"  
      }  
    },  
    {  
      "currentPrice": 78319.00,  
      "fluctuationRate": 526.00,  
      "stock": {  
        "stockId": 119,  
        "name": "제일기획",  
        "code": "030000"  
      }  
    },  
    {  
      "currentPrice": 18312.00,  
      "fluctuationRate": 449.00,  
      "stock": {  
        "stockId": 9,  
        "name": "현대차",  
        "code": "005380"  
      }  
    },  
    {  
      "currentPrice": 83652.00,  
      "fluctuationRate": 400.00,  
      "stock": {  
        "stockId": 93,  
        "name": "일진머티리얼즈",  
        "code": "020150"  
      }  
    },  
    {  
      "currentPrice": 21966.00,  
      "fluctuationRate": 392.00,  
      "stock": {  
        "stockId": 37,  
        "name": "삼성에스디에스",  
        "code": "018260"  
      }  
    },  
    {  
      "currentPrice": 70334.00,  
      "fluctuationRate": 386.00,  
      "stock": {  
        "stockId": 23,  
        "name": "두산에너빌리티",  
        "code": "034020"  
      }  
    },  
    {  
      "currentPrice": 79137.00,  
      "fluctuationRate": 336.00,  
      "stock": {  
        "stockId": 99,  
        "name": "삼성증권",  
        "code": "016360"  
      }  
    },  
    {  
      "currentPrice": 82159.00,  
      "fluctuationRate": 316.00,  
      "stock": {  
        "stockId": 13,  
        "name": "삼성물산",  
        "code": "028260"  
      }  
    }  
  ],  
  "pageNumber": 1,  
  "totalPages": 6  
}
```

#### 4.4 가격 하락 순 가져오기
기본 정보

| 메서드 | URL                                            |
|-----|------------------------------------------------|
| GET | http://127.0.0.1:8080/api/v1/stock/ranking/low |

##### 예제
요청
```
curl -X GET --location "http://127.0.0.1:8080/api/v1/stock/ranking/low?page=1"
```
응답
```json
{  
  "resultCode": "0",  
  "resultMessage": "Success",  
  "stocks": [  
    {  
      "currentPrice": 1846.00,  
      "fluctuationRate": -97.00,  
      "stock": {  
        "stockId": 55,  
        "name": "SK스퀘어",  
        "code": "402340"  
      }  
    },  
    {  
      "currentPrice": 5260.00,  
      "fluctuationRate": -90.00,  
      "stock": {  
        "stockId": 92,  
        "name": "한국금융지주",  
        "code": "071050"  
      }  
    },  
    {  
      "currentPrice": 7257.00,  
      "fluctuationRate": -86.00,  
      "stock": {  
        "stockId": 73,  
        "name": "현대제철",  
        "code": "004020"  
      }  
    },  
    {  
      "currentPrice": 2530.00,  
      "fluctuationRate": -85.00,  
      "stock": {  
        "stockId": 72,  
        "name": "메리츠화재",  
        "code": "000060"  
      }  
    },  
    {  
      "currentPrice": 10131.00,  
      "fluctuationRate": -84.00,  
      "stock": {  
        "stockId": 49,  
        "name": "기업은행",  
        "code": "024110"  
      }  
    },  
    {  
      "currentPrice": 11729.00,  
      "fluctuationRate": -81.00,  
      "stock": {  
        "stockId": 107,  
        "name": "한화시스템",  
        "code": "272210"  
      }  
    },  
    {  
      "currentPrice": 9639.00,  
      "fluctuationRate": -79.00,  
      "stock": {  
        "stockId": 22,  
        "name": "한국전력",  
        "code": "015760"  
      }  
    },  
    {  
      "currentPrice": 13058.00,  
      "fluctuationRate": -79.00,  
      "stock": {  
        "stockId": 16,  
        "name": "현대모비스",  
        "code": "012330"  
      }  
    },  
    {  
      "currentPrice": 23993.00,  
      "fluctuationRate": -78.00,  
      "stock": {  
        "stockId": 86,  
        "name": "한국가스공사",  
        "code": "036460"  
      }  
    },  
    {  
      "currentPrice": 9634.00,  
      "fluctuationRate": -77.00,  
      "stock": {  
        "stockId": 19,  
        "name": "SK",  
        "code": "034730"  
      }  
    },  
    {  
      "currentPrice": 16080.00,  
      "fluctuationRate": -74.00,  
      "stock": {  
        "stockId": 5,  
        "name": "삼성전자우",  
        "code": "005935"  
      }  
    },  
    {  
      "currentPrice": 8773.00,  
      "fluctuationRate": -72.00,  
      "stock": {  
        "stockId": 3,  
        "name": "SK하이닉스",  
        "code": "000660"  
      }  
    },  
    {  
      "currentPrice": 13526.00,  
      "fluctuationRate": -72.00,  
      "stock": {  
        "stockId": 10,  
        "name": "카카오",  
        "code": "035720"  
      }  
    },  
    {  
      "currentPrice": 25348.00,  
      "fluctuationRate": -70.00,  
      "stock": {  
        "stockId": 88,  
        "name": "TIGER 차이나전기차SOLACTIVE",  
        "code": "371460"  
      }  
    },  
    {  
      "currentPrice": 26272.00,  
      "fluctuationRate": -69.00,  
      "stock": {  
        "stockId": 12,  
        "name": "셀트리온",  
        "code": "068270"  
      }  
    },  
    {  
      "currentPrice": 30231.00,  
      "fluctuationRate": -68.00,  
      "stock": {  
        "stockId": 17,  
        "name": "SK이노베이션",  
        "code": "096770"  
      }  
    },  
    {  
      "currentPrice": 28814.00,  
      "fluctuationRate": -65.00,  
      "stock": {  
        "stockId": 106,  
        "name": "이마트",  
        "code": "139480"  
      }  
    },  
    {  
      "currentPrice": 13800.00,  
      "fluctuationRate": -65.00,  
      "stock": {  
        "stockId": 102,  
        "name": "BGF리테일",  
        "code": "282330"  
      }  
    },  
    {  
      "currentPrice": 37909.00,  
      "fluctuationRate": -62.00,  
      "stock": {  
        "stockId": 87,  
        "name": "한미약품",  
        "code": "128940"  
      }  
    },  
    {  
      "currentPrice": 33768.00,  
      "fluctuationRate": -61.00,  
      "stock": {  
        "stockId": 44,  
        "name": "우리금융지주",  
        "code": "316140"  
      }  
    }  
  ],  
  "pageNumber": 1,  
  "totalPages": 6  
}
```
#### 4.5 거래량 순 가져오기
기본 정보

| 메서드 | URL                                               |
|-----|---------------------------------------------------|
| GET | http://127.0.0.1:8080/api/v1/stock/ranking/volume |

##### 예제
요청
```
curl -X GET --location "http://127.0.0.1:8080/api/v1/stock/ranking/volume?page=1"
```
응답
```json
{  
  "resultCode": "0",  
  "resultMessage": "Success",  
  "stocks": [  
    {  
      "currentPrice": 38977.00,  
      "fluctuationRate": 583.00,  
      "stock": {  
        "stockId": 50,  
        "name": "SK아이이테크놀로지",  
        "code": "361610"  
      }  
    },  
    {  
      "currentPrice": 20573.00,  
      "fluctuationRate": -26.00,  
      "stock": {  
        "stockId": 48,  
        "name": "아모레퍼시픽",  
        "code": "090430"  
      }  
    },  
    {  
      "currentPrice": 37635.00,  
      "fluctuationRate": 25.00,  
      "stock": {  
        "stockId": 116,  
        "name": "GS건설",  
        "code": "006360"  
      }  
    },  
    {  
      "currentPrice": 70860.00,  
      "fluctuationRate": -2.00,  
      "stock": {  
        "stockId": 97,  
        "name": "현대차2우B",  
        "code": "005387"  
      }  
    },  
    {  
      "currentPrice": 21966.00,  
      "fluctuationRate": 392.00,  
      "stock": {  
        "stockId": 37,  
        "name": "삼성에스디에스",  
        "code": "018260"  
      }  
    },  
    {  
      "currentPrice": 59156.00,  
      "fluctuationRate": 2610.00,  
      "stock": {  
        "stockId": 40,  
        "name": "삼성화재",  
        "code": "000810"  
      }  
    },  
    {  
      "currentPrice": 71128.00,  
      "fluctuationRate": -26.00,  
      "stock": {  
        "stockId": 21,  
        "name": "카카오뱅크",  
        "code": "323410"  
      }  
    },  
    {  
      "currentPrice": 63761.00,  
      "fluctuationRate": 28.00,  
      "stock": {  
        "stockId": 71,  
        "name": "HD현대",  
        "code": "267250"  
      }  
    },  
    {  
      "currentPrice": 7206.00,  
      "fluctuationRate": -55.00,  
      "stock": {  
        "stockId": 60,  
        "name": "한온시스템",  
        "code": "018880"  
      }  
    },  
    {  
      "currentPrice": 89677.00,  
      "fluctuationRate": 43.00,  
      "stock": {  
        "stockId": 74,  
        "name": "DB손해보험",  
        "code": "005830"  
      }  
    },  
    {  
      "currentPrice": 16839.00,  
      "fluctuationRate": 1113.00,  
      "stock": {  
        "stockId": 89,  
        "name": "삼성카드",  
        "code": "029780"  
      }  
    },  
    {  
      "currentPrice": 46703.00,  
      "fluctuationRate": 134.00,  
      "stock": {  
        "stockId": 76,  
        "name": "현대미포조선",  
        "code": "010620"  
      }  
    },  
    {  
      "currentPrice": 92684.00,  
      "fluctuationRate": 23.00,  
      "stock": {  
        "stockId": 100,  
        "name": "포스코인터내셔널",  
        "code": "047050"  
      }  
    },  
    {  
      "currentPrice": 50601.00,  
      "fluctuationRate": -37.00,  
      "stock": {  
        "stockId": 105,  
        "name": "아모레G",  
        "code": "002790"  
      }  
    },  
    {  
      "currentPrice": 13526.00,  
      "fluctuationRate": -72.00,  
      "stock": {  
        "stockId": 10,  
        "name": "카카오",  
        "code": "035720"  
      }  
    },  
    {  
      "currentPrice": 24311.00,  
      "fluctuationRate": 309.00,  
      "stock": {  
        "stockId": 82,  
        "name": "롯데지주",  
        "code": "004990"  
      }  
    },  
    {  
      "currentPrice": 34493.00,  
      "fluctuationRate": 45.00,  
      "stock": {  
        "stockId": 14,  
        "name": "POSCO홀딩스",  
        "code": "005490"  
      }  
    },  
    {  
      "currentPrice": 18312.00,  
      "fluctuationRate": 449.00,  
      "stock": {  
        "stockId": 9,  
        "name": "현대차",  
        "code": "005380"  
      }  
    },  
    {  
      "currentPrice": 45838.00,  
      "fluctuationRate": -52.00,  
      "stock": {  
        "stockId": 108,  
        "name": "CJ대한통운",  
        "code": "000120"  
      }  
    },  
    {  
      "currentPrice": 39107.00,  
      "fluctuationRate": 4501.00,  
      "stock": {  
        "stockId": 39,  
        "name": "대한항공",  
        "code": "003490"  
      }  
    }  
  ],  
  "pageNumber": 1,  
  "totalPages": 6  
}
```

#### 4.6 거래 데이터 랜덤 생성
기본 정보

| 메서드 | URL                                       |
|-----|-------------------------------------------|
| GET | http://127.0.0.1:8080/api/v1/stock/random |

##### 예제
요청
```
curl -X GET --location "http://127.0.0.1:8080/api/v1/stock/random"
```

응답
```json
{  
  "resultCode": "0",  
  "resultMessage": "Success"  
}
```
---
