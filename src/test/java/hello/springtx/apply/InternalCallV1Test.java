package hello.springtx.apply;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@SpringBootTest
public class InternalCallV1Test {

    @Autowired CallService callService;

    @Test
    void printProxy(){
        log.info("callService class={}", callService.getClass());

    }

    @Test
    void internalCall(){
        callService.internal();
    }

    @Test
    void externalCall(){
        callService.external();
    }


    @TestConfiguration
    static class InternalCallV1TestConfig{

        @Bean
        public CallService callService(){
            return new CallService();
        }
    }


    @Slf4j
    static class CallService{

        public void external(){
            log.info("call external");
            printTxInfo();
            internal();// 외부에서 요청이 와서 인터널 요청을 할 때
            // 트랜잭셔널이 걸린 함수를 외부에서 호출하면
            // 프록시 객체가 생성이 안되서 트랜잭션 반영이 안 된다!!!

            // 이렇게 트랜잭션이 없는 외부에서 요청이 오면 프록시 객체가 생성이 안 된다...
            // external()[트랜잭션 적용 X] -> internal()[트랜잭션 적용 O]
            // -> 프록시를 안 거치고 내가 내거 그냥 호출한 거다.
            // 즉 그냥 서비스가 프록시를 부른게 아니라 자기 자신의 매서드를 호출한거다.
            // 나 자신의 인스턴스의 함수를 호출한 것
            // 프록시 방식의 aop의 한계
            // 매서드 내부 호출에서는 aop나 어노테이션 적용이 안 된다.
        }

        @Transactional // CGLIB가 붙은 프록시 객체 생성, 이걸 바로 실행하면 프록시 객체가 생성됨
        public void internal(){
            log.info("call internal");
            printTxInfo();
        }

        private void printTxInfo(){
            boolean txActive = TransactionSynchronizationManager.isSynchronizationActive();
            log.info("tx active = {}", txActive);
            boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
            log.info("tx readOnly = {}",readOnly);
        }
    }


}
