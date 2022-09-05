package com.example.stock.facade;

import com.example.stock.service.StockService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * - 한 세션이 레디서사용을 종료하면 대기중엔 세션에 알려줌
 * - 락 획득 재시도를 기본으로 제공
 * - pub-sub 방식으로 구현되어 있기 떄문에 lettuce와 비교했을때 부하가 덜 간다.
 * - 별도 라이브러리를 사용해야함
 */
@Component
public class RedissonLockStockFacade {
    private RedissonClient redissonClient;
    private StockService stockService;

    public RedissonLockStockFacade(RedissonClient redissonClient, StockService stockService) {
        this.redissonClient = redissonClient;
        this.stockService = stockService;
    }

    public void decrease(Long key, Long quantity) {
        RLock lock = redissonClient.getLock(key.toString());

        try {
            // 몇초동안 기다릴 것인지, 몇초동안 점유할 것인지
            boolean available = lock.tryLock(5, 1, TimeUnit.SECONDS);

            if(!available) {
                System.out.println("lock 획득 실패");
                return;
            }

            stockService.decrease(key, quantity);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

}
