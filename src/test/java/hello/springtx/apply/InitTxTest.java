package hello.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.PostConstruct;

@Slf4j
@SpringBootTest
public class InitTxTest {

    @Autowired Hello hello;

    @Test
    void go(){
        // 초기화 코드는 스프링이 초기화 시점에 호출한다.

    }


    @TestConfiguration
    static class InitTxTestConfig{

        @Bean
        Hello hello(){
            return new Hello();
        }
    }

    static class Hello{

        @PostConstruct// 초기화 코드에 트랜잭셔널 코드를 사용하면 적용이 안된다.
        @Transactional // 초기화 코드가 먼저 호출되고 그 다음에 aop가 적용되기 때문이다.
        public void initV1(){
            boolean isActive = TransactionSynchronizationManager.isSynchronizationActive();
            log.info("Hello init @PostConstruct tx active={}", isActive);
        }

        // 초기화 시점에 트랜잭션을 걸어주고 싶으면
        // 이벤트 리스너를 실행시키자
        @EventListener(ApplicationReadyEvent.class)// 스프링이 완전히 떴을 때 호출
        @Transactional
        public void initV2(){
            boolean isActive = TransactionSynchronizationManager.isSynchronizationActive();
            log.info("Hello init ApplicationReadyEvent tx active={}", isActive);
        }
    }



}
