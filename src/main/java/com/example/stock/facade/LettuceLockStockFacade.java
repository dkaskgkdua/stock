package com.example.stock.facade;

import com.example.stock.repository.RedisRepository;
import com.example.stock.service.StockService;
import org.springframework.stereotype.Component;

/**
 * - spring data redis를 이용하면 lettuce가 기본이기 때문에 별도 라이브러리 사용안해도 됨
 * - spin lock방식이기 때문에 동시에 많은 스레드가 lock 획득 대기 상태라면 redis에 부하가 갈 수도 있다.
 * 실무에선?
 * - 재시도가 필요하지 않은 lock은 lettuce 활용
 * - 재시도가 필요하면 redisson 활용
 */
@Component
public class LettuceLockStockFacade {
    private RedisRepository redisRepository;
    private StockService stockService;

    public LettuceLockStockFacade(RedisRepository redisRepository, StockService stockService) {
        this.redisRepository = redisRepository;
        this.stockService = stockService;
    }

    public void decrease(Long key, Long quantity) throws InterruptedException {
        while(!redisRepository.lock(key)) {
            System.out.println("locking!!");
            Thread.sleep(100);
        }

        try {
            stockService.decrease(key, quantity);
        } finally {
            redisRepository.unlock(key);
        }
    }
}
