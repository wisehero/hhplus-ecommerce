# 항해 플러스 이커머스 프로젝트 조회 성능 최적화 전략

## 개요

이커머스 플랫폼은 대량의 데이터를 처리하고 사용자에게 빠른 응답을 제공해야 하므로 성능 최적화는 시스템의 핵심 요소입니다.   
본 문서는 항해 플러스 이커머스 프로젝트의 조회 성능을 최적화하기 위한 전략과 실제 구현 방안을 다룹니다.  
현재 이커머스 시스템은 주문, 상품, 포인트, 쿠폰 도메인을 다루고 있으며 대표적으로 아래와 같은 API를 제공합니다.

- 상품 조회 API
- 선착순 쿠폰 발급 API
- 포인트 충전 API
- 포인트 결제 API
- 주문 API
- 인기 상품 조회 API

본 문서에서는 이 기능들을 수행하면서서 발생할 수 있는 대표적인 몇 가지 상황들의 조회 성능 문제를 분석하고,  
데이터베이스 인덱싱 전략을 통한 최적화 방안을 제시합니다.

## ✅ published_coupon 테이블에서 사용자 쿠폰 발급 여부 조회 성능 분석

### 1. 시나리오 및 문제 정의

published_coupon 테이블은 유저에게 발급된 쿠폰 정보를 저장하며, 약 1,000만 건의 데이터가 존재합니다.  
선착순 쿠폰 발급 API에서는 특정 사용자가 특정 쿠폰을 이미 발급받았는지 확인하는 쿼리가 필수적으로 실행됩니다.

```sql
SELECT 1
FROM published_coupon
WHERE user_id = 1
  AND coupon_id = 1 LIMIT 1;
```

### 2. 실행 계획 분석

**2-1 CASE 1: 결과가 존재하는 경우**

```sql
-> Limit: 1 row(s)  (cost=996832 rows=1) (actual time=0.549..0.549 rows=1 loops=1)
    -> Filter: ((published_coupon.coupon_id = 432) and (published_coupon.user_id = 8478465))  (cost=996832 rows=94498) (actual time=0.548..0.548 rows=1 loops=1)
        -> Table scan on published_coupon  (cost=996832 rows=9.45e+6) (actual time=0.536..0.541 rows=77 loops=1)
```

- 전체 테이블 스캔(Full Table Scan)이 발생하고 있습니다.
- 운이 좋게도 조건에 맞는 결과를 빠르게 찾아 LIMIT 1 조건으로 인해 스캔이 조기 종료되었습니다.
- 실행 시간: 약 0.549ms

2-2. CASE 2: 결과가 존재하지 않는 경우

```sql
-> Limit: 1 row(s)  (cost=996832 rows=1) (actual time=1882..1882 rows=0 loops=1)
    -> Filter: ((published_coupon.coupon_id = 1) and (published_coupon.user_id = 8478465))  (cost=996832 rows=94498) (actual time=1882..1882 rows=0 loops=1)
        -> Table scan on published_coupon  (cost=996832 rows=9.45e+6) (actual time=0.391..1622 rows=10e+6 loops=1)
```

- 결과가 존재하지 않아 테이블 전체(1000만 건)를 스캔했습니다.
- LIMIT 1 조건이 무의미해졌으며, 모든 레코드를 검사해야 했습니다.
- 실행 시간: 약 1.882초 (결과가 존재하는 경우보다 약 3,600배 느림)

이러한 성능 차이는 사용자 경험에 직접적인 영향을 미치며, 특히 선착순 쿠폰 발급과 같은 실시간 트랜잭션에서 심각한 병목 현상을 유발할 수 있습니다.

### 3. 성능 개선 전략: 복합 인덱스 구현

이 문제를 해결하기 위해 아래와 같은 복합 인덱스를 추가할 것입니다.

```sql
CREATE INDEX idx_user_coupon ON published_coupon (user_id, coupon_id);
```

이 복합 인덱스의 설계 근거는 다음과 같습니다

- 카디널리티 고려: 일반적으로 이커머스 시스템에서 사용자 수(user_id)는 쿠폰 원장 수(coupon_id)보다 많습니다. 높은 카디널리티를 가진 컬럼을 인덱스 앞부분에 배치하는 것이 효율적입니다. 또한 두
  조건은 모두 동치 조건이므로 카디 널리티가 높은 것부터 놓는 것이 효율적입니다.

- 쿠폰 발급 정책 반영: 시스템에서는 사용자당 동일 쿠폰은 최대 1개만 발행되므로, (user_id, coupon_id) 쌍은 유일성을 가집니다.

- 쿼리 패턴 최적화: WHERE 절의 조건 순서(user_id, coupon_id)와 인덱스 컬럼 순서를 일치시켜 인덱스 효율성을 극대화합니다.

### 4. 성능 개선 결과

```sql
-> Limit: 1 row(s)  (cost=1.1 rows=1) (actual time=0.0124..0.0124 rows=0 loops=1)
    -> Covering index lookup on published_coupon using idx_user_coupon (user_id=8478465, coupon_id=1)  (cost=1.1 rows=1) (actual time=0.0114..0.0114 rows=0 loops=1)
```

인덱스 적용 후 쿼리 실행 시간이 1.882초에서 0.0124ms로 단축되었습니다.(약 151,774배 성능 향상)  
커버링 인덱스(Covering Index)가 적용되어 테이블 접근 없이 인덱스만으로 쿼리가 해결되었습니다.  
사용자 쿠폰 정보 조회 쿼리도 유사한 성능 개선을 보였습니다.

```sql
##
사용자 쿠폰 조회 결과 X
explain analyze
SELECT id,
       user_id,
       coupon_id,
       discount_type,
       discount_value,
       valid_from,
       valid_to,
       is_used,
       issued_at,
       ordered_at,
       updated_at
FROM published_coupon
WHERE coupon_id = 1
  AND user_id = 8478465;

-> Index lookup on published_coupon using idx_user_coupon (user_id=8478465, coupon_id=1)  (cost=1.09 rows=1) (actual time=0.0993..0.0993 rows=0 loops=1)
```

이러한 성능 개선은 선착순 쿠폰 발급 API와 주문 시 쿠폰 적용 로직에서 사용자 경험을 크게 향상시킬 수 있습니다.

## ✅ Orders 테이블에서 시간 범위에 따른 주문 조회 성능 분석

### 1. 시나리오 및 문제 정의

항해 플러스 이커머스 시스템에서는 인기 상품 분석을 위해 매 정각마다 결제 완료 상태이면서 최근 1시간 이내에 생성된 주문 정보를 조회합니다.

```sql
SELECT id,
       user_id,
       product_id,
       order_status,
       payment_method,
       total_price,
       discount_price,
       ordered_at,
       updated_at
FROM orders
WHERE order_status = 'PAID'
  AND ordered_at >= '2025-04-17 11:00:00'
  AND ordered_at < '2025-04-17 12:00:00';
```

### 2. 실행 계획 분석

테스트 환경은 다음과 같습니다.

- MySQL 8.0.32
- 2025년 01월 01일부터 04월 17일까지의 데이터 존재
- 총 약 1,000만 건의 주문 데이터

```sql
-> Filter: ((orders.order_status = 'PAID') and (orders.ordered_at >= TIMESTAMP'2025-04-17 11:00:00') and (orders.ordered_at < TIMESTAMP'2025-04-17 12:00:00'))  (cost=1.01e+6 rows=107700) (actual time=2356..2610 rows=24858 loops=1)
    -> Table scan on orders  (cost=1.01e+6 rows=9.69e+6) (actual time=0.373..2100 rows=10e+6 loops=1)
```

분석

- 인덱스가 없는 상태에서 테이블 풀 스캔이 발생하고 있습니다.
- 실행 시간: 약 2.61초

인덱스가 없는 상황에서 전체 테이블을 스캔하고 있습니다.
이러한 성능은 데이터가 증가할수록 더욱 악화되며, 정기적으로 실행되는 배치 작업에서 시스템 전체 성능에 영향을 미칠 수 있습니다.

### 3. 성능 개선 전략: 인덱스 비교 분석

이 문제를 해결하기 위해 아래와 같은 인덱스를 추가할 것입니다.

```sql
CREATE INDEX idx_created_status ON orders (ordered_at, order_status);

CREATE INDEX idx_order_status_ordered_at ON orders (order_status, ordered_at);
```

두 인덱스 모두 테스트를 해본 결과, 두 인덱스를 각각 적용해서 얻은 실행 계획 분석 결과는 아래와 같습니다.

**CASE 1 : idx_created_status**

```sql
-> Index range scan on orders using idx_created_status over ('2025-04-17 11:00:00' <= ordered_at <= '2025-04-17 12:00:00' AND 'PAID' <= order_status),
with index condition: ((orders.order_status = 'PAID') and (orders.ordered_at >= TIMESTAMP'2025-04-17 11:00:00') and (orders.ordered_at < TIMESTAMP'2025-04-17 12:00:00'))  
(cost=87554 rows=82162) (actual time=10.3..54 rows=24858 loops=1)
```

**CASE 2 : idx_status_created**

```sql
-> Index range scan on orders using idx_status_created over (order_status = 'PAID' AND '2025-04-17 11:00:00' <= ordered_at < '2025-04-17 12:00:00'), 
with index condition: ((orders.order_status = 'PAID') and (orders.ordered_at >= TIMESTAMP'2025-04-17 11:00:00') and (orders.ordered_at < TIMESTAMP'2025-04-17 12:00:00')) 
(cost=51614 rows=48230) (actual time=8.88..49.6 rows=24858 loops=1)
```

위의 실행 계획을 분석해보면 현재 데이터셋 기준 실행 속도의 차이는 크지 않지만, idx_created_status 인덱스는 높은 카디널리티에서  
낮은 카디널리티 순서로 인해 필터링 효율성이 떨어질 수 있습니다. 그리고 주문 테이블의 order_status 컬럼은 우리의 시스템에서  
대부분의 값이 PAID일 것이기 때문에 (5분이 지나도 결제되지 않은 주문 건은 EXPIRED로 변경하므로) 필터링에서 대부분의 row가 남습니다.

### 4. 결론

결과적으로 idx_order_status_ordered_at 인덱스를 사용하는 것이 더 효율적일 것입니다. 뿐만 아니라 주문의 id 자체만 필요한 경우에는  
스프링부트 애플리케이션에서 DTO(id, order_status, orderedAt)로 변환하여 조회한다면 커버링 인덱스가 적용되어 더욱 빠르게 조회할 수 있습니다.

```sql
SELECT id, order_status, ordered_at
FROM orders
WHERE order_status = 'PAID'
  AND ordered_at >= '2025-04-17 11:00:00'
  AND ordered_at < '2025-04-17 12:00:00';

-> Filter: ((orders.order_status = 'PAID') and (orders.ordered_at >= TIMESTAMP'2025-04-17 11:00:00') and (orders.ordered_at < TIMESTAMP'2025-04-17 12:00:00'))  (cost=10201 rows=48230) (actual time=0.204..8.81 rows=24858 loops=1)
    -> Covering index range scan on orders using idx_status_created over (order_status = 'PAID' AND '2025-04-17 11:00:00' <= ordered_at < '2025-04-17 12:00:00')  (cost=10201 rows=48230) (actual time=0.194..5.61 rows=24858 loops=1)
```

하지만 우리의 시스템에서 order_status는 미결제(PENDING)에서 결제 완료(PAID)로, 혹은 미결제(PENDING)이 5분이 넘으면  
주문 만료(EXPIRED)로 변경 되는 일이 빈번하기 때문에 인덱스 재정렬/재배치 작업 역시 빈번하게 일어나 인덱스를 손상시킵니다.  
1000만 건의 수준에서는 크게 느린 편이 아니지만 인덱스 손상으로 인한 성능 저하가 발생할 경우 ordered_at 컬럼을 단독 인덱스로 지정하거나  
주문 상태에 따른 테이블 분리를 고려해볼 수 있습니다.

## ✅ 인기상품 조회 성능 분석

### 1. 시나리오 및 문제 정의

항해 플러스 이커머스 시스템에서는 인기 상품을 조회하는 기능을 제공하며 이를 통해 일간, 주간, 월간 인기 상품을 조회할 수 있고 상품은 판매량 순으로 보여집니다.
인기 상품 조회 쿼리는 다음과 같습니다.

```sql
SELECT *
FROM bestseller
WHERE created_at >= NOW() - INTERVAL 1 DAY -- 일간
ORDER BY sales_count DESC
    LIMIT 100;

SELECT *
FROM bestseller
WHERE created_at >= NOW() - INTERVAL 7 DAY
ORDER BY sales_count DESC
    LIMIT 100;

SELECT *
FROM bestseller
WHERE created_at >= NOW() - INTERVAL 1 MONTH
ORDER BY sales_count DESC
    LIMIT 100;
```

### 2. 실행 계획 분석

테스트 환경은 다음과 같습니다.

- MySQL 8.0.32
- 2025년 01월 18일부터 04월 17일까지의 데이터 존재
- 총 100만 건의 주문 데이터

```sql
-> Limit: 100 row(s)  (cost=36578 rows=100) (actual time=354..354 rows=100 loops=1)
    -> Sort: bestseller.sales_count DESC, limit input to 100 row(s) per chunk  (cost=36578 rows=992118) (actual time=354..354 rows=100 loops=1)
        -> Filter: (bestseller.created_at >= <cache>((now() - interval 1 day)))  (cost=36578 rows=992118) (actual time=0.0868..352 rows=11228 loops=1)
            -> Table scan on bestseller  (cost=36578 rows=992118) (actual time=0.0643..317 rows=1e+6 loops=1)

```

분석

- 인덱스가 없는 상태에서 테이블 풀 스캔이 발생하고 있습니다.
- 전체 테이블 스캔 → 필터링(1일치) → 정렬(매출순) → 제한(100건) 순으로 처리
- 병목은 **정렬 단계(354ms)**로, 11,228행을 메모리 정렬하는 데 대부분 시간이 소요되고 있습니다.

### 3. 성능 개선 전략: 인덱스 비교 분석

이 문제를 해결하기 위해 아래와 같은 인덱스를 추가할 것입니다.

```sql
CREATE INDEX idx_created_at_sales_count ON bestseller (created_at, sales_count);

CREATE INDEX idx_sales_count_created_at ON bestseller (sales_count, created_at);
```

두 인덱스 모두 테스트를 해본 결과, 두 인덱스를 각각 적용해서 얻은 실행 계획 분석 결과는 아래와 같습니다.

**CASE 1 : idx_created_at_sales_count**

```sql
-- 일간 조회
-> Limit: 100 row(s)  (cost=12407 rows=100) (actual time=92..92 rows=100 loops=1)
    -> Sort: bestseller.sales_count DESC, limit input to 100 row(s) per chunk  (cost=12407 rows=21498) (actual time=92..92 rows=100 loops=1)
        -> Index range scan on bestseller using idx_created_at_sales_count over ('2025-04-16 16:44:08' <= created_at), with index condition: (bestseller.created_at >= <cache>((now() - interval 1 day)))  (cost=12407 rows=21498) (actual time=0.0384..90.1 rows=11228 loops=1)

-- 월간 조회
-> Limit: 100 row(s)  (cost=103520 rows=100) (actual time=463..463 rows=100 loops=1)
    -> Sort: bestseller.sales_count DESC, limit input to 100 row(s) per chunk  (cost=103520 rows=992118) (actual time=463..463 rows=100 loops=1)
        -> Filter: (bestseller.created_at >= <cache>((now() - interval 1 month)))  (cost=103520 rows=992118) (actual time=0.141..423 rows=345396 loops=1)
            -> Table scan on bestseller  (cost=103520 rows=992118) (actual time=0.134..384 rows=1e+6 loops=1)
```

- 일간 조회에서는 준수한 성능을 보여줍니다.
- 하지만 월간 조회를 넘어가는 경우 풀 스캔을 하면서 비효율이 발생합니다.
- 즉 월간을 너머 분기, 반기, 연간 단위로 조회할 경우 인덱스 효율성이 떨어집니다.

**CASE 2 : idx_sales_count_created_at**

```sql
-- 일간
-> Limit: 100 row(s)  (cost=0.343 rows=33.3) (actual time=0.179..39 rows=100 loops=1)
    -> Filter: (bestseller.created_at >= <cache>((now() - interval 1 day)))  (cost=0.343 rows=33.3) (actual time=0.178..39 rows=100 loops=1)
        -> Index scan on bestseller using idx_sales_count_created_at (reverse)  (cost=0.343 rows=100) (actual time=0.175..38.7 rows=7985 loops=1)

-- 월간
-> Limit: 100 row(s)  (cost=0.343 rows=33.3) (actual time=0.0483..2.5 rows=100 loops=1)
    -> Filter: (bestseller.created_at >= <cache>((now() - interval 1 month)))  (cost=0.343 rows=33.3) (actual time=0.0473..2.49 rows=100 loops=1)
        -> Index scan on bestseller using idx_sales_count_created_at (reverse)  (cost=0.343 rows=100) (actual time=0.0439..2.48 rows=100 loops=1)
```

- 반면 정렬 조건인 판매량을 앞에 두면 역인덱스 스캔으로 정렬과 필터링을 인덱스 단계에서 동시에 해결합니다.
- 따라서 파일 소트와 풀스캔이 전혀 없어 실행 시간이 극적으로 감소합니다.

### 4. 결론

결과적으로 idx_sales_count_created_at 인덱스를 사용하는 것이 더 효율적입니다. 이후 더 많은 데이터 삽입이 발생한다면  
캐시를 활용하거나 기간 별 파티션을 나누는 방법을 구상할 수 있습니다.

## 추가 고려 사항

이외에도 다음과 같은 컬럼에 인덱스를 추가하는 것을 고려할 수 있습니다.

1. 주문에 속하는 주문 상품(OrderProdudct) 테이블의 order_id 컬럼 단독 인덱스
    - 근거: 주문 상세 조회 시 특정 주문(order_id)에 속한 모든 상품을 조회하는 쿼리가 빈번하게 발생합니다.
    - 효과: 주문 상세 페이지 로딩 시간이 크게 단축되며, 특히 대량 주문(많은 상품을 포함한 주문)에서 효과적입니다.

2. 상품별 판매량 분석 등을 위해 주문 상품 테이블의 product_id 컬럼 단독 인덱스
    - 근거: 상품별 판매량 분석, 인기 상품 조회 등에서 특정 상품의 주문 이력을 조회하는 쿼리가 필요합니다.
    - 효과: 상품 분석 대시보드 및 리포트 생성 속도가 개선됩니다.
    - 활용 사례: "이 상품을 구매한 사람들이 함께 구매한 상품" 추천 기능 구현 시 핵심 인덱스로 활용됩니다.

## 📌 시나리오별 성능 최적화 전략 요약

| 시나리오 | 목적/기능 설명                     | 대상 테이블             | 문제점 요약                                           | 인덱스 전략                                                      | 성능 개선 효과 요약                                |
|------|------------------------------|--------------------|--------------------------------------------------|-------------------------------------------------------------|--------------------------------------------|
| ①    | 사용자 쿠폰 발급 여부 확인 (선착순 쿠폰 API) | `published_coupon` | 1000만 건 풀스캔 + `LIMIT 1`이 무의미 (조건 만족 못하면 전건 탐색됨)  | `idx_user_coupon (user_id, coupon_id)`                      | 1.8초 → 0.012ms (약 150,000배 개선), 커버링 인덱스 가능 |
| ②    | 사용자 쿠폰 상세 조회                 | `published_coupon` | user_id만 조건이면 인덱스 일부만 사용 → 성능 저하 위험              | 동일하게 `idx_user_coupon`                                      | 전체 정보 조회 시에도 빠른 인덱스 탐색 및 커버링 가능            |
| ③    | 최근 1시간 결제 완료 주문 조회 (정각 배치용)  | `orders`           | 1000만 건 테이블에서 `PAID` + `ordered_at` 필터링 시 풀스캔 발생 | `idx_status_created (order_status, ordered_at)`             | Index Range Scan + 정렬 제거 → 2.6초 → 49ms 수준  |
| ④    | 인기 상품 조회 (일간/주간/월간)          | `bestseller`       | created_at만 조건이면 정렬 위해 파일 소트 발생, 월간 이상에서 풀스캔 발생  | `idx_sales_count_created_at (sales_count DESC, created_at)` | 정렬+필터링 모두 인덱스로 해결, 월간 조회도 수 ms로 가능         |
| ⑤    | 주문 상세 상품 조회 (1:N 관계)         | `order_product`    | 특정 주문(order_id) 하위 상품 전체 조회 시 테이블 전건 조회될 위험      | `idx_order_id`                                              | 주문 상세 페이지 로딩 속도 향상                         |
| ⑥    | 상품별 주문/판매량 조회                | `order_product`    | product_id로 필터링 시 선형 탐색 발생 가능                    | `idx_product_id`                                            | 상품별 통계 분석 속도 개선, 추천 알고리즘 등에 유용             |

---

## ✅ 핵심 정리 및 설계 원칙

- **카디널리티 높은 컬럼을 복합 인덱스 앞에 배치하는 것이 일반적이다.**
    - 단, 조건이 동치(=) vs 범위(BETWEEN, >=) 조합일 경우: 동치 조건 우선 배치
- **Covering Index를 활용하라.**
    - 쿼리에 사용되는 모든 컬럼을 인덱스에 포함시키면 성능 급상승
- **정렬이 필요한 쿼리는 정렬 대상 컬럼이 인덱스에 포함되어 있어야 한다.**
    - `ORDER BY sales_count DESC` + `created_at >= ?` → `idx_sales_count_created_at`
- **빈번한 UPDATE가 발생하는 컬럼은 인덱스 설계 시 신중해야 한다.**
    - 예: 주문 상태(order_status)는 자주 바뀌므로 인덱스 손상 가능성 있음


